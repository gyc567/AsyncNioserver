package com.github.eric.tcp;

import com.lmax.disruptor.EventFactory;

/**
 * Created by eric567 on 7/22/2016.
 */
public class NioTcpEventFactory implements EventFactory<NioTcpEvent>
{
    public NioTcpEvent newInstance()
    {
        return new NioTcpEvent();
    }
}
