package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.event.*;
import javax.swing.*;



public class GraphicalClient {
    final static int MY_TURN = 3;
    final static int OPPONENT_TURN = 4;
    private static final int WIN = 5;
    private static final int LOSE = 6;
    
    private static ClientUI gClient;
    private static Client cClient;

    private static void logging(String str) {

    }

    public static void main(String[] args) {
        // initialize gui
        gClient = new ClientUI();
        gClient.setStatus("HP");
        String[] testWaza = {"waza1", "waza2", "waza3", "waza4"};
        gClient.setWaza(testWaza);
        gClient.setImage(-1, "client/image/background.png");
        gClient.setImage(0, "client/image/a.png");
        gClient.setHP(0, 160, 250);
        gClient.setButtonAction(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog
                (null, gClient.getSelectedWaza());
            }
        });
        gClient.clearLog();
        
        gClient.setHP(1, 10, 140);
        gClient.setImage(1, "client/image/a.png");

        try{
            // ユーザー入力を標準入力に設定。サーバーと接続
            cClient = new Client(new BufferedReader(
                    new InputStreamReader(System.in)));
            cClient.receiveAndLog(1); // 接続確認応答を受け取る
            cClient.logging("新しく始めますか?(Yes/No)");
            boolean isNewAccount = cClient.yesNoInput();
            cClient.send(isNewAccount ? "new" : "login");
            if (!isNewAccount) {// ログインして始める
                while (true) {
                    cClient.logging("登録済みの名前を入力してください");
                    String inname = cClient.input("name");
                    cClient.logging("パスワードを入力してください");
                    String inpass = cClient.input("pass");
                    cClient.send(inname);
                    cClient.send(inpass);
                    String ack = cClient.receive();
                    if (ack.equals("correct")) {
                        cClient.logging("ログインしました");
                        break;
                    } else {
                        cClient.logging("名前またはパスワードが間違っています\n" +
                                "もう一度入力しますか?(Yes) 新規登録しますか?(No)");
                        if (cClient.yesNoInput()) {
                            cClient.send("onemore");
                            continue;
                        } else {
                            isNewAccount = true;
                            cClient.send("quit");
                            break;
                        }
                    }
                }
            }
            if (isNewAccount) {// 新規登録
                while (true) {
                    String name;
                    cClient.logging("新規の名前を入力してください");
                    name = cClient.input("name");
                    cClient.send(name);
                    if (cClient.receive().equals("duplicate")) {
                        cClient.logging("その名前は使用できません");
                    } else {
                        break;
                    }
                }
                while (true) {
                    String password1, password2;
                    cClient.logging("パスワードを入力してください");
                    password1 = cClient.input("pass");
                    cClient.logging("パスワードを再度入力してください");
                    password2 = cClient.input("pass");
                    if (password1.equals(password2)) {
                        cClient.send(password1);
                        cClient.logging("登録しました");
                        break;
                    } else {
                        cClient.logging("パスワードが一致しません\n" +
                                "1から入力し直してください");
                    }
                }
                cClient.logging("モンスターの属性を選択してください\n" +
                        "1: 火 2: 水 3: 草  4: 光  5: 闇");
                String monsterType = cClient.input("type");
                cClient.send(monsterType);
            }

            cClient.receiveAndLog(10);// モンスター情報を受け取る
            cClient.logging("対戦相手を探しています");
            cClient.receiveAndLog(2); // 対戦相手を表示

            // 対戦
            if (cClient.receive().equals("first")) {
                cClient.logging("あなたが先攻です");
                cClient.setState(MY_TURN);
            } else {// receive() == "second"
                cClient.logging("あなたが後攻です");
                cClient.setState(OPPONENT_TURN);
            }

            // 対戦開始
            cClient.logging("対戦を開始します");
            while (cClient.getState() != WIN && cClient.getState() != LOSE) {
                if (cClient.getState() == MY_TURN) {
                    String moveIndex;
                    cClient.logging("あなたのターンです");
                    cClient.send(Integer.toString(cClient.getState()));// 先に状態を送る
                    cClient.receiveAndLog(10);// 技の表示
                    moveIndex = cClient.input("move");
                    cClient.send(Integer.toString(Integer.parseInt(moveIndex) - 1));
                    cClient.receiveAndLog(5 + 3);// 技の結果とHP表示
                    if (cClient.receive().equals("gameisover")) { // ゲーム終了判定
                        cClient.setState(WIN);
                        break;
                    } else {
                        cClient.setState(OPPONENT_TURN);
                    }
                } else if (cClient.getState() == OPPONENT_TURN) {
                    cClient.logging("相手のターンです");
                    cClient.send(Integer.toString(cClient.getState()));// 先に状態を送る
                    cClient.receiveAndLog(5 + 3);// 技の結果とHP表示
                    cClient.send("resultreceived"); // 技の結果の受け取りを報告
                    if (cClient.receive().equals("gameisover")) { // ゲーム終了判定
                        cClient.setState(LOSE);
                        break;
                    } else {
                        cClient.setState(MY_TURN);
                    }
                }
            }
            // 対戦結果の表示
            if (cClient.getState() == WIN) {
                cClient.logging("あなたの勝ちです");
            } else if (cClient.getState() == LOSE) {
                cClient.logging("あなたの負けです");
            }
            if(cClient.getState() == LOSE) {
                cClient.send("LOSE");
            }else if(cClient.getState() == WIN){
                cClient.send("WIN");
                cClient.receiveAndLog(10);
                cClient.logging("対戦に勝利したのでモンスターのステータスを1つ選んで強化することができます");
                cClient.logging("強化するステータスを選んでください\n1:hp 2:攻撃 3: 防御　4:特攻 5:特防 6:素早さ");
                String status = cClient.input("status");
                cClient.send(status);
                cClient.receiveAndLog(1 + 10);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}