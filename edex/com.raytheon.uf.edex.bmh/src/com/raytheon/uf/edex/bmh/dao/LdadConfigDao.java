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
package com.raytheon.uf.edex.bmh.dao;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import com.raytheon.uf.common.bmh.audio.BMHAudioFormat;
import com.raytheon.uf.common.bmh.datamodel.transmitter.LdadConfig;

/**
 * BMH Dao for {@link LdadConfig}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 11, 2014 3803       bkowal      Initial creation
 * Nov 13, 2014 3803       bkowal      Added getLdadConfigByName. Fixed
 *                                     selectConfigReferences.
 * Nov 19, 2014 3385       bkowal      Added {@link LdadConfigDao#getLdadConfigsForMsgType(int)}
 * Jan 07, 2015 3958       bkowal      Return an empty list when no {@link LdadConfig}s are found.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class LdadConfigDao extends AbstractBMHDao<LdadConfig, Long> {
    public LdadConfigDao() {
        super(LdadConfig.class);
    }

    public LdadConfigDao(boolean operational) {
        super(operational, LdadConfig.class);
    }

    public List<LdadConfig> selectConfigReferences() {
        List<?> ldadConfigs = super
                .findByNamedQuery(LdadConfig.SELECT_LDAD_CONFIG_REFERENCES);
        if (ldadConfigs == null || ldadConfigs.isEmpty()) {
            return Collections.emptyList();
        }

        List<LdadConfig> ldadConfigurations = new ArrayList<>(
                ldadConfigs.size());
        for (Object object : ldadConfigs) {
            if (object instanceof Object[] == false) {
                logger.error("The "
                        + LdadConfig.SELECT_LDAD_CONFIG_REFERENCES
                        + " query returned results in the wrong format. Expected an array of Object.");
                continue;
            }

            Object[] objects = (Object[]) object;
            if (objects.length != 5) {
                logger.error("The "
                        + LdadConfig.SELECT_LDAD_CONFIG_REFERENCES
                        + " query returned results in the wrong format. Expected an array of Object with 5 elements.");
                continue;
            }

            if (objects[0] instanceof Long == false
                    || objects[1] instanceof String == false
                    || objects[2] instanceof String == false
                    || objects[3] instanceof String == false
                    || objects[4] instanceof BMHAudioFormat == false) {
                logger.error("The "
                        + LdadConfig.SELECT_LDAD_CONFIG_REFERENCES
                        + " query returned results in the wrong format. Expected the object array to have objects of type: [Long, String, String, String, BMHAudioFormat].");
                continue;
            }

            LdadConfig ldadConfig = new LdadConfig();
            ldadConfig.setId((Long) objects[0]);
            ldadConfig.setName((String) objects[1]);
            ldadConfig.setHost((String) objects[2]);
            ldadConfig.setDirectory((String) objects[3]);
            ldadConfig.setEncoding((BMHAudioFormat) objects[4]);

            ldadConfigurations.add(ldadConfig);
        }

        return ldadConfigurations;
    }

    public LdadConfig getLdadConfigByName(final String name) {
        List<?> ldadConfigs = super.findByNamedQueryAndNamedParam(
                LdadConfig.SELECT_LDAD_CONFIG_BY_NAME, new String[] { "name" },
                new String[] { name });
        if (ldadConfigs == null || ldadConfigs.isEmpty()) {
            return null;
        }

        if (ldadConfigs.get(0) instanceof LdadConfig) {
            return (LdadConfig) ldadConfigs.get(0);
        }

        return null;
    }

    public List<LdadConfig> getLdadConfigsForMsgType(final String afosid) {
        List<?> ldadConfigObjects = super.findByNamedQueryAndNamedParam(
                LdadConfig.SELECT_LDAD_CONFIG_BY_MSG_TYPE,
                new String[] { "afosid" }, new Object[] { afosid });
        if (ldadConfigObjects == null || ldadConfigObjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<LdadConfig> ldadConfigs = new ArrayList<>(ldadConfigObjects.size());
        for (Object object : ldadConfigObjects) {
            if (object instanceof LdadConfig == false) {
                StringBuilder errMsg = new StringBuilder("The ");
                errMsg.append(LdadConfig.SELECT_LDAD_CONFIG_BY_NAME);
                errMsg.append(" returned results in the wrong format. Expected an object of type ");
                errMsg.append(LdadConfig.class.getName());
                errMsg.append("; received an object of type ");
                errMsg.append(object.getClass().getName());
                errMsg.append(".");
                logger.error(errMsg.toString());

                return Collections.emptyList();
            }
            ldadConfigs.add((LdadConfig) object);
        }

        return ldadConfigs;
    }
}