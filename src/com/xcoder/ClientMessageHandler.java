package com.xcoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by booly on 2016/5/26.
 */
public class ClientMessageHandler {

    private Socket Socket;
    private InputStream In;
    private OutputStream Out;
    private BufferedReader BReader;
    private long ClientID;
    private String WorkingDir;
    /**
     * Client用于缓存消息的缓冲区
     */
    private byte Msg[] = new byte[1024];


    public ClientMessageHandler(Socket socket) {
        try {
            Socket = socket;
            In = Socket.getInputStream();
            Out = Socket.getOutputStream();
            BReader = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            printMenu();
            clientRegister();
            while (true) {
                System.out.print("cmd>");
                String cmd = BReader.readLine();
                judgeCmd(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.closeStream(In, Out);
            Util.closeSocket(Socket);
        }
    }


    public void judgeCmd(String cmd) throws Exception {
        if (cmd.equals("pwd")) {
            if (WorkingDir == null) {
                getWorkingDir();
            } else {
                System.out.println("Working dir : " + WorkingDir);
            }
        } else if (cmd.contains("cd")) {
            String d[] = cmd.split(" ");
            if (d.length >= 2) {
                String dir = d[1];
                updateWorkingDir(dir);
            }
        } else if (cmd.equals("ls")) {

        } else if (cmd.contains("mkdir")) {
            if (cmd.split(" ").length >= 2) {
                String dir = cmd.split(" ")[1];
                makeDir(dir);
            }
        } else if (cmd.contains("create")) {

        } else if (cmd.contains("rm")) {

        } else if (cmd.contains("stat")) {

        } else if (cmd.equals("exit")) {
            System.exit(0);
        } else {
            System.out.println("wrong command");
        }
    }


    private void getWorkingDir() {
        byte buf[] = new byte[10];
        buf[0] = MSG.HEAD_CLIENT;
        buf[1] = MSG.CLIENT_QUERY_PWD;
        Util.getBytes(ClientID, buf, 2, 2 + 8);
        try {
            Out.write(buf, 0, buf.length);
            Out.flush();
            //等待服务器返回
            int len = In.read(Msg, 0, Msg.length);
            if (len == -1) {
                return;
            }
            WorkingDir = new String(Msg, 2, len - 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateWorkingDir(String dir) throws Exception {
        /**
         * 发送到消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) TargetDir(variable)]
         * 返回消息格式：
         * [Head(1B) Status(1B) NewWorkingDir(variable)|errorMessage]
         * */
        byte buf[] = new byte[2 + 8 + dir.length()];
        buf[0] = MSG.HEAD_CLIENT;
        buf[1] = MSG.CLIENT_UPDATE_PWD;
        Util.getBytes(ClientID, buf, 2, 2 + 8);
        for (int i = 0; i < dir.length(); i++) {
            buf[i + 10] = (byte) dir.charAt(i);
        }
        Out.write(buf);
        int len = In.read(Msg, 0, Msg.length);
        if (len >= 2) {
            byte status = Msg[1];
            switch (status) {
                case MSG.MASTER_ACK_OK:
                    WorkingDir = new String(Msg, 2, len - 2);
                    break;
                case MSG.MASTER_ACK_FAIL:
                    System.out.println("error:" + new String(Msg, 2, len - 2));
                    break;
            }

        }
    }


    public void clientRegister() {
        try {
            // 向master发送消息表明自己是client
            byte head[] = {MSG.HEAD_CLIENT, MSG.CLIENT_REGISTER};
            Out.write(head);
            Out.flush();
            int len = In.read(Msg, 0, Msg.length);
            // TODO
            System.out.println("msg length : " + len);
            if (len >= 2) {
                byte status = Msg[1];
                switch (status) {
                    case MSG.MASTER_ACK_OK:
                        ClientID = Util.parseNum(Msg, 2, 2 + 8);
                        System.out.println("client id : " + ClientID);
                        break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void makeDir(String dir) throws Exception {
        /**
         * 在当前工作目录下创建文件夹
         * 发送消息格式：
         * [Head(1B) OpType(1B) DirName(variable)]
         *
         * */
        byte buf[] = new byte[2 + dir.length()];
        buf[0] = MSG.HEAD_CLIENT;
        buf[1] = MSG.CLIENT_CREATE_FILE;
        for (int i = 0; i < dir.length(); i++) {
            buf[i + 2] = (byte) dir.charAt(i);
        }
        Out.write(buf, 0, buf.length);
        int len = In.read(Msg, 0, Msg.length);
        if (len >= 2) {
            byte status = Msg[1];
            switch (status) {
                case MSG.MASTER_ACK_OK:

                    break;
            }
        }


    }

    public void printMenu() {
        System.out.println("usage:\n" +
                "pwd           : print working directory\n" +
                "cd dir        : change directory\n" +
                "ls            : list all current directory files\n" +
                "mkdir dir     : make a directory under current directory\n" +
                "create file   : create file under current directory\n" +
                "rm file       : remove file or directory\n" +
                "stat file     : get file status information\n" +
                "exit          : exit system\n");
    }

}
