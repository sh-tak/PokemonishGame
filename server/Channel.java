package server;

import java.io.BufferedReader;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import server.bin.Monster;
import server.bin.Move;

public class Channel extends Thread {
    Server server;
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    int id;
    String name = "";
    String opponentName = "";
    int opponentId = -1;
    Monster monster;
    boolean onTurn;
    int durationMs = 500;

    Channel(Socket socket, Server server, int id){
        this.server = server;
        this.socket = socket;
        this.id = id;
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(
                socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("入出力エラー");
        }
        this.start();
    }

    public void run() {
        try {
            // 状態1: 名前入力(名前が空or重複でなくなるまで入力させる)
            // この状態でモンスターの生成を行う
            write("--- サーバーに接続されました ---");
            write("--- あたたのidは" + id + "です ---");
            write("--- モンスターの名前を入力してください ---");
            while(true){
                String  name = in.readLine();
                if(name.equals("")){
                    write("--- 1文字以上の名前を入力してください ---\n");
                    continue;
                }else if(server.existSameName(name)){
                    write("--- 同じ名前のモンスターがいます。名前を変えてください ---\n");
                    continue;
                }else{
                    server.channels[id].name = name;
                    break;
                }
            }

            Move[] myMoves = server.chooseFourMoves(
                server.fetchMovesFromCsv("./server/bin/moves.csv"));
            this.monster = server.getMonster(myMoves, name);
            write("--- 技をランダムに選択しました ---");
            write("--- あなたのモンスターを生成しました ---");
            Thread.sleep(durationMs);
            server.showStats(this);
            Thread.sleep(durationMs*2);

            // 状態2: 対戦相手待ち
            write("--- 対戦相手を探しています ---\n");
            Thread.sleep(durationMs*2); 
            while(opponentName.equals("")){
                opponentName = server.findOpponent(id);
            }
            opponentId = server.getIdByName(opponentName);
            write("--- 対戦相手が見つかりました ---");
            write("--- 対戦相手は" + opponentName + "です ---\n");
            Thread.sleep(durationMs*2);

            // 状態3: 対戦開始(開始ターンはモンスターの素早さに依存)
            // 素早さが同じなら接続順でターンを決める
            write("--- 対戦を開始します ---\n");
            write("--- あなたのモンスターの素早さは" + 
                Integer.toString(monster.speed.getValue()) + "です ---\n");
            if(monster.speed.getValue() >
                    server.channels[opponentId].monster.speed.getValue()){
                onTurn = true;
            }else if(monster.speed.getValue() < 
                    server.channels[opponentId].monster.speed.getValue()){
                onTurn = false;
            }else{
                if(id < opponentId){
                    onTurn = true;
                }else{
                    onTurn = false;
                }
            }
            Thread.sleep(durationMs);
            server.initializeTurn(id, onTurn);
            if(onTurn){
                write("--- あなたのほうが素早さが速いので先攻です ---\n");
            }else{
                write("--- あなたのほうが素早さが遅いので後攻です ---\n");
            }

            server.showTurn(id, opponentId);
            // 状態4: 対戦中
            List<String> bin = new ArrayList<String>();
            while (true) {
                while (true) {//状態4.1 相手のターン
                    bin.add(in.readLine());
                    onTurn = server.getTurn(id);
                    if(onTurn){
                        break;
                    }
                }//状態4.2: 自身のターン
                bin.clear();
                server.showMoveLineup(this);
                String moveIndex;
                while(!server.isValidNum(moveIndex = in.readLine())){
                    write("--- 0~3の数字を入力してください ---\n");
                }
                server.useMove(id, opponentId, Integer.parseInt(moveIndex));
                server.showCurrentHp(id, opponentId);
                if(server.isGameover(id, opponentId)){// 状態5: 対戦終了
                    server.clientDisconnect(id);
                    server.clientDisconnect(opponentId);
                    break;
                }
                server.changeTurn(id, opponentId);
                server.showTurn(id,opponentId);
                }
        return;
        } catch (IOException | InterruptedException e) {
            logging(e);
        }
    }

    private static void logging(Object s) {
        System.out.println(s);
    }

    synchronized void write(String s){
        out.write(s +"\n");
        out.flush(); 
    }

    public void endConnection(){
        try {
            write("通信を終了します");
            in.close();
            out.close();
            socket.close();
            socket = null;
        } catch (IOException e) {
            logging("通信終了エラー");
        }
    }
}
