package com.xcoder;

import java.io.*;
import java.net.Socket;

/**
 * Created by booly on 2016/5/26.
 * usage
 * pwd           : print working directory
 * cd dir        : change directory
 * ls            : list all current directory files
 * mkdir dir     : make a directory under current directory
 * create file   : create file under current directory
 * rm file       : remove file or directory
 * stat file     : get file status information
 * exit          : exit system
 */
public class ClientMessageHandler {

    private Socket Socket;
    private BufferedReader In;
    private PrintWriter Out;
    private BufferedReader BReader;
    private long ClientID;
    private String WorkingDir;


    public ClientMessageHandler(Socket socket) {
        try {
            Socket = socket;
            In = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
            Out = new PrintWriter(Socket.getOutputStream(), true);
            BReader = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            printMenu();
            registerClient();
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


    public void judgeCmd(String cmd) throws Exception{
        if(cmd.equals("pwd")){
            if(WorkingDir == null){
                WorkingDir = pwd();
            }

        }else if(cmd.contains("cd")){

        }else if(cmd.equals("ls")){

        }else if(cmd.contains("mkdir")){

        }else if(cmd.contains("create")){

        }else if(cmd.contains("rm")){

        }else if(cmd.contains("stat")){

        }else if(cmd.equals("exit")){
            System.exit(0);
        }else{
            System.out.println("wrong command");
        }
    }


    public String pwd(){
        return null;
    }

    public void registerClient() {
        try{
            // 向master发送消息表明自己是client
            byte head[] = {MSG.HEAD_CLIENT, MSG.CLIENT_REGISTER};
            Out.println(new String(head) + "client message");
            String rsp = In.readLine();
            System.out.println("rsp length : " + rsp.length());
            if(rsp.length() >= 2){
                byte opType = rsp.substring(1,2).getBytes()[0];
                if(opType == MSG.MASTER_ACK){
                    ClientID = Long.parseLong(rsp.substring(2, rsp.length()));
                    System.out.println("client id : " + ClientID);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void printMenu(){
        System.out.println("usage:\n" +
                           "pwd           : print working directory\n" +
                           "cd dir        : change directory\n" +
                           "ls            : list all current directory files\n" +
                           "mkdir dir     : make a directory under current directory\n" +
                           "create file   : create file under current directory\n" +
                           "rm file       : remove file or directory\n" +
                           "stat file     : get file status information\n" +
                           "exit          : exit system");
    }

}
