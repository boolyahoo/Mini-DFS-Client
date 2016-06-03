package com.xcoder;

import io.netty.channel.*;

import java.io.PrintStream;

/**
 * Created by booly on 2016/5/26.
 */


public class ClientMessageHandler extends SimpleChannelInboundHandler<String> {

    private PrintStream out = System.out;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        out.println(s);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        out.println("channel active!");
        out.println("channel remote address : " + channel.remoteAddress());
        out.println("channel local address : " + channel.localAddress());
        byte type = 0x01;
        ChannelFuture cFuture = channel.writeAndFlush("client     fdffff" + "\r\n");
        cFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                out.println("client write to master successfully");
            }
        });
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        out.println("channel inactive");
        ctx.channel().close();
    }
}
