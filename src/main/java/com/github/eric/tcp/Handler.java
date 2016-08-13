package com.github.eric.tcp;

/**
 * Created by eric567 on 8/13/2016.
 */

import java.io.IOException;
import java.nio.channels.SelectionKey;


interface Handler {
    /**
     * {@link SelectionKey#OP_ACCEPT}
     * @param key
     * @throws IOException
     */
    void handleAccept(SelectionKey key) throws IOException;
    /**
     * {@link SelectionKey#OP_READ}
     * @param key
     * @throws IOException
     */
    void handleRead(SelectionKey key) throws IOException;
    /**
     * {@link SelectionKey#OP_WRITE}
     * @param key
     * @throws IOException
     */
    void handleWrite(SelectionKey key) throws IOException;
}
