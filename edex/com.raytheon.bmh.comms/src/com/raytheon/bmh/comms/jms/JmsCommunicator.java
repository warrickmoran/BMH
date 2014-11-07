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
package com.raytheon.bmh.comms.jms;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.url.URLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raytheon.bmh.comms.CommsManager;
import com.raytheon.bmh.comms.DacTransmitKey;
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.bmh.comms.logging.JmsStatusMessageAppender;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.jms.notification.JmsNotificationManager;
import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.comms.CommsConfig;

/**
 * 
 * Component of the {@link CommsManager} responsible for communicating over JMS.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 25, 2014  3399     bsteffen    Initial creation
 * Jul 31, 2014  3286     dgilling    Wire up DacHardwareStatusNotification.
 * Sep 23, 2014  3485     bsteffen    Enable sending anything for dac status.
 *                                    Add methods to specifically listen for playlist changes.
 * Oct 16, 2014  3687     bsteffen    Implement practice mode.
 * Nov 03, 2014  3525     bsteffen    Allow sending of status messages to alert topic.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class JmsCommunicator extends JmsNotificationManager {

    private static final Logger logger = LoggerFactory
            .getLogger(JmsCommunicator.class);

    private static final int DAC_STATUS_QUEUE_SIZE = 100;

    private final boolean operational;
    
    private final ProducerWrapper dacStatusProducer;

    private final ProducerWrapper alertProducer;

    public JmsCommunicator(CommsConfig config, boolean operational)
            throws URLSyntaxException {
        super(new AMQConnectionFactory(config.getJmsConnection()));
        this.operational = operational;
        String topic = "BMH.DAC.Status";
        if (!operational) {
            topic = "BMH.Practice.DAC.Status";
        }
        this.dacStatusProducer = new ProducerWrapper(topic, DAC_STATUS_QUEUE_SIZE);

        this.alertProducer = new ProducerWrapper("edex.alerts.msg");
        JmsStatusMessageAppender.setJmsCommunicator(this);
    }

    public void sendDacStatus(Object notification) {
        dacStatusProducer.enqueue(notification);
        dacStatusProducer.sendQueued();
    }

    public void sendStatusMessage(StatusMessage message) throws JMSException,
            SerializationException {
        alertProducer.send(message);
    }

    @Override
    public void connect(boolean notifyError) {
        super.connect(notifyError);
        dacStatusProducer.sendQueued();
    }

    @Override
    public void disconnect(boolean notifyError) {
        disconnectProducers();
        super.disconnect(notifyError);
    }

    @Override
    public void onException(JMSException e) {
        super.onException(e);
        disconnectProducers();
    }

    public void listenForPlaylistChanges(DacTransmitKey key, String group,
            DacTransmitServer server) {
        addQueueObserver(
                PlaylistUpdateNotification.getQueueName(group, operational),
                new PlaylistNotificationObserver(server, key));
    }

    public void unlistenForPlaylistChanges(DacTransmitKey key, String group,
            DacTransmitServer server) {
        removeQueueObserver(
                PlaylistUpdateNotification.getQueueName(group, operational),
                null, new PlaylistNotificationObserver(server, key));
    }

    protected synchronized void disconnectProducers() {
        alertProducer.disconnect();
        dacStatusProducer.disconnect();
    }

    @Override
    public void close() {
        JmsStatusMessageAppender.setJmsCommunicator(null);
        super.close();
    }

    /**
     * Class for holding the {@link Session} and {@link MessageProducer}
     * associated with a producer. This also provides the option to have a queue
     * of messages that will be sent later if there is a temporary
     * communications problem.
     */
    private class ProducerWrapper {

        private final String topicName;

        private final Deque<Object> unsent;

        private Session session;

        private MessageProducer producer;

        public ProducerWrapper(String topicName) {
            this(topicName, 1);
        }

        public ProducerWrapper(String topicName, int queueSize) {
            this.topicName = topicName;
            unsent = new LinkedBlockingDeque<>(queueSize);
        }

        public synchronized void send(Object message) throws JMSException,
                SerializationException {
            if (session == null) {
                session = createSession();
            }
            if (session == null) {
                return;
            }
            try {
                BytesMessage m = session.createBytesMessage();
                m.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
                byte[] bytes = SerializationUtil.transformToThrift(message);
                m.writeBytes(bytes);
                if (producer == null) {
                    Topic t = session.createTopic(topicName);
                    producer = session.createProducer(t);
                }
                producer.send(m);
            } catch (JMSException e) {
                disconnect();
                throw e;
            }
        }

        public void enqueue(Object message) {
            /* If too many messages are queued up drop the oldest message. */
            while (!unsent.offerLast(message)) {
                unsent.pollFirst();
            }
        }

        public synchronized void sendQueued() {
            Object message = unsent.pollFirst();
            while (message != null) {
                try {
                    send(message);
                } catch (JMSException e) {
                    logger.error("Cannot send message, will retry.", e);
                    disconnect();
                    unsent.offerFirst(message);
                    return;
                } catch (SerializationException e) {
                    logger.error("Unable to send message" + message, e);
                }
                message = unsent.pollFirst();
            }
        }

        public synchronized void disconnect() {
            if (producer != null) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    logger.error("Cannot close producer.", e);
                }
                producer = null;
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    logger.error("Cannot close producer session.", e);
                }
                session = null;
            }
        }

    }

}
