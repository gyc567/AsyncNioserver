package com.github.eric;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by eric567 on 7/22/2016.
 */
public class LongEventMain
{
    public static void main(String[] args) throws Exception
    {
        // Executor that will be used to construct new threads for consumers
        Executor executor = Executors.newCachedThreadPool();

        // The factory for the event
        NioTcpEventFactory factory = new NioTcpEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        Disruptor<NioTcpEvent> disruptor =
                new Disruptor<NioTcpEvent>(factory, bufferSize, executor);

//        Disruptor disruptor = new Disruptor(factory,
//                bufferSize,
//                ProducerType.SINGLE, // Single producer
//                new BlockingWaitStrategy(),
//                executor);
        // Connect the handler
        disruptor.handleEventsWith(new NioTcpEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<NioTcpEvent> ringBuffer = disruptor.getRingBuffer();

        NioTcpEventProducer producer = new NioTcpEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);
//        for (long l = 0; true; l++)
//        {
//            bb.putLong(0, l);
//            producer.onData(bb);
//            Thread.sleep(1000);
//        }
    }
}
