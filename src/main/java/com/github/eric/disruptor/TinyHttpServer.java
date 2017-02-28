package com.github.eric.disruptor;


import com.lmax.disruptor.*;
import com.lmax.disruptor.TimeoutException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;


/**
 * Created by eric567 on 8/23/2016.
 */
public class TinyHttpServer {


    public static final String HOSTNAME = "127.0.0.1";
    public static final int PORT = 3000;

    public static void main(String[] args) throws InterruptedException, IOException, AlertException, TimeoutException {
        final int cores = Runtime.getRuntime().availableProcessors();
        final int bufferSize = 4 * 1024;
        final int ringSize = 2048;
        Executor executor = Executors.newFixedThreadPool(cores);

        final BlockingDeque<ByteBuffer> bufferPool = buildBufferPool(bufferSize, ringSize);

        final Map<Long, SocketChannel> channels = new ConcurrentSkipListMap();
        final Map<Long, SelectionKey> selectionKeys = new ConcurrentSkipListMap();

        final RingBuffer<SelectionEvent> workerRing = getSelectionEventRingBuffer(cores, executor, bufferPool, channels, selectionKeys);

        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);


        Selector selector = Selector.open();
        startServer(bufferSize, channels, selectionKeys, workerRing, serverSocketChannel, selector);
    }

    private static RingBuffer<SelectionEvent> getSelectionEventRingBuffer(int cores, Executor executor, BlockingDeque<ByteBuffer> bufferPool, Map<Long, SocketChannel> channels, Map<Long, SelectionKey> selectionKeys) {
        int handlerCount = cores;
        final Sequence[] sequences = new Sequence[handlerCount];
        WorkerPool workerPool = buildWorkerPool(bufferPool, channels, selectionKeys, handlerCount, sequences);
        final RingBuffer<SelectionEvent> workerRing = workerPool.start(executor);
        workerRing.addGatingSequences(sequences);
        return workerRing;
    }

    private static BlockingDeque<ByteBuffer> buildBufferPool(int bufferSize, int ringSize) {
        final BlockingDeque<ByteBuffer> bufferPool = new LinkedBlockingDeque(ringSize);
        for (int i = 0; i < ringSize; i++) {
            bufferPool.add(ByteBuffer.allocate(bufferSize));
        }
        return bufferPool;
    }

    private static void startServer(int bufferSize, Map<Long, SocketChannel> channels, Map<Long, SelectionKey> selectionKeys,
                                    RingBuffer<SelectionEvent> workerRing, ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(HOSTNAME, PORT), 1024);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server listen on :127.0.0.1:3000!------");

        // Allocate the first worker
        allocateJobToWorker(bufferSize, channels, selectionKeys, workerRing, serverSocketChannel, selector);
    }

    private static void allocateJobToWorker(int bufferSize, Map<Long, SocketChannel> channels, Map<Long, SelectionKey>
            selectionKeys, RingBuffer<SelectionEvent> workerRing, ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        long workerId = workerRing.next();
        while (true) {
            int cnt = 0;
            try {

                cnt = selector.select();

            } catch (CancelledKeyException e) {e.printStackTrace();}

            if (cnt > 0) {
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid()) {
                        SelectionEvent event;
                        if (key.isAcceptable()) {
                            workerId = handleAccept(bufferSize, channels, selectionKeys, workerRing, serverSocketChannel, selector, workerId);
                        } else if (key.isReadable()) {
                            workerId = handleRead(channels, selectionKeys, workerRing, workerId, key);

                        }
                    }
                }
            }
        }
    }

    private static WorkerPool buildWorkerPool(BlockingDeque<ByteBuffer> bufferPool, Map<Long, SocketChannel> channels, Map<Long, SelectionKey> selectionKeys, int handlerCount, Sequence[] sequences) {
        WorkHandler<SelectionEvent>[] handlers = new WorkHandler[handlerCount];
        for (int i = 0; i < handlerCount; i++) {
            buildWorkHandler(bufferPool, channels, selectionKeys, handlers, sequences, i);
        }

        // Use a WorkerPool for handling requests
        return new WorkerPool(
                new EventFactory<SelectionEvent>() {
                    public SelectionEvent newInstance() {
                        return new SelectionEvent();
                    }
                },

                new FatalExceptionHandler(),
                handlers);
    }

    private static void buildWorkHandler(final BlockingDeque<ByteBuffer> bufferPool, final Map<Long, SocketChannel> channels,
                                         final Map<Long, SelectionKey> selectionKeys, WorkHandler<SelectionEvent>[] handlers, final Sequence[] sequences, int i) {
        final int handlerId = i;
        handlers[i] = new WorkHandler<SelectionEvent>() {

            ByteBuffer msg = ByteBuffer.wrap(
                    ("HTTP/1.1 200 OK\r\n" +
                            "Connection: Keep-Alive\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: 12\r\n\r\n" +
                            "Hello World!").getBytes());
            Sequence sequence = new Sequence(-1);

            {
                sequences[handlerId] = sequence;
            }


            public void onEvent(SelectionEvent ev) throws Exception {
                // Allocate a ByteBuffer from a RingBuffer
                ByteBuffer buffer = bufferPool.take();
                if (buffer.position() > 0) {
                    buffer.clear();
                }

                SocketChannel channel = channels.get(ev.id);
                SelectionKey key = selectionKeys.get(ev.id);

                try {
                    int read = safeRead(channel, buffer);
                    while (read > 0) {
                        safeWrite(channel, msg.duplicate());

                        // Read the data into memory
                        buffer.flip();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        //String input = new String(bytes);
                        buffer.clear();
                        read = safeRead(channel, buffer);
                    }
                    if (read < 0) {
                        key.cancel();
                        selectionKeys.remove(ev.id);
                    }
                } finally {
                    // Put the ByteBuffer back into the RingBuffer for re-use
                    bufferPool.add(buffer);
                    sequence.set(ev.id);
                }
            }
        };
    }

    private static long handleRead(Map<Long, SocketChannel> channels,
                                   Map<Long, SelectionKey> selectionKeys,
                                   RingBuffer<SelectionEvent> workerRing, long workerId, SelectionKey key) {
        SelectionEvent event;// Allocate an Event object for dispatching to the handler
        event = workerRing.get(workerId);
        event.id = workerId;
        channels.put(workerId, (SocketChannel) key.channel());
        selectionKeys.put(workerId, key);
        // Dispatch this event to a handler
        workerRing.publish(workerId);
        // Immediately allocate the next worker ID
        workerId = workerRing.next();
        return workerId;
    }

    private static long handleAccept(int bufferSize, Map<Long, SocketChannel> channels,
                                     Map<Long, SelectionKey> selectionKeys, RingBuffer<SelectionEvent> workerRing,
                                     ServerSocketChannel serverSocketChannel, Selector selector, long workerId) throws IOException {
        SelectionEvent event;ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.setReceiveBufferSize(bufferSize);
        serverSocket.setReuseAddress(true);

        boolean hasSocket = true;
        do {
            SocketChannel channel = serverSocketChannel.accept();
            if (null != channel) {
                channel.configureBlocking(false);
//                channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
//                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
//                channel.setOption(StandardSocketOptions.SO_RCVBUF, bufferSize);
//                channel.setOption(StandardSocketOptions.SO_SNDBUF, bufferSize);
                SelectionKey readKey = channel.register(selector, SelectionKey.OP_READ);

                // Allocate an Event object for dispatching to the handler
                event = workerRing.get(workerId);
                event.id = workerId;
                channels.put(workerId, channel);
                selectionKeys.put(workerId, readKey);
                // Dispatch this event to a handler
                workerRing.publish(workerId);
                // Immediately allocate the next worker ID
                workerId = workerRing.next();
            } else {
                hasSocket = false;
            }
        } while (hasSocket);
        return workerId;
    }

    static int safeRead(ReadableByteChannel channel, ByteBuffer dst) throws IOException {
        int read = -1;
        try {
            // Read data from the Channel
            read = channel.read(dst);
        } catch (IOException e) {
            switch ("" + e.getMessage()) {
                case "null":
                case "Connection reset by peer":
                case "Broken pipe":
                    break;
                default:
                    e.printStackTrace();
            }
            channel.close();
        } catch (CancelledKeyException e) {
            channel.close();
        }
        return read;
    }

    static int safeWrite(WritableByteChannel channel, ByteBuffer src) throws IOException {
        int written = -1;
        try {
            // Write the response immediately
            written = channel.write(src);
        } catch (IOException e) {
            switch ("" + e.getMessage()) {
                case "null":
                case "Connection reset by peer":
                case "Broken pipe":
                    break;
                default:
                    e.printStackTrace();
            }
            channel.close();
        } catch (CancelledKeyException e) {
            channel.close();
        }
        return written;
    }

    static class SelectionEvent {

        Long id;

        public SelectionEvent() {
        }

    }

}
