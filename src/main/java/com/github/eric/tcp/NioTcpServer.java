package com.github.eric.tcp;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Created by eric567 on 8/13/2016.
 */
public class NioTcpServer implements Runnable {

    private static final Logger log = Logger.getLogger(NioTcpServer.class.getName());
    private InetSocketAddress inetSocketAddress;
    private Handler handler = new ServerHandler();

    private         Disruptor disruptor;

    public NioTcpServer(String hostname, int port) {
        inetSocketAddress = new InetSocketAddress(hostname, port);
        initDisruptor();
    }


    public void run() {
        try {
            Selector selector = Selector.open(); //
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); //
            serverSocketChannel.configureBlocking(false); //
            serverSocketChannel.socket().bind(inetSocketAddress);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //
            log.info("Server: socket server started.");
            while(true) { //
                int nKeys = selector.select();
                if(nKeys>0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectedKeys.iterator();
                    while(it.hasNext()) {
                        SelectionKey key = it.next();
                        if(key.isAcceptable()) {
                            log.info("Server: SelectionKey is acceptable.");
                           handler.handleAccept(key);
                           // publishEvent(NioTcpEventType.ACCEPT,key);

                        } else if(key.isReadable()) {
                            log.info("Server: SelectionKey is readable.");
                       //     handler.handleRead(key);
                            publishEvent(NioTcpEventType.READ,key);
                        }
                        else if(key.isWritable()) {
                            log.info("Server: SelectionKey is writable.");
//                            handler.handleWrite(key);
                            publishEvent(NioTcpEventType.WRITE,key);
                        }
                        it.remove();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void publishEvent(NioTcpEventType nioTcpEventType,SelectionKey key)
    {
        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<NioTcpEvent> ringBuffer = disruptor.getRingBuffer();

        NioTcpEventProducer producer = new NioTcpEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);

            //bb.putLong(0, 888);
           //producer.onData(bb);
            producer.onKey(nioTcpEventType,key);


    }

    public void initDisruptor()
    {

        // Executor that will be used to construct new threads for consumers
        Executor disrutporExecutor = ThreadPool.getInstance().getExecutor();

        // The factory for the event
        NioTcpEventFactory factory = new NioTcpEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        Disruptor<NioTcpEvent> disruptor =
                new Disruptor<NioTcpEvent>(factory, bufferSize, disrutporExecutor);


        // Connect the handler
        disruptor.handleEventsWith(new NioTcpEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();
        this.disruptor=disruptor;

    }


    public static void main(String[] args) throws InterruptedException {
        ThreadPool executor = ThreadPool.getInstance();
        NioTcpServer server = new NioTcpServer("localhost", 1000);
        executor.execute(server);



    }
}
