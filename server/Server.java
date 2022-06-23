package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import server.bin.Monster;
import server.bin.Move;

public class Server extends Thread {
    static int MAX_CONNECTIONS = 128;
    private static final int PORT = 8080;
    ServerSocket serverSocket;
    ClientInfo clientsInfo[] = new ClientInfo[MAX_CONNECTIONS]; // クライアント間で通信するためのメンバ変数
    Move[] importedMoves;
    // 対戦待ちの人のリスト
    List<Integer> waitingPlayers = new ArrayList<Integer>();// 対戦待ちリスト
    // 登録最中の人のリスト、サーバに名前(アカウント)が登録されるのはパスワードを設定してからなので
    // パスワード設定中に他の人が同じ名前で登録してしまわないようにこのリストを使う
    List<String> registeList = new ArrayList<String>();

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
            importedMoves = fetchMovesFromCsv("./server/bin/moves.csv");
            // debug用のin server.debug関数を定義するとサーバー側で標準入力して確認に使える
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                // 接続待ちの時はsocket.acceptの部分でブロッキングされるのでdebug関数が呼び出されるのは
                // クライアントが接続された直後
                if(in.ready()){
                    String input = in.readLine();
                    debug(input);
                }
                Socket socket = serverSocket.accept();
                new Channel(socket, this);
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

    private void debug(String input) {
        // サーバー側でrenewmoveと打つと技を更新する。
        //　サーバーを起動した後でも技を追加削除できる
        if(input.equals("renewmove")){
            fetchMovesFromCsv("./server/bin/moves.csv");
        }
    }

    /*
     * 確認関連
     */
    // 名前が重複していたらtrueを返す
    public boolean isDuplicationName(String name) {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (clientsInfo[i] != null && clientsInfo[i].name.equals(name)) {
                return true;
            }
        }
        for(int i = 0; i < registeList.size(); i++){
            if(registeList.get(i).equals(name)){
                return true;
            }
        }
        return false;
    }

    public boolean isGameover(int myId, int opponentId) {
        if (clientsInfo[myId].monster.hp <= 0) {
            return true;
        } else if (clientsInfo[opponentId].monster.hp <= 0) {
            return true;
        }
        return false;
    }

    // 登録した名前が存在してパスワードが正しいかったらtrueを返す
    public boolean isValidAccount(String name, String password) throws NoSuchAlgorithmException {
        byte[] hashedPassword = ClientInfo.getHashedPass(password);
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (clientsInfo[i] != null && 
                clientsInfo[i].name.equals(name) &&
                !clientsInfo[i].online) {
                byte[] storedPassword = clientsInfo[i].getPassward();
                if(storedPassword.length != hashedPassword.length){
                    return false;
                }
                for(int j = 0; j < hashedPassword.length; j++){
                    if(storedPassword[j] != hashedPassword[j]){
                        return false;   
                    }
                }
                return true;
            }
        }
        return false;
    }

    // speedの値に応じて自分が先攻ならtrue, 後攻ならfalseを返す
    public boolean isFirstTurn(int myId, int opponentId) {
        int mySpeed = clientsInfo[myId].monster.speed.getValue();
        int opponentSpeed = clientsInfo[opponentId].monster.speed.getValue();
        if (mySpeed > opponentSpeed) {
            return true;
        } else if (mySpeed < opponentSpeed) {
            return false;
        } else if (myId < opponentId) { //mySpeed=opponentSpeedのとき
            return true;
        } else {
            return false;
        }
    }

    public String getNameById(int id) {
        return clientsInfo[id].name;
    }

    public int getIdByName(String name) {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (clientsInfo[i] != null && clientsInfo[i].name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /*
     * 登録関連
     */
    // 新しいアカウントを登録し、アカウントのidを返す
    public int addAccount(String name, String plainPassword, String monsterType, PrintWriter out) throws NoSuchAlgorithmException {
        int i;
        for (i = 0; i < MAX_CONNECTIONS; i++) {
            if (clientsInfo[i] == null) {
                // 技とモンスターはランダムに生成
                Move[] moves = chooseFourMoves(importedMoves);
                clientsInfo[i] = new ClientInfo(
                        i, name, plainPassword, new Monster(moves, monsterType), out);
                break;
            }
        }
        return i;
    }

    // csvファイルmoves.csvから全ての技リストを読み込む(サーバー起動時に呼び出し)
    public Move[] fetchMovesFromCsv(String filename) {
        int i = 0;
        try {
            ArrayList<Move> moves = new ArrayList<Move>();
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] moveInfo = line.split(",");
                    if (moveInfo.length > 5) {
                        moves.add(new Move(
                                moveInfo[0],
                                Integer.parseInt(moveInfo[1]),
                                Integer.parseInt(moveInfo[2]),
                                Integer.parseInt(moveInfo[3]),
                                moveInfo[4].equals("1") ? true : false,
                                Integer.parseInt(moveInfo[5])));    //命中率追加
                        i++;
                    }
                }
            }
            logging(i + "個の技を読み込みました");
            return moves.toArray(new Move[moves.size()]);
        } catch (IOException e) {
            System.out.println("for debug" +
                    "moves.csvのパスを確認してください\n" +
                    "(MacOS12.0.01で動作確認済み)\n" +
                    "パスはChannel.java内で指定しています\n");
        }
        return null;
    }

    // 4つのランダムに選んだ技を返す(モンスター生成時に呼び出し)
    public Move[] chooseFourMoves(Move[] moves) {
        // 実例: moves.length = 5 のとき[0,1,2,3,4]をシャッフルして[2,3,1,4,0]
        // のようになる。このときmoves[2], moves[3], moves[1], moves[4]のリストを返す。
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < moves.length; i++) {
            list.add(i);
        }
        for (int i = 0; i < 30; i++) { 
            Collections.shuffle(list);
        }
        Move returnMove[] = new Move[4];
        for (int i = 0; i < 4; i++) {
            returnMove[i] = moves[list.get(i)];
        }
        return returnMove;
    }

    // 相手を探して、相手のidを返す
    public int findOpponent(int id) throws InterruptedException {
        waitingPlayers.add(id);
        while (true) {
            Thread.sleep(100);
            // 自分の前に対戦待ちが1人以下かつ、自分の後ろか前に対戦待ちがいたらその人と対戦
            if (waitingPlayers.indexOf(id) < 2 && waitingPlayers.size() > 1) {
                return waitingPlayers.get(waitingPlayers.indexOf(id) == 1 ? 0 : 1);
            }
        }
    }

    // 対戦開始前に対戦開始待ちリストから削除する
    public void removeWaitingPlayer(int id) {
        if(waitingPlayers.indexOf(id) == 0) {
            waitingPlayers.remove(1);
            waitingPlayers.remove(0);
        }
    }

    // ステータスを選んで3上げる
    public void levelUp(int id, int status){
        if(status == 1){
            int val = clientsInfo[id].monster.health.getValue();
            clientsInfo[id].monster.health.setValue(val + 3);
        }else if(status == 2){
            int val = clientsInfo[id].monster.attack.getValue();
            clientsInfo[id].monster.attack.setValue(val + 3);
        }else if(status == 3){
            int val = clientsInfo[id].monster.block.getValue();
            clientsInfo[id].monster.block.setValue(val + 3);
        }else if(status == 4){
            int val = clientsInfo[id].monster.contact.getValue();
            clientsInfo[id].monster.contact.setValue(val + 3);
        }else if(status == 5){
            int val = clientsInfo[id].monster.defense.getValue();
            clientsInfo[id].monster.defense.setValue(val + 3);
        }else if(status == 6){
            int val = clientsInfo[id].monster.speed.getValue();
            clientsInfo[id].monster.speed.setValue(val + 3);
        }
    }

    /*
     * バトル関連
     */
    public void showStats(int id) {
        clientsInfo[id].send(clientsInfo[id].monster.toString());
    }

    public void showMoveLineup(int id) {
        String str = "技は4種類あります\n";
        for (int i = 1; i < 5; i++) {
            str += "技" + i + ": " + clientsInfo[id].monster.moves[i-1].toString() + "\n";
        }
        str += "使いたい技を数字で入力してください";
        clientsInfo[id].send(str);
    }

    // 攻撃後にクライアント全員に攻撃結果を表示 moveIdxはクライアントが選択した技の番号
    public void useMove(int myId, int opponentId, int moveIdx) {
        String myName = clientsInfo[myId].name;
        String opponentName = clientsInfo[opponentId].name;
        Move myMove = clientsInfo[myId].monster.moves[moveIdx];
        String partition = "--------------";
        String result;

        // 選んだ技の使用可能回数が0なら悪あがきを繰り出す
        if (clientsInfo[myId].monster.moves[moveIdx].count <= 0) {
            int damage = clientsInfo[myId].monster.moves[moveIdx].getStruggleGamage();
            clientsInfo[opponentId].monster.hp -= damage;
            clientsInfo[myId].monster.hp -= damage / 2;
            result = partition + "\n" +
                    myName + "は" +
                    myMove.name + "を使用できません\n" +
                    "代わりに悪あがきを繰り出した!\n" +
                    opponentName + "に" +
                    damage + "のダメージを与えたが自分も" + damage / 2 + "のダメージを受けた\n" +
                    partition;
            clientsInfo[myId].send(result);
            clientsInfo[opponentId].send(result);
            return;
        } else if((100-myMove.hitRate) > Math.random()*100){            //技が命中しなかった場合
            clientsInfo[myId].monster.moves[moveIdx].count--;           //命中しなくてもPP減らす
            result = partition + "\n" +
                    myName + "は" +
                    myMove.name + "を使用した!\n" +
                    "しかし" +
                    opponentName + "には当たらなかった!\n" +partition + "\n";
            clientsInfo[myId].send(result);
            clientsInfo[opponentId].send(result);
        } else {
            String compatibility;
            double multiplier = myMove.calculateMultiplier(clientsInfo[myId].monster,
                    clientsInfo[opponentId].monster);
            if (multiplier < 1.0) {
                compatibility = "効果はいまいちだ\n";
            } else if (multiplier < 1.5) {
                compatibility = "効果はまあまあだ\n";
            } else {
                compatibility = "効果は抜群だ\n";
            }

            int proccessedDamage = (int) (clientsInfo[myId].monster.moves[moveIdx].damage * multiplier);
            clientsInfo[myId].monster.moves[moveIdx].count--;
            clientsInfo[opponentId].monster.hp -= proccessedDamage;
            result = partition + "\n" +
                    myName + "は" +
                    myMove.name + "を使用した!\n" +
                    compatibility +
                    opponentName + "に" +
                    proccessedDamage + "のダメージを与えた!\n" + partition;
            clientsInfo[myId].send(result);
            clientsInfo[opponentId].send(result);
        }
    }

    public void showCurrentHp(int myId, int opponentId) {
        int myHp = clientsInfo[myId].monster.hp;
        int opponentHp = clientsInfo[opponentId].monster.hp;
        String str = "現在の" + clientsInfo[myId].name + "のHPは " +
                (myHp > 0 ? myHp : "0") + "です\n" +
                "現在の" + clientsInfo[opponentId].name + "のHPは " +
                (opponentHp > 0 ? opponentHp : "0") + "です\n" +
                "-------------";
        clientsInfo[myId].send(str);
        clientsInfo[opponentId].send(str);
    }

    public void logging(String str) {
        System.out.println(str);
    }
}
