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
package com.raytheon.uf.common.bmh.datamodel.transmitter;

import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter.TxMode;
import com.raytheon.uf.common.serialization.IDeserializationContext;
import com.raytheon.uf.common.serialization.ISerializationContext;
import com.raytheon.uf.common.serialization.ISerializationTypeAdapter;
import com.raytheon.uf.common.serialization.SerializationException;

/**
 * Serialization Adapter for {@link Transmitter}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 6, 2014    3173     mpduff      Initial creation
 * Feb 09, 2015   4095     bsteffen    Remove Transmitter Name.
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class TransmitterAdapter implements
        ISerializationTypeAdapter<Transmitter> {

    @Override
    public void serialize(ISerializationContext serializer,
            Transmitter transmitter) throws SerializationException {
        serializeNoGroup(serializer, transmitter);
        TransmitterGroupAdapter.serializeNoTransmitter(serializer,
                transmitter.getTransmitterGroup());
    }

    @Override
    public Transmitter deserialize(IDeserializationContext deserializer)
            throws SerializationException {
        Transmitter t = deserializeNoGroup(deserializer);
        TransmitterGroup tg = TransmitterGroupAdapter
                .deserializeNoTransmitter(deserializer);
        t.setTransmitterGroup(tg);

        return t;
    }

    /**
     * Serialize with no group info.
     * 
     * @param serializer
     *            The serializer
     * 
     * @param transmitter
     *            The Transmitter to serialize
     * @throws SerializationException
     */
    public static void serializeNoGroup(ISerializationContext serializer,
            Transmitter transmitter) throws SerializationException {
        serializer.writeString(transmitter.getCallSign());
        serializer.writeObject(transmitter.getFipsCode());
        serializer.writeString(transmitter.getLocation());
        serializer.writeString(transmitter.getMnemonic());
        serializer.writeString(transmitter.getServiceArea());
        serializer.writeObject(transmitter.getDacPort());
        serializer.writeFloat(transmitter.getFrequency());
        serializer.writeI32(transmitter.getId());
        serializer.writeI32(transmitter.getPosition());
        serializer.writeObject(transmitter.getTxMode());
        serializer.writeObject(transmitter.getTxStatus());
    }

    /**
     * Deserialize with no group info
     * 
     * @param deserializer
     *            The derserializer
     * @return
     * @throws SerializationException
     */
    public static Transmitter deserializeNoGroup(
            IDeserializationContext deserializer) throws SerializationException {
        Transmitter t = new Transmitter();
        t.setCallSign(deserializer.readString());
        t.setFipsCode((String) deserializer.readObject());
        t.setLocation(deserializer.readString());
        t.setMnemonic(deserializer.readString());
        t.setServiceArea(deserializer.readString());
        t.setDacPort((Integer) deserializer.readObject());
        t.setFrequency(deserializer.readFloat());
        t.setId(deserializer.readI32());
        t.setPosition(deserializer.readI32());
        t.setTxMode((TxMode) deserializer.readObject());
        t.setTxStatus((TxStatus) deserializer.readObject());

        return t;
    }
}
