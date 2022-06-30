package com.room.server.session;


import io.netty.channel.Channel;

/**
 * 会话管理接口
 */
public interface Session {

    void bind(Channel channel, String username);

    void unbind(Channel channel);

    Object getAttribute(Channel channel, String name);

    void setAttribute(Channel channel, String name, Object value);

    Channel getChannel(String username);
}
