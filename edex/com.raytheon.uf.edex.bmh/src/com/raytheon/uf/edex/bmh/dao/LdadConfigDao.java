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
        List<?> objects = super
                .findByNamedQuery(LdadConfig.SELECT_LDAD_CONFIG_REFERENCES);
        if (objects == null || objects.isEmpty()) {
            return Collections.emptyList();
        }

        List<LdadConfig> ldadConfigurations = new ArrayList<>(objects.size());
        for (Object object : objects) {
            if (object instanceof LdadConfig) {
                ldadConfigurations.add((LdadConfig) object);
            }
        }

        return ldadConfigurations;
    }
}