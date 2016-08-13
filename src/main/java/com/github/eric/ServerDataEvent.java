/**
 * 
 */
package com.github.eric;

import java.nio.channels.SocketChannel;

/**
 * @author eric guo 
 * @email  gyc567@aol.com
 */
public class ServerDataEvent {
	public TinyNIOServer server;
	public SocketChannel socket;
	public byte[] data;
	
	public ServerDataEvent(TinyNIOServer server, SocketChannel socket, byte[] data) {
		this.server = server;
		this.socket = socket;
		this.data = data;
	}

	/**
	 * @return the server
	 */
	public final TinyNIOServer getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public final ServerDataEvent setServer(TinyNIOServer server) {
		this.server = server;
		return this;
	}

	/**
	 * @return the socket
	 */
	public final SocketChannel getSocket() {
		return socket;
	}

	/**
	 * @param socket the socket to set
	 */
	public final ServerDataEvent setSocket(SocketChannel socket) {
		this.socket = socket;
		return this;
	}

	/**
	 * @return the data
	 */
	public final byte[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public final ServerDataEvent setData(byte[] data) {

		this.data = data;
		return this;
	}

	/**
	 * 
	 */
	public ServerDataEvent() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
