package com.room.message;

public class PingMessage extends Message{
    @Override
    public int getMessageType() {
        return PingMessage;
    }
}
