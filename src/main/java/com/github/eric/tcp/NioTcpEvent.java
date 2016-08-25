package com.github.eric.tcp;

import java.nio.channels.SelectionKey;

/**
 * Created by eric567 on 7/22/2016.
 */
public class NioTcpEvent
{
    //private long value;
    private  NioTcpEventType nioTcpEventType;

    public NioTcpEventType getNioTcpEventType() {
        return nioTcpEventType;
    }

    public void setNioTcpEventType(NioTcpEventType nioTcpEventType) {
        this.nioTcpEventType = nioTcpEventType;
    }

    private SelectionKey selectionKey;

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

   // public void set(long value)
//    {
//        this.value = value;
//    }
}
