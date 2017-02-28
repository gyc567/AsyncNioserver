package com.github.eric.disruptor;


import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.TimeoutException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


/**
 * Created by eric567 on 8/23/2016.
 */
public class TinyHttpClient {

    
    public static final String HOSTNAME = "127.0.0.1";
    public static final int PORT = 3000;
    private SocketChannel socketChannel;

    private Selector selector;

    public static void main(String[] args) throws InterruptedException, IOException, AlertException, TimeoutException {


        // Kick off connection establishment
       // socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));

        TinyHttpClient client=new TinyHttpClient();
        client.init(new InetSocketAddress(HOSTNAME, PORT));
        client.invoke("hello server,i am client!");

    }


    private void init(final InetSocketAddress address) {
        System.out.println("Initializing client...");
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.bind(new InetSocketAddress(0));
            socketChannel.connect(address);
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            System.out.println("Client is connected at http://"+address.getHostName()+":"+address.getPort());
        } catch (final IOException e) {
            throw new RuntimeException("Exception while creating client", e);
        }
    }

    public void invoke(final String req) throws IOException {
        System.out.println("Invoking client...");

        while (selector.select() > 0) {
            final Set<SelectionKey> keys = selector.selectedKeys();
            final Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                final SelectionKey key = it.next();
                final SocketChannel channel = (SocketChannel) key.channel();
                it.remove();

                if (key.isConnectable()) {
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    }
                    channel.write(ByteBuffer.wrap(req.getBytes()));
                }
            }
        }
    }

}
