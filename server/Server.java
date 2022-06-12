package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import server.bin.Monster;
import server.bin.Move;

public class Server extends Thread {
    static int MAX_CONNECTIONS = 2;
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
            serverSocket = new ServerSocket(8000);
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


    public boolean isValidNum(String stringOfNum) {
        return (stringOfNum.matches("0") ||
                stringOfNum.matches("1") ||
                stringOfNum.matches("2") ||
                stringOfNum.matches("3"));
    }

    public String isValidName(String name) {
        if (name.equals("") || name == null) {
            return "zeroLentghNameError";
        } else if (existSameName(name)) {
            return "duplicateNameError";
        } else {
            return "validName";
        }
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

    public boolean existSameName(String name) {
        logging("input name: " + name);
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (channels[i] != null && name.equals(channels[i].name)) {
                return true;
            }
        }
        return false;
    }

    // クライアントが自分のターンになったた使える技の一覧を表示
    public void showMoveLineup(Channel ch) {
        sendOne("--- 技は4種類あります ---", ch);
        for (int i = 0; i < ch.monster.moves.length; i++) {
            sendOne("技" + i + " :" + ch.monster.moves[i].toString() + "\n", ch);
        }
        sendOne("--- 使いたい技の番号を入力してください ---", ch);
    }

    // ターンの終了ごとにクライアント全員にHP状況を表示
    public void showAllHp(Channel ch1, Channel ch2) {
        sendAll("Now " + ch1.monster.name + "'s health is " + ch1.monster.hp);
        sendAll("Now " + ch2.monster.name + "'s health is " + ch2.monster.hp);
        sendAll("-----------------------------------------------------");
    }

    // ゲームが終了(どちらかのhpが0)するならtrueを返す。攻撃後に毎回確認する。
    public boolean isGameover(int myId, int opponentId) {
        if(channels[myId].monster.hp <= 0) {
            sendOne("あなたの負けです", channels[myId]);
            return true;
        }else if (channels[opponentId].monster.hp <= 0) {
            sendOne("あなたの勝ちです", channels[myId]);
            return true;
        }
        return false;
    }

    // 攻撃後にクライアント全員に攻撃結果を表示 moveIdxはクライアントが選択した技の番号
    public void useMove(int myId, int opponentId, int moveIdx) {
        String myName = channels[myId].name;
        String opponentName = channels[opponentId].name;
        Move myMove = channels[myId].monster.moves[moveIdx];
        String partition = "------------\n";

        if (channels[myId].monster.moves[moveIdx].count <= 0) {
            int STRUGGLE_DAMAGE = new Random().nextInt(51);
            channels[opponentId].monster.hp -= STRUGGLE_DAMAGE;
            sendAll(partition +
                    myName + "は" +
                    myMove.name + "を使用できません\n" +
                    "代わりに悪あがきを繰り出した!\n" +
                    opponentName + "に" +
                    STRUGGLE_DAMAGE + "のダメージを与えた!" + partition);
            return;
        } else {
            String compatibility;
            double multiplier = myMove.calculateMultiplier(channels[myId].monster,
                    channels[opponentId].monster);
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
            sendAll(partition +
                    myName + "は" +
                    myMove.name + "を使用した!\n" +
                    compatibility + 
                    opponentName + "に" +
                    proccessedDamage + "のダメージを与えた!\n" + partition);
        }
    }

    public void showStats(Channel ch) {
        sendOne("-----------------\n", ch);
        sendOne("属性は " + ch.monster.getType(), ch);
        sendOne("hp値は " + ch.monster.health.getValue(), ch);
        sendOne("攻撃は " + ch.monster.attack.getValue(), ch);
        sendOne("防御は " + ch.monster.block.getValue(), ch);
        sendOne("特攻は" + ch.monster.contact.getValue(), ch);
        sendOne("特防は " + ch.monster.defense.getValue(), ch);
        sendOne("素早さは " + ch.monster.speed.getValue(), ch);
        if (ch.monster.sum < 91) {
            sendOne("まずまずな能力", ch);
        } else if (ch.monster.sum < 151) {
            sendOne("平均以上な能力", ch);
        } else if (ch.monster.sum < 151) {
            sendOne("相当優秀な能力", ch);
        } else {
            sendOne("素晴らしい能力!", ch);
        }
        sendOne("\n--------------------", ch);
    }

    // ターンを変える。スレッド間でターンの情報を同期するためにサーバに保存したchannels[]に対してターン情報onTurnを更新する。
    public synchronized void changeTurn(int myId, int opponentId) {
        if (channels[myId].onTurn) {
            channels[myId].onTurn = false;
            channels[opponentId].onTurn = true;
            return;
        } else if(channels[opponentId].onTurn) {
            channels[myId].onTurn = true;
            channels[opponentId].onTurn = false;
            return;
        }
    }

    public void showTurn(int myId, int opponentId) {
        if (!channels[myId].onTurn) {
           sendOne("--- " + channels[opponentId].name + "のターンです ---", channels[myId]); 
           sendOne("--- あなたのターンです ---\n Enterを押してください", channels[opponentId]);
        }
    }

    // ターンの更新 サーバーに保存した情報を取ってくる。
    public synchronized boolean getTurn(int id) {
        return channels[id].onTurn;
    }

    public void showCurrentHp(int myId, int opponentId) {
        sendAll("現在の" + channels[myId].monster.name + "のHPは " + 
            (channels[myId].monster.hp > 0 ? channels[myId].monster.hp : "0") + "です\n");
        sendAll("現在の" + channels[opponentId].monster.name + "のHPは " + 
            (channels[opponentId].monster.hp > 0 ? channels[opponentId].monster.hp : "0") + "です\n");
    }

    // csvファイルmoves.csvから技リストを読み込む
    // Channelごとに技リストを取得している。
    // -> サーバー側で技リスト取得、保存し、それをChannelに配布したほうがいいかも
    // OSによってパスの指定方法が異なるなら、パスをコマンドライン引数で指定できるように変更したほうがいいかも
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

    public synchronized void initializeTurn(int id, boolean onTurn) {
        channels[id].onTurn = onTurn;
    }
    
    public void clientDisconnect(int id){
        channels[id].endConnection();
        channels[id] = null;
    }

    public void logging(String str) {
        System.out.println(str);
    }
}
