package com.github.eric;

import com.lmax.disruptor.EventFactory;

/**
 * Created by eric567 on 7/22/2016.
 */
public class LongEventFactory implements EventFactory<LongEvent>
{
    public LongEvent newInstance()
    {
        return new LongEvent();
    }
}
