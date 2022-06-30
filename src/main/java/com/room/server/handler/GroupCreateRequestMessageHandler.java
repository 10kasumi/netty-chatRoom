package com.room.server.handler;

import com.room.message.GroupCreateRequestMessage;
import com.room.message.GroupCreateResponseMessage;
import com.room.server.session.Group;
import com.room.server.session.GroupSession;
import com.room.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        Set<String> members = msg.getMembers();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);
        if(group == null){
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, groupName + "创建成功"));
            List<Channel> channels = groupSession.getMembersChannel(groupName);
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true, "您以加入" + groupName));
            }
        } else{
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, groupName + "已存在"));
        }
    }
}
