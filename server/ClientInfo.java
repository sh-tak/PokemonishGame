package server;

import java.io.PrintWriter;
import server.bin.Monster;

public class ClientInfo{
    public int id;
    public String name;
    private String hashedPassword;
    public Monster monster;
    private PrintWriter out;
    boolean online;

    ClientInfo(int id, String name, String hashedPassword, Monster monster, PrintWriter out) {
        this.id = id;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.monster = monster;
        this.out = out;
        this.online = true; //ログイン判定で使うかも
    }

    public String getPassward() {
        return hashedPassword;
    }

    // ログインした時に呼び出す
    public void login(PrintWriter out) {
        this.online = true;
        this.out = out;
    } 
    
    // 対戦(通信)終了時に呼び出す
    public void logout(){
        out = null;
        monster.resetHp();
        for(int i = 0; i < monster.moves.length; i++){
            monster.moves[i].reset();
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
