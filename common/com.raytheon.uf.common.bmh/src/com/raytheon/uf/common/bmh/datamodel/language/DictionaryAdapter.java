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

import java.util.HashSet;
import java.util.Set;

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
public class DictionaryAdapter implements ISerializationTypeAdapter<Dictionary> {

    @Override
    public void serialize(ISerializationContext serializer, Dictionary dict)
            throws SerializationException {
        serializeNoWord(serializer, dict);
        Set<Word> words = dict.getWords();
        if (words != null) {
            int size = words.size();
            serializer.writeI32(size);
            for (Word w : words) {
                WordAdapter.serializeNoDict(serializer, w);
            }
        } else {
            serializer.writeI32(0);
        }
    }

    @Override
    public Dictionary deserialize(IDeserializationContext deserializer)
            throws SerializationException {
        Dictionary d = deserializeNoWord(deserializer);
        int size = deserializer.readI32();
        if (size > 0) {
            Set<Word> words = new HashSet<>(size, 1);

            for (int i = 0; i < size; i++) {
                Word w = WordAdapter.deserializeNoDict(deserializer);
                words.add(w);
            }

            d.setWords(words);
        }

        return d;
    }

    public static void serializeNoWord(ISerializationContext serializer,
            Dictionary dict) throws SerializationException {
        serializer.writeString(dict.getName());
        serializer.writeObject(dict.getLanguage());
    }

    public static Dictionary deserializeNoWord(
            IDeserializationContext deserializer) throws SerializationException {
        Dictionary dict = new Dictionary();
        dict.setName(deserializer.readString());
        dict.setLanguage((Language) deserializer.readObject());
        return dict;
    }
}
