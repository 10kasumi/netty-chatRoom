package com.room.server.session;


import io.netty.channel.Channel;

import java.util.List;
import java.util.Set;

public interface GroupSession {
    Group createGroup(String name, Set<String> members);

    Group joinMember(String name, String member);

    Group removeMember(String name, String member);

    Group removeGroup(String name);

    Set<String> getMembers(String name);

    List<Channel> getMembersChannel(String name);
}
