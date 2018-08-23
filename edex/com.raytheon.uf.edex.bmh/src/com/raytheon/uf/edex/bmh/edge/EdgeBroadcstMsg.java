/**
 * 
 */
package com.raytheon.uf.edex.bmh.edge;

import javax.jms.JMSException;
import org.apache.qpid.url.URLSyntaxException;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * @author awips
 *
 */
@DynamicSerialize
public class EdgeBroadcstMsg {

	
	/**
	 * The priority
	 */
	@DynamicSerializeElement
	private int priority;

	@DynamicSerializeElement
	private String audioFileName;

	/**
	 * The message
	 */
	@DynamicSerializeElement
	private byte[] message;

	@DynamicSerializeElement
	private String type;

	/**
	 * @param priority
	 * @param message
	 * @throws JMSException
	 * @throws URLSyntaxException
	 */
	public EdgeBroadcstMsg(byte[] audioMessage, String fileName, int priority)
			throws URLSyntaxException, JMSException {
		super();
		this.message = audioMessage;
		this.priority = priority;
		this.audioFileName = fileName;		
	}	

	/**
	 * @return the message
	 */
	public byte[] getMessage() {
		return message;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the audioFileName
	 */
	public String getAudioFileName() {
		return audioFileName;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @param audioFileName the audioFileName to set
	 */
	public void setAudioFileName(String audioFileName) {
		this.audioFileName = audioFileName;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(byte[] message) {
		this.message = message;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
