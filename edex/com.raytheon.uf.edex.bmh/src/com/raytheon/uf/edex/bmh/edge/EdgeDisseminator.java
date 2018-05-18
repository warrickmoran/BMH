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


import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.msg.BroadcastMsgGroup;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylist;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessage;
import com.raytheon.uf.common.bmh.datamodel.playlist.DacPlaylistMessageMetadata;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.BMHJmsDestinations;
import com.raytheon.uf.edex.bmh.msg.logging.MessageActivity.MESSAGE_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.IMessageLogger;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_ACTIVITY;
import com.raytheon.uf.edex.bmh.msg.logging.ErrorActivity.BMH_COMPONENT;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.core.EdexException;
import com.raytheon.uf.edex.core.IContextStateProcessor;

/**
 * The Edge Disseminator will transfer broadcastrmessages to the edge topic
 * to make available to the edge utility
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

	private static final IBMHStatusHandler statusHandler = BMHStatusHandler
			.getInstance(EdgeDisseminator.class);        

	private final IMessageLogger messageLogger;

	public EdgeDisseminator(final IMessageLogger messageLogger) {
		this.messageLogger = messageLogger;
		try {
            this.jaxbManager = new JAXBManager(DacPlaylistMessage.class,
                    DacPlaylistMessageMetadata.class, DacPlaylist.class);
        } catch (JAXBException e) {
            throw new RuntimeException(
                    "Failed to instantiate the JAXB Manager.", e);
        }
	}    

	private void initialize() {
		statusHandler.info("Initializing the EDGE Disseminator ...");
		/**
		 *Add initialization steps here.
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
			throw new Exception(
					"Receieved an empty playlist");			
		}
		try {
			this.sendToDestination(BMHJmsDestinations.getBMHEdgeDestination(operational),
					SerializationUtil.transformToThrift(group));						
		}
		catch (EdexException | SerializationException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to send playlist notification.", e);
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
			throw new Exception(
					"Receieved an empty playlist");			
		}
		try {			
			this.sendToDestination(BMHJmsDestinations.getBMHEdgeDestination(operational),
					jaxbManager.marshalToXml(playlist));
						
		}
		catch (EdexException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to send playlist notification.", e);
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
			throw new Exception(
					"Receieved an empty messageMetadata");			
		}
		try {
			this.sendToDestination(BMHJmsDestinations.getBMHEdgeDestination(operational),
					jaxbManager.marshalToXml(messageMetadata));
						
		}
		catch (EdexException e) {
            statusHandler.error(BMH_CATEGORY.PLAYLIST_MANAGER_ERROR,
                    "Unable to send playlist notification.", e);
		}

	}

	private void sendToDestination(final String destinationURI,
			final Object message) throws EdexException {
		EDEXUtil.getMessageProducer().sendAsyncUri(destinationURI, message);
	}


	}