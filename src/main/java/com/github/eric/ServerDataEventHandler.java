package com.github.eric;

import com.lmax.disruptor.EventHandler;

/**
 * Created by eric567 on 7/22/2016.
 */
public class ServerDataEventHandler implements EventHandler<ServerDataEvent> {

    public void onEvent(ServerDataEvent event, long sequence, boolean endOfBatch) {

        System.out.println("doing business logic");
        System.out.println("Event: " + event);

    }


}
