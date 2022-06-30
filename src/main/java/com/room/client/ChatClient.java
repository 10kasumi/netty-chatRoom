package com.room.client;

import com.room.message.*;
import com.room.protocol.MessageCodecSharable;
import com.room.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable messageCodecSharable = new MessageCodecSharable();
        CountDownLatch loginCountDown = new CountDownLatch(1);
        AtomicBoolean login = new AtomicBoolean(false);
        AtomicBoolean exit = new AtomicBoolean(false);
        Scanner scanner = new Scanner(System.in);
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProcotolFrameDecoder());
                            ch.pipeline().addLast(messageCodecSharable);
                            ch.pipeline().addLast(new IdleStateHandler(0, 3, 0));
                            ch.pipeline().addLast(new ChannelDuplexHandler(){
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if(event.state() == IdleState.WRITER_IDLE){
                                        ctx.writeAndFlush(new PingMessage());
                                    }
                                }
                            });
                            ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter(){
                                /**
                                 * Calls {@link ChannelHandlerContext#fireChannelRead(Object)} to forward
                                 * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
                                 * <p>
                                 * Sub-classes may override this method to change behavior.
                                 *
                                 * @param ctx
                                 * @param msg
                                 */
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    log.debug("msg: {}", msg);
                                    if(msg instanceof LoginResponseMessage){
                                        LoginResponseMessage response = (LoginResponseMessage) msg;
                                        if(response.isSuccess()){
                                            login.set(true);
                                        }
                                        loginCountDown.countDown();
                                    }
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    new Thread(() -> {
                                        System.out.println("请输入用户名：");
                                        String username = scanner.nextLine();
                                        if (exit.get()) {
                                            return;
                                        }
                                        System.out.println("请输入密码：");
                                        String password = scanner.nextLine();
                                        if (exit.get()) {
                                            return;
                                        }
                                        LoginRequestMessage loginMsg = new LoginRequestMessage(username, password);
                                        System.out.println(loginMsg);
                                        ctx.writeAndFlush(loginMsg);
                                        System.out.println("正在处理中");
                                        try {
                                            loginCountDown.await();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        //登录失败
                                        if (!login.get()) {
                                            ctx.channel().close();
                                            return;
                                        }
                                        while (true) {
                                            System.out.println("==================================");
                                            System.out.println("send [username] [content]");
                                            System.out.println("groupsend [group name] [content]");
                                            System.out.println("groupcreate [group name] [m1,m2,m3...]");
                                            System.out.println("groupmembers [group name]");
                                            System.out.println("groupjoin [group name]");
                                            System.out.println("groupquit [group name]");
                                            System.out.println("quit");
                                            System.out.println("==================================");
                                            String command = null;
                                            try {
                                                command = scanner.nextLine();
                                            } catch (Exception e) {
                                                break;
                                            }
                                            if (exit.get()) {
                                                return;
                                            }
                                            String[] cs = command.split(" ");
                                            switch (cs[0]) {
                                                case "send":
                                                    ctx.writeAndFlush(new ChatRequestMessage(username, cs[1], cs[2]));
                                                    break;
                                                case "groupsend":
                                                    ctx.writeAndFlush(new GroupChatRequestMessage(username, cs[1], cs[2]));
                                                    break;
                                                case "groupcreate":
                                                    Set<String> set = new HashSet<>(Arrays.asList(cs[2].split(",")));
                                                    set.add(username); // 加入自己
                                                    ctx.writeAndFlush(new GroupCreateRequestMessage(cs[1], set));
                                                    break;
                                                case "groupmembers":
                                                    ctx.writeAndFlush(new GroupMembersRequestMessage(cs[1]));
                                                    break;
                                                case "groupjoin":
                                                    ctx.writeAndFlush(new GroupJoinRequestMessage(username, cs[1]));
                                                    break;
                                                case "groupquit":
                                                    ctx.writeAndFlush(new GroupQuitRequestMessage(username, cs[1]));
                                                    break;
                                                case "quit":
                                                    ctx.channel().close();
                                                    return;
                                            }
                                        }
                                    }, "input command").start();
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.debug("连接断开");
                                    exit.set(true);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    log.debug("连接断开");
                                    exit.set(true);
                                }
                            });
                        }
                    });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e){
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
