package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    //TODO 最大クライアント数を4に拡張?(その際sendEmやisGameOverなどの関数も変更必要)
    static int MAX_CONNECTIONS = 2;
    static ServerThread serverThreads[] = new ServerThread[MAX_CONNECTIONS]; 
    // クライアント間で通信するためにサーバーに情報を保存(現段階では、具体的にはターンを表すnoTurnをリアルタイムで更新するために使う。)

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8000);
            while (true) {
                Socket socket = serverSocket.accept();
                serverThreads[0] = new ServerThread(socket, null, generateMonster(), true);
                if (serverThreads[0].opponent == null) {
                    socket = serverSocket.accept();
                    serverThreads[1] = new ServerThread(socket, serverThreads[0], generateMonster(), false);
                    serverThreads[0].opponent = serverThreads[1];
                    serverThreads[0].start();
                    serverThreads[1].start();
                }
            }
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    public static Monster generateMonster() {
        Move moveList[] = new Move[4];
        moveList[0] = new Move("fire", 50, 3, 1, true);
        moveList[1] = new Move("water", 50, 3, 2, true);
        moveList[2] = new Move("grass", 50, 3, 3, true);
        moveList[3] = new Move("light", 50, 3, 4, true);
        Monster monster = new Monster(moveList, "Grass");
        return monster;
    }

    // 一人のクライアントに対して送信
    public void sendHim(String msg, ServerThread st) {
        st.out.println(msg);
    }
    //二人のクライアントに対して送信(ブロードキャスト)
    public void sendEm(String msg, ServerThread st1, ServerThread st2) {
        sendHim(msg, st1);
        sendHim(msg, st2);
    }
    // クライアントが自分のターンになったた使える技の一覧を表示
    public void showMoveLineup(ServerThread st) {
        sendHim("You have 4 moves", st);
        for (int i = 0; i < st.monster.moveList.length; i++) {
            sendHim(st.monster.moveList[i].toString(), st);
        }
        sendHim("---Enter the number of the move you want to use---", st);
    }
    // ターンの終了ごとにクライアント全員にHP状況を表示
    public void showTheirHp(ServerThread st1, ServerThread st2) {
        sendEm("Now " + st1.monster.name + "'s health is " + st1.monster.hp, st1, st2);
        sendEm("Now " + st2.monster.name + "'s health is " + st2.monster.hp, st1, st2);
        sendEm("-----------------------------------------------------", st1, st2);
    }
    //　ゲームが終了(どちらかのhpが0)するならtrueを返す。攻撃後に毎回確認する。
    public boolean isGameover(ServerThread st1, ServerThread st2) {
        if (st1.monster.hp <= 0) {
            sendHim("You Lost", st1);
            sendHim("You Win", st2);
            return true;
        } else if (st2.monster.hp <= 0) {
            sendHim("You win", st1);
            sendHim("You Lost", st2);
            return true;
        }
        return false;
    }
    // 攻撃後にクライアント全員に攻撃結果を表示　moveIdxはクライアントが選択して技の番号
    public void showMoveResult(ServerThread atker, ServerThread opponent, int moveIdx) {
        int dmg = atker.monster.moveList[moveIdx].useMove(atker.monster, opponent.monster);
        opponent.monster.decreaseHP(dmg);
        sendEm(atker.monster.name + " used " + atker.monster.moveList[moveIdx].name + " and did " + dmg + " damage",
                atker, opponent);
    }
    // ゲーム開始時？にクライアントに自身のモンスター情報を表示
    public void showStats(ServerThread st) {
        sendHim("-----------------",st);
        sendHim("\nYour type is " + st.monster.type, st);
        sendHim("\nYour hp is " + st.monster.health, st);
        sendHim("\nYour attack is " + st.monster.attack, st);
        sendHim("\nYour block is " + st.monster.block, st);
        sendHim("\nYour contanct is " + st.monster.contact, st);
        sendHim("\nYour defense is " + st.monster.defense, st);
        sendHim("\nYour speed is " + st.monster.speed, st);
        if(st.monster.sum < 91){
            sendHim("まずまずな能力", st);
        }else if(st.monster.sum < 151){
            sendHim("平均以上な能力", st);
        }else if(st.monster.sum < 151){
            sendHim("相当優秀な能力", st);
        }else{
            sendHim("素晴らしい能力!", st);
        }
        sendHim("\n-----------------------", st);
    }
    // ターンを変える。スレッド間でターンの情報を同期するためにサーバに保存したserverThreads[]に対してターン情報onTurnを更新する。
    // その後、そのターン情報をスレッド自体にも更新。
    //　FIXME : クライアントが入力したモンスタの名前で検索しているので、それが同じ名前だと正しくターンを更新できない。
    public synchronized void changeTurn(ServerThread st1, ServerThread st2) {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (serverThreads[i] != null) {
                if (serverThreads[i].monster.name.equals(st1.monster.name)) {
                    if (serverThreads[i].onTurn) {
                        serverThreads[i].onTurn = false;
                        st1.onTurn = false;
                    }
                }
            }
            if (serverThreads[i] != null) {
                if (serverThreads[i].monster.name.equals(st2.monster.name)) {
                    if (!serverThreads[i].onTurn) {
                        serverThreads[i].onTurn = true;
                        st2.onTurn = true;
                    }
                }
            }
        }
    }
    // ターン情報を表示
    public void showTurn(ServerThread st1, ServerThread st2) {
        if (st1.onTurn) {
            sendEm("---It's " + st1.monster.name + "'s turn---", st1, st2);
        } else {
            sendEm("---It's " + st2.monster.name + "'s turn---", st1, st2);
        }
    }
    // ターンの更新 サーバーに保存した情報を取ってくる。
    public synchronized boolean isTurn(ServerThread st) {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            if (serverThreads[i] != null) {
                if (serverThreads[i].monster.name == st.monster.name) {
                    return serverThreads[i].onTurn;
                }
            }
        }
        return false;
    }

    public void showCurrentHp(ServerThread st1, ServerThread st2) {
        sendEm("Now " + st1.monster.name + "'s health is " + st1.monster.hp, st1, st2);
        sendEm("Now " + st2.monster.name + "'s health is " + st2.monster.hp, st1, st2);
    }
}
