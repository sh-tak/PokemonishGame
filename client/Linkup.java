package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
// クライアントが入力待ちになると、ブロッキングされてそれ以外の作業ができなくなるので。スレッドを使う
// 入力待ちでも他の作業ができるようになる. ブロッキングを考えてクライアント側を実装するとクライアント側が自身のターン状態
// を管理しないといけなくなり、複雑になるのでスレッドの実装でブロッキングを考えないようにした。
public class Linkup extends Thread {
    BufferedReader in;
    PrintWriter out;

    public Linkup(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    // 接続が切れるまでずっと入力を待ち続ける。
    public void run() {
        try {
            String indat;
            while ((indat = in.readLine()) != null) {
                this.out.println(indat);
            }
        } catch (IOException e) {
            System.out.println("Error");
        }
    }
}
