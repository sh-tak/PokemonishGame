package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import server.bin.Monster;
import server.bin.Move;

public class Channel extends Thread {
    Server server;
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    public int id;
    public String name = "";
    public String opponentName = "";
    public int opponentId = -1;
    public Monster monster;
    public boolean onTurn;

    private int state;
    private static final int WAIT_FOR_NAME_INPUT = 0;

    Channel(Socket socket, Server server, int id) {
        this.server = server;
        this.socket = socket;
        this.id = id;
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(
                    socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("入出力接続エラー");
        }
        this.start();
    }

    public void run() {
        send("サーバーに接続しました\n" +
                "idは" + id + "です\n" +
                "モンスターの名前を入力してください");
        try {
            nameAck();

            // モンスターをランダムに生成
            Move[] myMoves = server.chooseFourMoves(
                    server.fetchMovesFromCsv("./server/bin/moves.csv"));
            monster = new Monster(myMoves, name);
            server.showStats(id);
            // 対戦相手を見つける
            while (opponentName.equals("")) {
                opponentName = server.findOpponent(id);
            }
            opponentId = server.getIdByName(opponentName);
            send("対戦相手が見つかりました\n" +
                    "対戦相手は" + opponentName + "です");

            // 対戦相手に対戦を開始する
            send(Integer.toString(server.isFirstTurn(id)));
            inBattle();
            close();

        } catch ( IOException e) {
            logging(e);
        }
    }

    public void send(String s) {
        out.println(s);
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void logging(Object s) {
        System.out.println(s);
    }

    public void nameAck() throws IOException {
        while (true) { // クライアントの状態が変わるまで
            String clientState = receive();
            if (!clientState.equals("0")) {
                break;
            }
            String name = receive();
            // サーバに名前の重複を確認しに行き結果を送信
            if (server.isDuplicate(name)) {
                send("1");
            } else {
                send("0");
                server.setName(name, this.id); // サーバに名前を登録
            }
        }
    }

    public void inBattle() throws IOException {
        while (true) { // クライアントの状態が変わるまで
            String clientState = receive();
            if (clientState.equals("3")) {// 攻撃側
                server.showMoveLineup(id);
                int moveIdx = Integer.parseInt(receive());// 技番号の受け取り
                server.useMove(id, opponentId, moveIdx);// 技の結果を表示
                server.showCurrentHp(id, opponentId);// HPを表示
                if (server.isGameover(id, opponentId)) {
                    send("1");
                    break;
                }else {
                    send("0");
                }
            } else if (clientState.equals("4")) {// 防御側
                // 攻撃側の呼び出しで技の結果, HPを表示
                receive(); // 技の結果の受け取り確認
                if (server.isGameover(id, opponentId)) {
                    send("1");
                    break;
                }else {
                    send("0");
                }
            }
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
            server.channels[id] = null;
        } catch (IOException e) {
            logging(e);
        }
    }
}
