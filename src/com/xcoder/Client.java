package com.xcoder;


import java.io.*;
import java.net.Socket;

/**
 * Netty异步通信框架客户端
 */
public class Client {
    private static final String host = "localhost";
    private static final int port = 8080;


    public void run() throws Exception {
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("query time order");
            System.out.println("sending order to server success");
            String resp = in.readLine();
            System.out.println("time is " + resp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }

    }


    public static void main(String[] args) throws Exception {
        new Client().run();
    }


}