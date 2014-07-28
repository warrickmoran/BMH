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
package com.raytheon.uf.edex.bmh.comms;

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

import com.raytheon.uf.common.bmh.notify.MessagePlaybackStatusNotification;
import com.raytheon.uf.common.bmh.notify.PlaylistSwitchNotification;
import com.raytheon.uf.common.jms.notification.JmsNotificationManager;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.edex.bmh.comms.config.CommsConfig;

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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class JmsCommunicator extends JmsNotificationManager {

    private static final Logger logger = LoggerFactory
            .getLogger(JmsCommunicator.class);

    private static final int MAX_UNSENT_SIZE = 100;

    private Session producerSession;

    private MessageProducer producer;

    private Deque<Object> unsent = new LinkedBlockingDeque<>(MAX_UNSENT_SIZE);

    public JmsCommunicator(CommsConfig config) throws URLSyntaxException {
        super(new AMQConnectionFactory(config.getJmsConnection()));
    }

    public void sendStatus(PlaylistSwitchNotification notification) {
        sendStatusInternal(notification);
    }

    public void sendStatus(MessagePlaybackStatusNotification notification) {
        sendStatusInternal(notification);
    }

    protected void sendStatusInternal(Object notification) {
        /* If too many messages are queued up drop the oldest message. */
        while (!unsent.offerLast(notification)) {
            unsent.pollFirst();
        }
        connectProducerAndSend();
    }

    @Override
    public void connect(boolean notifyError) {
        super.connect(notifyError);
        connectProducerAndSend();
    }

    @Override
    public void disconnect(boolean notifyError) {
        disconnectProducer();
        super.disconnect(notifyError);
    }

    @Override
    public void onException(JMSException e) {
        super.onException(e);
        disconnectProducer();
    }

    protected synchronized void connectProducerAndSend() {
        if (producer == null) {
            try {
                producerSession = createSession();
                if (producerSession != null) {
                    Topic t = producerSession.createTopic("BMH.DAC.Status");
                    producer = producerSession.createProducer(t);
                }
            } catch (JMSException e) {
                logger.error("Cannot open producer.", e);
                disconnectProducer();
            }
        }
        /*
         * This is not just an else, if producer was not successfully created
         * this will be false.
         */
        if (producer != null) {
            Object statusObject = unsent.pollFirst();
            while (statusObject != null) {
                try {
                    byte[] bytes = SerializationUtil
                            .transformToThrift(statusObject);
                    BytesMessage m = producerSession.createBytesMessage();
                    m.writeBytes(bytes);
                    m.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
                    producer.send(m);
                } catch (JMSException e) {
                    logger.error("Cannot send message, will retry.", e);
                    disconnectProducer();
                    unsent.offerFirst(statusObject);
                    return;
                } catch (SerializationException e) {
                    logger.error("Unable to send message" + statusObject, e);
                }
                statusObject = unsent.pollFirst();
            }
        }
    }

    protected synchronized void disconnectProducer() {
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException e) {
                logger.error("Cannot close producer.", e);
            }
            producer = null;
        }
        if (producerSession != null) {
            try {
                producerSession.close();
            } catch (JMSException e) {
                logger.error("Cannot close producer session.", e);
            }
        }
    }

}