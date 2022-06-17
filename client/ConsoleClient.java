package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleClient {
    public static void main(String[] args) {
        try {
            Client client = new Client();
            client = new Client();
            // コンソールなので標準入力そ引数として渡す。(GUIなら別)
            // GuiClientではテキストフィールドを引数として渡せば良さそう?
            client.connect(new BufferedReader(new InputStreamReader(System.in)));
            client.receiveAndLog(3); // 接続確認応答を受け取る
            client.nameInputAndAck(); //モンスターの名前を入力
            client.MonsterTypeInputAndSend(); // モンスターの属性を入力 
            client.logging("あなたのモンスターを生成します");
            client.receiveAndLog(10); // 生成したモンスターのデータを受け取る
            client.logging("対戦相手を探しています");
            client.receiveAndLog(2); // 対戦相手を表示
            client.logging("対戦相手を待っています");
            client.receiveAndLog(1); // 対戦開始
            client.initTurn(client.receive()); // 先攻か後攻かを受け取ってターンを初期化
            client.inBattle();
            client.showBattleResult();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}