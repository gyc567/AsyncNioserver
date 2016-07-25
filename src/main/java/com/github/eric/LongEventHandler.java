package com.github.eric;

import com.lmax.disruptor.EventHandler;

/**
 * Created by eric567 on 7/22/2016.
 */
public class LongEventHandler implements EventHandler<LongEvent>
{
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("Event: " + event);
    }
}
