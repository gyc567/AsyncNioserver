package com.github.eric;

import com.lmax.disruptor.EventHandler;

import java.io.IOException;
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
    }

    public  void onAccept(NioTcpEvent event) throws IOException {
        SelectionKey key=event.getSelectionKey();
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        log.info("Server: accept client socket " + socketChannel);
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ);

    }
}
