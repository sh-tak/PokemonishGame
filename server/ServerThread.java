package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    Object opponent;
    public Monster monster;
    boolean onTurn;

    // 同時にクライアントに対応するためにServerThreadがクライアントに対応する。
    // それぞれのサーバースレッドは敵のスレッド、自身のモンスター
    public ServerThread(Socket socket, ServerThread opponent, Monster monster, boolean onTurn) throws IOException {
        this.socket = socket;
        this.monster = monster;
        this.opponent = opponent;
        this.onTurn = onTurn;
    }

    public void run() {
        try {
            Server server = new Server();
            ServerThread opponent = (ServerThread) this.opponent;
            // Create input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String name = in.readLine();
            this.monster.name = name;
            logging("新しいクライアントが接続されました");
            String s; // store message from client
            server.sendHim("\n---あなたはサーバーに接続されました---", this);
            server.sendHim("\nあなたの敵は " + this.monster.name, opponent);
            server.sendHim("\n--- ゲームが開始します ---", this);
            while (true) {
                while (true) {
                        server.isTurn(this, opponent);
                        if(this.onTurn) break;
                    }
                    server.showTurn(this, opponent);
                    server.showMoveLineup(this);
                    s = in.readLine();
                    server.showMoveResult(this, opponent, Integer.parseInt(s));
                    if (server.isGameover(this, opponent)) {
                        break;
                    }
                    server.showCurrentHp(this, opponent);
                    server.changeTurn(this, opponent);
                    server.isTurn(this, opponent);
                    continue;
                }
        } catch (IOException e) {
            logging("Error");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logging("Error");
            }
            logging("Client  disconnected");
        }
    }

    private static void logging(String s) {
        System.out.println(s);
    }
}
