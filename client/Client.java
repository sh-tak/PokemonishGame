package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final int PORT = 8080;
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    BufferedReader stdIn = null;

    private static final int NAME_INPUTTING = 0;
    private static final int WAIT_FOR_NAME_ACK = 1;
    private static final int WAIT_FOR_OPPONENT = 2;
    private static final int MY_TURN = 3;
    private static final int OPPONENT_TURN = 4;
    private static final int WIN = 5;
    private static final int LOSE = 6;
    private int state = NAME_INPUTTING;

    public void connect() {
        try {
            socket = new Socket("localhost", PORT);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            stdIn = new BufferedReader(
                    new InputStreamReader(System.in));
            state = NAME_INPUTTING;
        } catch (IOException e) {
            logging("接続失敗");
        }
    }

   

    public void logging(Object s) {
        System.out.println(s);
    }

    public String read() throws IOException {
        return stdIn.readLine();
    }

    public String recieve() throws IOException {
        return in.readLine();
    }

    public void recieveAndLog(int line) throws IOException {
        for(int i = 0; i < line; i++) {
            logging(recieve());
        }
    }

    public void send(String s) {
        out.println(s);
    }

     private void nameInputAndSend() throws IOException {
        String name =  read();
        while (name == null || name.equals("")) {
            logging("名前は一文字以上で入力してください");
            name = read();
        }
        send(name);
        state = WAIT_FOR_NAME_ACK;
    }

    private void nameAck() throws NumberFormatException, IOException {
        int ack = Integer.parseInt(recieve());
        if (ack == 1) {
            logging("名前が重複しています");
            logging("別の名前にして下さい");
            state = NAME_INPUTTING;
        } else {
            logging("名前を登録しました");
            state = WAIT_FOR_OPPONENT;
            send(Integer.toString(state)); //最後に状態遷移を確認させて終了
        }
    }

    public void nameInputAndAck() throws IOException {
        while (state == NAME_INPUTTING) {
            send(Integer.toString(state));// 先に状態を送る 
            nameInputAndSend();
            nameAck(); // サーバからの確認待ち
        }
    }

    public boolean isValidInput(String str) {
        if(str.equals("0") || str.equals("1") || str.equals("2") || str.equals("3")) {
            return true;
        }
        return false;
    }

    public void moveIndexInputAndSend() throws IOException{
        String moveIndex = read();
        while (!isValidInput(moveIndex)) {
            logging("0~3の数字を入力してください");
            moveIndex = read();
        }
        send(moveIndex);
    }

    public void initTurn(String turn) throws IOException {
        if (turn.equals("1")) {
            logging("あなたは先攻です");
            state = MY_TURN;
        } else {
            logging("あなたは後攻です");
            state = OPPONENT_TURN;
        }
    }

    public void inBattle() throws IOException {
        while(state != WIN && state != LOSE) {
            if(state == MY_TURN){
                logging("あなたのターンです");
                send(Integer.toString(state));// 先に状態を送る
                recieveAndLog(10);// 技の表示
                moveIndexInputAndSend();
                recieveAndLog(5+3);// 技の結果とHP表示
                if(recieve().equals("1")){ //ゲーム終了判定
                    state = WIN;
                    break;
                }else {
                    state = OPPONENT_TURN;
                }

            }else if(state == OPPONENT_TURN){
                logging("相手のターンです");
                send(Integer.toString(state));// 先に状態を送る
                recieveAndLog(5+3);// 技の結果とHP表示
                send("0"); // 技の結果の受け取りを報告
                if(recieve().equals("1")){ //ゲーム終了判定
                    state = LOSE;
                    break;
                }else{
                    state = MY_TURN;
                }
            }
        }
    }

    public void showBattleResult() throws IOException {
        if(state == WIN){
            logging("あなたの勝ちです");
        }else if(state == LOSE){
            logging("あなたの負けです");
        }
    }
}