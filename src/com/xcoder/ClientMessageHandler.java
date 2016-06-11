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
            /**打印当前目录**/
            getWorkingDir();
        } else if (cmd.contains("cd")) {
            /**更改当前目录**/
            updateWorkingDir(cmd);
        } else if (cmd.equals("ls")) {
            /**列出当前目录下所有文件**/
            listAllFile();
        } else if (cmd.contains("mkdir")) {
            /**在当前目录下创建目录**/
            createFile(cmd, MSG.FILE_DIR);
        } else if (cmd.contains("create")) {
            /**在当前目录下创建文件**/
            createFile(cmd, MSG.FILE_COMN);
        } else if (cmd.contains("rm")) {
            /**删除当前目录下的文件或目录**/
            removeFile(cmd);
        } else if (cmd.contains("stat")) {
            /**列出当前目录下某一文件的信息**/
            System.out.println("sorry, not implement yet");
        } else if (cmd.equals("exit")) {
            Out.close();
            System.exit(0);
        } else {
            System.out.println("wrong command");
        }
    }


    private void getWorkingDir() {
        /**
         * pwd:打印当前工作目录
         *
         * 发送到消息格式：
         * [Head(1B) OpType(1B) ClientID(8B)]
         *
         * 返回消息格式：
         * [Head(1B) Status(1B) WorkingDir|Message(variable)]
         * */
        byte buf[] = new byte[10];
        buf[0] = MSG.HEAD_CLIENT;
        buf[1] = MSG.CLIENT_QUERY_PWD;
        Util.getNumBytes(ClientID, buf, 2, 2 + 8);
        try {
            Out.write(buf, 0, buf.length);
            Out.flush();
            //等待服务器返回
            int len = In.read(Msg, 0, Msg.length);
            byte status = Msg[1];
            switch (status) {
                case MSG.MASTER_ACK_OK:
                    WorkingDir = new String(Msg, 2, len - 2);
                    break;
                case MSG.MASTER_ACK_FAIL:
                    System.out.println("error:" + new String(Msg, 2, len - 2));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("working dir : " + WorkingDir);
    }


    private void updateWorkingDir(String cmd) throws Exception {
        /**
         * 更改工作目录到dir表示的目录
         *
         * 发送到消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) TargetDir(variable)]
         *
         * 返回消息格式：
         * [Head(1B) Status(1B) NewWorkingDir(variable)|errorMessage]
         * */
        String dir;
        if (cmd.split(" ").length >= 2) {
            dir = cmd.split(" ")[1];
        }else{
            System.out.println("error:inout target directory");
            return;
        }
        byte buf[] = new byte[2 + 8 + dir.length()];
        buf[0] = MSG.HEAD_CLIENT;
        buf[1] = MSG.CLIENT_UPDATE_PWD;
        Util.getNumBytes(ClientID, buf, 2, 2 + 8);
        Util.stringToBytes(dir, buf, 10);
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


    /**
     * 向master发送消息表明自己是client
     */
    public void clientRegister() {
        /**
         * 发送到消息格式：
         * [Head(1B) OpType(1B)]
         * 返回消息格式：
         * [Head(1B) Status(1B) ClientID(8B)]
         * */
        try {
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
                        WorkingDir = "/";
                        System.out.println("client id : " + ClientID);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 在当前工作目录下创建文件夹
     *
     * @param cmd：命令行
     * @param type：文件类型
     */
    public void createFile(String cmd, byte type) throws Exception {
        /**
         * 发送消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) FileType(1B) DirName(variable)]
         *
         * 返回消息格式：
         * [Head(1B) Status(1B) Message(variable)]
         * */

        if (cmd.split(" ").length >= 2) {
            String dir = cmd.split(" ")[1];
            if (dir.charAt(0) == '/') {
                System.out.println("error:you can only make directories or files under current working directory");
                return;
            }
        }else{
            System.out.println("error:input a file or directory name");
            return;
        }
        String file = cmd.split(" ")[1];
        byte buf[] = new byte[2 + 8 + 1 + file.length()];
        buf[0] = MSG.HEAD_CLIENT;
        buf[1] = MSG.CLIENT_CREATE_FILE;
        Util.getNumBytes(ClientID, buf, 2, 2 + 8);
        buf[10] = type;
        Util.stringToBytes(file, buf, 11);
        Out.write(buf, 0, buf.length);
        int len = In.read(Msg, 0, Msg.length);
        if (len >= 2) {
            byte status = Msg[1];
            switch (status) {
                case MSG.MASTER_ACK_OK:
                    break;
                case MSG.MASTER_ACK_FAIL:
                    String msg = new String(Msg, 2, len - 2);
                    System.out.println(msg);
                    break;
            }
        }
    }


    /**
     * 列出当前工作目录下所有文件
     */
    public void listAllFile() throws Exception {
        /**
         * 列出当前工作目录下所有文件
         *
         * 发送消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) FileName(variable)]
         *
         * 返回的消息格式：
         * OK : [Head(1B) Status(1B) FileList]
         * FileList:
         * Type(1B) FileNameLenth(1B) FileName(variable)
         *
         * FAIL : [Head(1B) Status(1B) ErrorMessage(variable)]
         * */
        byte buf[] = new byte[1024];
        buf[0] = MSG.HEAD_CLIENT;
        buf[1] = MSG.CLIENT_READ_FILE;
        Util.getNumBytes(ClientID, buf, 2, 2 + 8);
        Util.stringToBytes(WorkingDir, buf, 10);
        Out.write(buf, 0, 10 + WorkingDir.length());
        int len = In.read(Msg, 0, Msg.length);
        if (len >= 2) {
            byte status = Msg[1];
            switch (status) {
                case MSG.MASTER_ACK_OK:
                    int index = 2;
                    while (index < len) {
                        byte type = Msg[index++];
                        byte step = Msg[index++];
                        String f = new String(Msg, index, step);
                        index += step;
                        System.out.println((type == MSG.FILE_COMN ? "-:" : "d:") + f);
                    }
                    break;
                case MSG.MASTER_ACK_FAIL:
                    String msg = new String(Msg, 0, len - 2);
                    System.out.println(msg);
                    break;
            }
        }

    }


    /**
     * 删除当前目录下的文件
     * @param cmd:命令行
     * */
    private void removeFile(String cmd) throws Exception{
        /**
         * 发送消息格式：
         * [Head(1B) OpType(1B) ClientID(8B) FileName(variable)]
         *
         * 返回的消息格式：
         * [Head(1B) Status(1B) Message(variable)]
         * */
        if(cmd.split(" ").length < 2){
            System.out.println("error:input file name");
            return;
        }
        String file = cmd.split(" ")[1];
        if(file.charAt(0) == '/'){
            System.out.println("error:can only remove file or directory under current working directory");
            return;
        }

        if(WorkingDir.equals("/")){
            file = WorkingDir + file;
        }else{
            file = WorkingDir + "/" + file;
        }
        byte buf[] = new byte[1024];
        buf[0] = MSG.HEAD_CLIENT;
        buf[1] = MSG.CLIENT_DELETE_FILE;
        Util.getNumBytes(ClientID, buf, 2, 2 + 8);
        Util.stringToBytes(file, buf, 10);
        Out.write(buf, 0, 10 + file.length());
        int len = In.read(Msg, 0, Msg.length);
        if(len >= 2){
            byte status = Msg[1];
            switch (status){
                case MSG.MASTER_ACK_OK:
                    System.out.println("remove " + file + " successful");
                    break;
                case MSG.MASTER_ACK_FAIL:
                    String msg = new String(Msg, 2, len - 2);
                    System.out.println(msg);
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
