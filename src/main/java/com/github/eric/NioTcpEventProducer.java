package com.github.eric;

import com.lmax.disruptor.RingBuffer;

import java.nio.channels.SelectionKey;

/**
 * Created by eric567 on 7/22/2016.
 */

public class NioTcpEventProducer {
    private final RingBuffer<NioTcpEvent> ringBuffer;

    public NioTcpEventProducer(RingBuffer<NioTcpEvent> ringBuffer) {

        this.ringBuffer = ringBuffer;
    }

//    public void onData(ByteBuffer bb) {
//        long sequence = ringBuffer.next();  // Grab the next sequence
//        try {
//            NioTcpEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
//            // for the sequence
//            event.set(bb.getLong(0));  // Fill with data
//
//        } finally {
//            ringBuffer.publish(sequence);
//        }
//    }
    public void onKey(NioTcpEventType nioTcpEventType,SelectionKey key) {
        long sequence = ringBuffer.next();  // Grab the next sequence
        try {
            NioTcpEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
            event.setSelectionKey(key);
            event.setNioTcpEventType(nioTcpEventType);

        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
