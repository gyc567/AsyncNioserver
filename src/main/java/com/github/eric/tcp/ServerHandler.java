package com.github.eric.tcp;

/**
 * Created by eric567 on 8/13/2016.
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class ServerHandler implements Handler {
    private static Logger log = Logger.getLogger(ServerHandler.class.getName());


    public void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        log.info("Server: accept client socket " + socketChannel);
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ);
    }


    public void handleRead(SelectionKey key) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        SocketChannel socketChannel = (SocketChannel)key.channel();
        while(true) {
            int readBytes = socketChannel.read(byteBuffer);
            if(readBytes>0) {
                log.info("Server: readBytes = " + readBytes);
                log.info("Server: data = " + new String(byteBuffer.array(), 0, readBytes));
                String echo="I got u ,hello client!";

                byteBuffer.flip();
                ByteBuffer sendBuffer=ByteBuffer.wrap(echo.getBytes("UTF-8"));
                socketChannel.write(sendBuffer);
                break;
            }
        }
        socketChannel.close();
    }


    public void handleWrite(SelectionKey key) throws IOException {
        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        byteBuffer.flip();
        SocketChannel socketChannel = (SocketChannel)key.channel();
        socketChannel.write(byteBuffer);
        if(byteBuffer.hasRemaining()) {
            key.interestOps(SelectionKey.OP_READ);
        }
        byteBuffer.compact();
    }
}
