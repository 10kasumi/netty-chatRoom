package com.room.server.handler;

import com.room.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.debug("{}", msg);
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if(promise != null){
            Object returnValue = msg.getReturnValue();
            Exception exceptionValue = msg.getExceptionValue();
            if(exceptionValue == null){
                promise.setSuccess(returnValue);
            } else{
                promise.setFailure(exceptionValue);
            }
        }
    }
}
