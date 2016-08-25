package com.github.eric.tcp;

import com.lmax.disruptor.EventHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 * Created by eric567 on 7/22/2016.
 */
public class NioTcpEventHandler implements EventHandler<NioTcpEvent> {
    private static final Logger log = Logger.getLogger(NioTcpEventHandler.class.getName());

    public void onEvent(NioTcpEvent event, long sequence, boolean endOfBatch) throws IOException {
        //System.out.println("Event: " + event);
        System.out.println("Event getSelectionKey: " + event.getSelectionKey());
        if (event.getNioTcpEventType().equals(NioTcpEventType.ACCEPT)) {
            onAccept(event);
        }
        if (event.getNioTcpEventType().equals(NioTcpEventType.READ)) {
            onRead(event);
        }
        if (event.getNioTcpEventType().equals(NioTcpEventType.WRITE)) {
            onWrite(event);
        }
    }

    public void onAccept(NioTcpEvent event) throws IOException {
        SelectionKey key = event.getSelectionKey();
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        log.info("Server: accept client socket " + socketChannel);
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ);

    }

    public void onWrite(NioTcpEvent event) throws IOException {
        SelectionKey key = event.getSelectionKey();
        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        byteBuffer.flip();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.write(byteBuffer);
        if (byteBuffer.hasRemaining()) {
            key.interestOps(SelectionKey.OP_READ);
        }
        byteBuffer.compact();

    }

    public void onRead(NioTcpEvent event) throws IOException {
        SelectionKey key = event.getSelectionKey();
        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        SocketChannel socketChannel = (SocketChannel) key.channel();
        while (true) {
            int readBytes = socketChannel.read(byteBuffer);
            if (readBytes > 0) {
                log.info("Server: readBytes = " + readBytes);
                log.info("Server: data = " + new String(byteBuffer.array(), 0, readBytes));
                String echo = "from server: I got u ,hello client!";

                //byteBuffer.flip();
                ByteBuffer sendBuffer = ByteBuffer.wrap(echo.getBytes("UTF-8"));
                socketChannel.write(sendBuffer);


                break;
            }
        }
        socketChannel.close();

    }
}
