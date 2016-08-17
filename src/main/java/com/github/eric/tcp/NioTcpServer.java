package com.github.eric.tcp;
import java.io.IOException;
import java.net.InetSocketAddress;
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

    public NioTcpServer(String hostname, int port) {
        inetSocketAddress = new InetSocketAddress(hostname, port);
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
                        } else if(key.isReadable()) {
                            log.info("Server: SelectionKey is readable.");
                            handler.handleRead(key);
                        }
                        else if(key.isWritable()) {
                            log.info("Server: SelectionKey is writable.");
                            handler.handleWrite(key);
                        }
                        it.remove();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public static void main(String[] args) {
        Executor executor = ThreadPool.getInstance();
        NioTcpServer server = new NioTcpServer("localhost", 1000);
        executor.execute(server);
    }
}
