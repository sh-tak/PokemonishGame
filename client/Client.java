package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
//　クライアントの実装を簡潔にするためにスレッドLinkupを使う。
public class Client {

    public static void main(String[] args) {
        try {
            BufferedReader userin = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter userout = new PrintWriter(System.out, true);
            Socket socket = new Socket(InetAddress.getLocalHost(), 8000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Linkup usr2srv = new Linkup(userin, out);
            Linkup srv2usr = new Linkup(in, userout);
            usr2srv.start();
            srv2usr.start();
        } catch (IOException e) {
            System.out.println("サーバーが起動していないか、接続できませんでした。");
        }
    }
    public static void logging(String s){
        System.out.println(s);
    }
}
