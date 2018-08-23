/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.bmh.edge;

import javax.jms.BytesMessage;
import javax.jms.Session;
import javax.xml.bind.JAXBException;

import org.apache.qpid.client.AMQConnectionFactory;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.audio.AudioConvererterManager;
import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.BMHJmsDestinations;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;
import com.raytheon.uf.common.jms.JmsPooledConnection;
import com.raytheon.uf.common.jms.JmsPooledConnectionFactory;
import com.raytheon.uf.common.jms.wrapper.JmsSessionWrapper;
import javax.jms.Destination;
import javax.jms.MessageProducer;

/**
 * The Edge Disseminator will transfer broadcastrmessages to the edge topic to
 * make available to the edge utility
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * April 06, 2018       	ccastro      Initial creation
 * 
 * </pre>
 * 
 * @author ccastro
 * @version 0.1
 */

public class EdgeDisseminator {

	private boolean operational = true;
	private final JAXBManager jaxbManager;
	// Session session;
	private JmsSessionWrapper session;
	// private JmsPooledSession session;
	private JmsPooledConnection conn = null;
	Thread thread = Thread.currentThread();
	private JmsPooledConnectionFactory connFactory = null;
	private AMQConnectionFactory amqConnFactory = null;

	private static final IBMHStatusHandler statusHandler = BMHStatusHandler.getInstance(EdgeDisseminator.class);

	// private final IMessageLogger messageLogger;

	public EdgeDisseminator(final IMessageLogger messageLogger) {
		// this.messageLogger = messageLogger;
		try {
			this.jaxbManager = new JAXBManager(DacPlaylistMessage.class, DacPlaylistMessageMetadata.class,
					DacPlaylist.class);
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to instantiate the JAXB Manager.", e);
		}
	}

	@SuppressWarnings("unused")
	private void initialize() {
		statusHandler.info("Initializing the EDGE Disseminator ...");
		/**
		 * Add initialization steps here.
		 *
		 */
		statusHandler.info("Initialization Successful!");
	}
	

	/**
	 * Sends a BroadcastMsgGroup to Edge queue.
	 * 
	 * @param group
	 *            the specified {@BroadcastMsgGroup}.
	 * @throws Exception
	 *             if the specified {@BroadcastMsgGroup} is NULL
	 */
	public void sendToEdge(BroadcastMsgGroup group) throws Exception {
		if (group == null) {
			throw new Exception("Receieved an empty playlist");
		}
		try {
			this.sendToDestination(BMHJmsDestinations.getBMHEdgeDestination(operational),
					SerializationUtil.transformToThrift(group));
		} catch (EdexException | SerializationException e) {
			statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR, "Unable to send playlist notification.", e);
		}

	}
	/**
	 * Sends a DacPlaylist to Edge queue.
	 * 
	 * @param group
	 *            the specified {@DacPlaylist}.
	 * @throws Exception
	 *             if the specified {@DacPlaylist} is NULL
	 */
	public void sendToEdge(DacPlaylist playlist) throws Exception {
		if (playlist == null) {
			throw new Exception("Receieved an empty playlist");
		}
		try {
			this.sendToDestination(BMHJmsDestinations.getBMHEdgeDestination(operational),
					jaxbManager.marshalToXml(playlist));

		} catch (EdexException e) {
			statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR, "Unable to send playlist notification.", e);
		}

	}
	/**
	 * Sends a DacPlaylistMessageMetadata to Edge queue.
	 * 
	 * @param group
	 *            the specified {@DacPlaylistMessageMetadata}.
	 * @throws Exception
	 *             if the specified {@DacPlaylistMessageMetadata} is NULL
	 */
	public void sendToEdge(DacPlaylistMessageMetadata messageMetadata) throws Exception {
		if (messageMetadata == null) {
			throw new Exception("Receieved an empty messageMetadata");
		}
		try {
			this.sendToDestination(BMHJmsDestinations.getBMHEdgeDestination(operational),
					jaxbManager.marshalToXml(messageMetadata));

		} catch (EdexException e) {
			statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR, "Unable to send playlist notification to Edge", e);
		}

	}

	/**
	 * Sends a StreamMessage to Edge queue.
	 * 
	 * @param audioMessage
	 *            the specified {AudioMessage}.
	 * @throws Exception
	 *             if the specified {AudioMessage} is NULL
	 */
	public void sendToEdge(byte[] audioMessage, String id) throws Exception {
		if (audioMessage == null) {
			throw new Exception("Receieved an empty audioMessage");
		}

		String jmsVirtualHost = System.getenv("JMS_VIRTUALHOST");
		String jmsServer = System.getenv("JMS_SERVER");
		String connString = "amqp://guest:guest@/" + jmsVirtualHost + "?brokerlist='" + jmsServer + "'&amp;ssl='true'";
		String filename = id; // id.substring(0, id.indexOf('.'));

		amqConnFactory = new AMQConnectionFactory(connString);
		connFactory = new JmsPooledConnectionFactory(amqConnFactory);
		conn = new JmsPooledConnection(connFactory, thread);
		session = conn.getSession(false, Session.AUTO_ACKNOWLEDGE);

		// Manually send to queue for the prototype.
		// Cannot get the EdexUtil to send a BytesMessage
		Destination dest = session.createQueue("BMH.EDGE");
		MessageProducer messProducer = session.createProducer(dest);
		BytesMessage bMess = session.createBytesMessage();
		BytesMessage newbMess = session.createBytesMessage();
		BytesMessage newbMessMp3 = session.createBytesMessage();

		// Default priority is 4. Set it higher to get higher route priority
		int priority = 6;
		bMess.writeBytes(SerializationUtil.transformToThrift(audioMessage));
		bMess.setJMSPriority(priority);
		bMess.setStringProperty("FileName", filename);
		bMess.setJMSType(audioMessage.getClass().getName());
		bMess.reset();

		// Convert Msg
		byte[] convertedMessage = null;
        // Convert Msg in 2 steps ULAW->WAV->MP3
		try {
			convertedMessage = AudioConvererterManager.getInstance().convertAudio(audioMessage, BMHAudioFormat.ULAW,
					BMHAudioFormat.WAV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// If ULAW was converted to WAV then convert to MP3 and send
		if (convertedMessage != null) {
			byte[] convertedMessageMp3 = null;
			try {
				convertedMessageMp3 = AudioConvererterManager.getInstance().convertAudio(convertedMessage,
						BMHAudioFormat.WAV, BMHAudioFormat.MP3);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (convertedMessageMp3 != null) {
				newbMessMp3.setJMSPriority(priority);
				newbMessMp3.writeBytes(SerializationUtil.transformToThrift(convertedMessageMp3));
				newbMessMp3.setStringProperty("FileName", filename);
				messProducer.send(newbMessMp3);
			}

		}

		// Manually sending to queue for the prototype.
		// Cannot get the EdexUtil to send a BytesMessage
		/*
		 * try {
		 * this.sendToDestination(BMHJmsDestinations.getBMHEdgeDestination(
		 * operational) + options,bMess); } catch (EdexException e) {
		 * statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
		 * "Unable to send audio message.", e); }
		 */
	}

	private void sendToDestination(final String destinationURI, final Object message)
			throws EdexException, SerializationException {
		EDEXUtil.getMessageProducer().sendAsyncUri(destinationURI, message);
	}

}