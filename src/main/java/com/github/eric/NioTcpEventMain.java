package com.github.eric;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by eric567 on 7/22/2016.
 */
public class NioTcpEventMain
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
        for (long l = 0; true; l++)
        {
            bb.putLong(0, l);
            producer.onKey(NioTcpEventType.READ, new SelectionKey() {
                @Override
                public SelectableChannel channel() {
                    return null;
                }

                @Override
                public Selector selector() {
                    return null;
                }

                @Override
                public boolean isValid() {
                    return false;
                }

                @Override
                public void cancel() {

                }

                @Override
                public int interestOps() {
                    return 0;
                }

                @Override
                public SelectionKey interestOps(int ops) {
                    return null;
                }

                @Override
                public int readyOps() {
                    return 0;
                }
            });
            Thread.sleep(1000);
        }
    }
}
