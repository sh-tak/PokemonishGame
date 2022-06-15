package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import server.bin.Monster;
import server.bin.Move;


public class Server extends Thread {
    static int MAX_CONNECTIONS = 2;
    private static final int PORT = 8080;
    ServerSocket serverSocket;
    Channel channels[] = new Channel[MAX_CONNECTIONS]; // クライアント間で通信するためのメンバ変数

    public static void main(String[] args) throws IOException {
        new Server();
    }

    public Server() {
        this.start();
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("サーバーが起動しました\n");
            int i;
            while(true){
                for(i = 0; i < MAX_CONNECTIONS; i++){
                    if(channels[i] == null){
                        break;
                    }
            }
            Socket socket = serverSocket.accept();
            if(i == MAX_CONNECTIONS){
                socket.close();
                continue;
            }
            channels[i] = new Channel(socket, this,/* id = */i);
            logging("クライアントid:" + i + "が接続しました");
            }
        } catch (IOException e) {
            System.out.println("サーバーエラー");
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 一人のクライアントに対して送信
    public void sendOne(String msg, Channel ch) {
        ch.out.println(msg);
    }

    // 全員のクライアントに対して送信(ブロードキャスト)
    public void sendAll(String msg) {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (channels[i] != null) {
                channels[i].out.println(msg);
            }
        }
    }
    
    // 名前重複確認
    public boolean isDuplicate(String name) {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (channels[i] != null && channels[i].name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    // サーバーに名前を登録
    public void setName(String name, int id) {
        channels[id].name = name;
    }

    public String findOpponent(int id) {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (channels[i] != null && i != id) {
                return channels[i].name;
            }
        }
        return "";
    }

    public int getIdByName(String name) {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (channels[i] != null && channels[i].name == name) {
                return i;
            }
        }
        return -1;
    }
    
    public void showMoveLineup(int id) {
        String str  = "技は4種類あります\n";
        for(int i = 0; i < 4; i++){
            str +=  "技"+ i + ": " + channels[id].monster.moves[i].toString() + "\n";
        }
        str += "使いたい技を数字で入力してください";
        sendOne(str, channels[id]);
    }

      public boolean isGameover(int myId, int opponentId) {
        if(channels[myId].monster.hp <= 0) {
            return true;
        }else if (channels[opponentId].monster.hp <= 0) {
            return true;
        }
        return false;
    }

    // 攻撃後にクライアント全員に攻撃結果を表示 moveIdxはクライアントが選択した技の番号
    public void useMove(int myId, int opponentId, int moveIdx) {
        String myName = channels[myId].name;
        String opponentName = channels[opponentId].name;
        Move myMove = channels[myId].monster.moves[moveIdx];
        String partition = "--------------";

        // 選んだ技の使用可能回数が0なら悪あがきを繰り出す
        if (channels[myId].monster.moves[moveIdx].count <= 0) {
            int damage = channels[myId].monster.moves[moveIdx].getStruggleGamage();
            channels[opponentId].monster.hp -= damage;
            sendAll(partition + "\n" +
                    myName + "は" +
                    myMove.name + "を使用できません\n" +
                    "代わりに悪あがきを繰り出した!\n" +
                    opponentName + "に" +
                    damage + "のダメージを与えた!\n" + partition);
            return;
        } else {
            String compatibility;
            double multiplier = myMove.calculateMultiplier(channels[myId].monster,
                    channels[opponentId].monster);
            logging("倍率" + multiplier);
            if(multiplier < 1.0) {
                compatibility = "効果はいまいちだ\n";
            }else if(multiplier < 1.5) {
                compatibility = "効果はまあまあだ\n";
            }else{
                compatibility = "効果は抜群だ\n";
            }

            int proccessedDamage = (int) (channels[myId].monster.moves[moveIdx].damage * multiplier);
            channels[myId].monster.moves[moveIdx].count--;
            channels[opponentId].monster.hp -= proccessedDamage;
            sendAll(partition + "\n" +
                    myName + "は" +
                    myMove.name + "を使用した!\n" +
                    compatibility + 
                    opponentName + "に" +
                    proccessedDamage + "のダメージを与えた!\n" + partition);
        }
    }

    public void showStats(int id){
        sendOne(channels[id].monster.toString(), channels[id]);
    }

    public int isFirstTurn(int id) {
        int mySpeed = channels[id].monster.speed.getValue();
        int opponentSpeed = channels[id==1?0:1].monster.speed.getValue();
        if(mySpeed > opponentSpeed) {
            return 1;
        }else if(mySpeed < opponentSpeed) {
            return 0;
        }else {
            return id==1?0:1;
        }
    }

    public void showCurrentHp(int myId, int opponentId) {
        sendAll("現在の" + channels[myId].monster.name + "のHPは " + 
            (channels[myId].monster.hp > 0 ? channels[myId].monster.hp : "0") + "です");
        sendAll("現在の" + channels[opponentId].monster.name + "のHPは " + 
            (channels[opponentId].monster.hp > 0 ? channels[opponentId].monster.hp : "0") + "です\n");
    }

    // csvファイルmoves.csvから技リストを読み込む
    // Channelごとに技リストを取得している。
    // -> サーバー起動時で技リスト取得、保存し、それをChannelに配布したほうがいいかも
    public Move[] fetchMovesFromCsv(String filename) {
        int i = 0;
        try {
            ArrayList<Move> moves = new ArrayList<Move>();
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] moveInfo = line.split(",");
                    if (moveInfo.length > 4) {
                        moves.add(new Move(
                                moveInfo[0],
                                Integer.parseInt(moveInfo[1]),
                                Integer.parseInt(moveInfo[2]),
                                Integer.parseInt(moveInfo[3]),
                                moveInfo[4].equals("1") ? true : false));
                        logging(moves.get(i).name + "を読み込みました");
                        i++;
                    }
                }
            }
            return moves.toArray(new Move[moves.size()]);
        } catch (IOException e) {
            System.out.println("for debug" +
                    "moves.csvのパスを確認してください\n" +
                    "(MacOS12.0.01で動作確認済み)\n" +
                    "パスはChannel.java内で指定しています\n");
        }
        return null;
    }

    public Move[] chooseFourMoves(Move[] moves) {
        // 実例: moves.length = 5 のとき[0,1,2,3,4]をシャッフルして[2,3,1,4,0]
        // のようになる。このときmoves[2], moves[3], moves[1], moves[4]のリストを返す。
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < moves.length; i++) {
            list.add(i);
        }
        logging(moves.length + "個数の技をmoves.csvから追加しました");
        for (int i = 0; i < 30; i++) {
            Collections.shuffle(list);
        }
        Move returnMove[] = new Move[4];
        for (int i = 0; i < 4; i++) {
            returnMove[i] = moves[list.get(i)];
        }
        return returnMove;
    }

    public Monster getMonster(Move[] moves, String name) {
        return new Monster(moves, name);
    }

    public void logging(String str) {
        System.out.println(str);
    }
}
