package com.xcoder;


import java.net.Socket;

/**
 * 客户端
 */
public class Boot {
    private static final String HOST = "127.0.0.1";
    private static final int MASTER_PORT = 8080;

    public void run() throws Exception {
        try {
            Socket socket = new Socket(HOST, MASTER_PORT);
            new ClientMessageHandler(socket).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        new Boot().run();
    }

}