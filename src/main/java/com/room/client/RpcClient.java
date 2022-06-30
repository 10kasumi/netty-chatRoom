package com.room.client;

import com.room.client.handler.RpcResponseMessageHandler;
import com.room.message.RpcRequestMessage;
import com.room.protocol.MessageCodecSharable;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        RpcResponseMessageHandler rpcResponseMessageHandler = new RpcResponseMessageHandler();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(loggingHandler);
                            ch.pipeline().addLast(messageCodecSharable);
                            ch.pipeline().addLast(rpcResponseMessageHandler);
                        }
                    });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.writeAndFlush(new RpcRequestMessage(
                    1,
                    "com.room.server.service.HelloService",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"张三"}
            )).addListener(promise -> {
                if(!promise.isSuccess()){
                    Throwable cause = promise.cause();
                    log.error("error", cause);
                }
            });
            channel.closeFuture().sync();
        } catch (Exception e){
            log.error("client error: {}", e);
        } finally {
            group.shutdownGracefully();
        }

    }
}
