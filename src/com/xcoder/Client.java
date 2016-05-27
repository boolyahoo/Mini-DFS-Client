package com.xcoder;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Netty异步通信框架客户端
 */
public class Client {
    private String host = "localhost";
    private int port = 18888;
    private PrintStream out = System.out;

    public Client(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bStrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());
            Channel channel = bStrap.connect(host, port).sync().channel();
            SocketAddress address = channel.localAddress();
            out.println("client address : " + address.toString());
            out.println("client started!");
            BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                channel.writeAndFlush(bReader.readLine() + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws Exception{
        new Client("localhost",8080).run();
    }


}