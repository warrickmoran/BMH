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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
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
import com.raytheon.bmh.comms.dactransmit.DacTransmitServer;
import com.raytheon.bmh.comms.logging.JmsStatusMessageAppender;
import com.raytheon.uf.common.bmh.datamodel.playlist.PlaylistUpdateNotification;
import com.raytheon.uf.common.jms.notification.JmsNotificationManager;
import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.stats.StatisticsEvent;
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
 * Nov 19, 2014  3817     bsteffen    Updates to send system status messages.
 * Feb 16, 2015  4107     bsteffen    Notify the playlist observer when it is successfully observing.
 * Apr 15, 2015  4397     bkowal      Added {@link #bmhStatisticProducer}.
 * Apr 20, 2015  4407     bkowal      Cleanup of {@link PlaylistUpdateNotification}.
 * Aug 12, 2015  4424     bkowal      Eliminate Dac Transmit Key.
 * Dec 21, 2015  5218     rjpeter     Added SendThread.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class JmsCommunicator extends JmsNotificationManager {

    private static final Logger logger = LoggerFactory
            .getLogger(JmsCommunicator.class);

    private static final int QUEUE_SIZE = 200;

    private static final int RETRY_LIMIT = 5;

    private final Set<PlaylistNotificationObserver> playlistObservers = new HashSet<>();

    private final boolean operational;

    private final String bmhStatusTopic;

    private final String bmhStatisticTopic;

    private final String alertTopic;

    private final SendThread sendThread;

    public JmsCommunicator(CommsConfig config, boolean operational)
            throws URLSyntaxException {
        super(new AMQConnectionFactory(config.getJmsConnection()));
        this.operational = operational;
        if (operational) {
            bmhStatusTopic = "BMH.Status";
            bmhStatisticTopic = "BMH.Statistic";
        } else {
            bmhStatusTopic = "BMH.Practice.Status";
            bmhStatisticTopic = null;
        }
        alertTopic = "edex.alerts.msg";
        sendThread = new SendThread(QUEUE_SIZE);
        /* Don't want a hung thread to hold up jvm shutdown */
        sendThread.setDaemon(true);
        sendThread.start();
        JmsStatusMessageAppender.setJmsCommunicator(this);
    }

    public void sendBmhStatus(Object notification) {
        sendThread.enqueue(bmhStatusTopic, notification);
    }

    public void sendBmhStat(StatisticsEvent event) {
        if (this.operational == false) {
            return;
        }
        sendThread.enqueue(bmhStatisticTopic, event);
    }

    public void sendStatusMessage(StatusMessage message) {
        sendThread.enqueue(alertTopic, message);
    }

    @Override
    public void connect(boolean notifyError) {
        super.connect(notifyError);

        if (connected) {
            sendThread.wake();
            synchronized (playlistObservers) {
                for (PlaylistNotificationObserver observer : playlistObservers) {
                    observer.connected();
                }
            }
        }
    }

    @Override
    public void disconnect(boolean notifyError) {
        synchronized (playlistObservers) {
            for (PlaylistNotificationObserver observer : playlistObservers) {
                observer.disconnected();
            }
        }

        sendThread.disconnect();
        super.disconnect(notifyError);
    }

    @Override
    public void onException(JMSException e) {
        super.onException(e);
        sendThread.disconnect();
    }

    public void listenForPlaylistChanges(String group, DacTransmitServer server) {
        String topic = PlaylistUpdateNotification.getTopicName(operational);
        PlaylistNotificationObserver observer = new PlaylistNotificationObserver(
                server, group);
        addObserver(topic, observer);
        synchronized (playlistObservers) {
            playlistObservers.add(observer);
            if (connected) {
                observer.connected();
            }
        }
    }

    public void unlistenForPlaylistChanges(String group,
            DacTransmitServer server) {
        String topic = PlaylistUpdateNotification.getTopicName(operational);
        PlaylistNotificationObserver observer = new PlaylistNotificationObserver(
                server, group);
        synchronized (playlistObservers) {
            playlistObservers.remove(observer);
            if (connected) {
                observer.disconnected();
            }
        }
        removeObserver(topic, observer, null);

    }

    @Override
    public void close() {
        JmsStatusMessageAppender.setJmsCommunicator(null);
        super.close();

        // any unsent message will be discarded
        sendThread.continueRunning = false;

        // this will interrupt unsent.take() as well as sleep/wait
        sendThread.interrupt();
    }

    private class MessageWrapper {
        private final String topicName;

        private final byte[] data;

        private int attempts = 0;

        public MessageWrapper(String topicName, byte[] data) {
            this.topicName = topicName;
            this.data = data;
        }
    }

    /**
     * Class for holding the {@link Session} and {@link MessageProducer}
     * associated with a producer. This also provides the option to have a queue
     * of messages that will be sent later if there is a temporary
     * communications problem.
     */
    private class SendThread extends Thread {
        private final BlockingDeque<MessageWrapper> unsent;

        private Session session;

        private boolean continueRunning = true;

        private final Map<String, MessageProducer> producers = new HashMap<String, MessageProducer>();

        private final Object connectionLock = new Object();

        public SendThread(int queueSize) {
            super("JmsMessageSender");
            unsent = new LinkedBlockingDeque<>(queueSize);
        }

        public void enqueue(String topic, Object obj) {
            try {
                MessageWrapper message = new MessageWrapper(topic,
                        SerializationUtil.transformToThrift(obj));

                /* If too many messages are queued up drop the oldest message. */
                while (!unsent.offerLast(message)) {
                    unsent.pollFirst();
                }
            } catch (SerializationException e) {
                logger.error("Error serializing message {} to topic {}", obj,
                        topic, e);
            }
        }

        @Override
        public void run() {
            while (continueRunning) {
                boolean messageProcessed = false;
                MessageWrapper message = null;

                try {
                    message = unsent.take();
                    MessageProducer producer = null;
                    BytesMessage m = null;

                    synchronized (connectionLock) {
                        if (session == null) {
                            session = createSession();
                        }

                        if (session == null) {
                            connectionLock.wait(10000);
                            continue;
                        }

                        message.attempts++;
                        producer = producers.get(message.topicName);

                        if (producer == null) {
                            Topic t = session.createTopic(message.topicName);
                            producer = session.createProducer(t);
                            producers.put(message.topicName, producer);
                        }

                        m = session.createBytesMessage();
                    }

                    m.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
                    m.writeBytes(message.data);

                    producer.send(m);
                    messageProcessed = true;
                } catch (InterruptedException e) {
                    // ignore
                } catch (JMSException e) {
                    logger.error("Error occurred sending message to topic {}",
                            message.topicName, e);

                    JmsCommunicator.this.disconnect();
                } catch (Throwable e) {
                    if (message != null) {
                        logger.error(
                                "Unexcepted error occurred sending message to JMS topic {}",
                                message.topicName, e);
                    } else {
                        logger.error(
                                "Unexcepted error occurred getting next message to send via JMS",
                                e);
                    }
                } finally {
                    if ((messageProcessed == false) && (message != null)) {
                        if (message.attempts >= RETRY_LIMIT) {
                            logger.warn(
                                    "Message for topic {} failed to deliver {} times, dropping message",
                                    message.topicName, message.attempts);
                        } else if (unsent.offerFirst(message) == false) {
                            logger.warn("Internal message queue is full, dropping message");
                        }
                    }

                }
            }

        }

        public void wake() {
            synchronized (connectionLock) {
                connectionLock.notify();
            }
            this.interrupt();
        }

        public void disconnect() {
            synchronized (connectionLock) {
                for (Map.Entry<String, MessageProducer> entry : producers
                        .entrySet()) {
                    try {
                        entry.getValue().close();
                    } catch (JMSException e) {
                        logger.error("Cannot close producer for topic {}.",
                                entry.getKey(), e);
                    }
                }

                producers.clear();

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

}
