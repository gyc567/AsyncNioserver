/**
 * 
 */
package com.github.eric;

import com.lmax.disruptor.EventFactory;


/**
 * @author eric guo 
 * @email  gyc567@aol.com
 */
public class ServerDataEventFactory implements EventFactory<ServerDataEvent> {

	/* (non-Javadoc)
	 * @see com.lmax.disruptor.EventFactory#newInstance()
	 */
	public ServerDataEvent newInstance() {
		// TODO Auto-generated method stub
		return new ServerDataEvent();
	}

}
