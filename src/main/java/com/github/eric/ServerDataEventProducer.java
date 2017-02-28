package com.github.eric;

import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by eric567 on 7/22/2016.
 */

public class ServerDataEventProducer {
    private final RingBuffer<ServerDataEvent> ringBuffer;

    public ServerDataEventProducer(RingBuffer<ServerDataEvent> ringBuffer) {

        this.ringBuffer = ringBuffer;
    }

    public void onData(ByteBuffer bb) {
        long sequence = ringBuffer.next();  // Grab the next sequence
        try {
            ServerDataEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
           // event.set(bb.getLong(0));  // Fill with data
            event.setData(bb.array());

        } finally {
            ringBuffer.publish(sequence);
        }
    }


    public void processData(TinyNIOServer server, SocketChannel socket, byte[] data, int count) {

        long sequence = ringBuffer.next();  // Grab the next sequence
        try {
            ServerDataEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
            // event.set(bb.getLong(0));  // Fill with data
            event.setData(data);
            event.setServer(server);
            event.setSocket(socket);

        } finally {
            ringBuffer.publish(sequence);
        }

    }
}
