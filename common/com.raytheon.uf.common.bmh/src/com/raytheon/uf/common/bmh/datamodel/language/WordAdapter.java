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
package com.raytheon.uf.common.bmh.datamodel.language;

import com.raytheon.uf.common.serialization.IDeserializationContext;
import com.raytheon.uf.common.serialization.ISerializationContext;
import com.raytheon.uf.common.serialization.ISerializationTypeAdapter;
import com.raytheon.uf.common.serialization.SerializationException;

/**
 * Serialization Adapter to fix circular reference between words and
 * dictionaries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 5, 2014  3175      rjpeter     Initial creation
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
public class WordAdapter implements ISerializationTypeAdapter<Word> {

    @Override
    public void serialize(ISerializationContext serializer, Word word)
            throws SerializationException {
        serializeNoDict(serializer, word);
        DictionaryAdapter.serializeNoWord(serializer, word.getDictionary());
    }

    @Override
    public Word deserialize(IDeserializationContext deserializer)
            throws SerializationException {
        Word w = deserializeNoDict(deserializer);
        Dictionary d = DictionaryAdapter.deserializeNoWord(deserializer);
        w.setDictionary(d);
        // Note: dictionary specifically not populated with a set for Word since
        // we don't have the full list
        return w;
    }

    public static void serializeNoDict(ISerializationContext serializer,
            Word word) throws SerializationException {
        serializer.writeI32(word.getId());
        serializer.writeString(word.getWord());
        serializer.writeString(word.getSubstitute());
    }

    public static Word deserializeNoDict(IDeserializationContext deserializer)
            throws SerializationException {
        Word w = new Word();
        w.setId(deserializer.readI32());
        w.setWord(deserializer.readString());
        w.setSubstitute(deserializer.readString());
        return w;
    }
}
