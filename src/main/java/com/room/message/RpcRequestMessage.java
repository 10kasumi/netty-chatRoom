package com.room.message;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class RpcRequestMessage extends Message {

    private String interfaceName;

    private String methodName;

    private Class<?> returnType;

    private Class[] parameterTypes;

    private Object[] parameterValue;

    public RpcRequestMessage(int sequenceId, String interfaceName, String methodName, Class<?> returnType, Class[] parameterTypes, Object[] parameterValue) {
        super.setSequenceId(sequenceId);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterValue = parameterValue;
    }

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_REQUEST;
    }
}
