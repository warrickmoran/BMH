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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import com.raytheon.uf.common.bmh.BMH_CATEGORY;
import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.TransmitterGroup;
import com.raytheon.uf.edex.bmh.BMHConstants;
import com.raytheon.uf.edex.bmh.dao.DacDao;
import com.raytheon.uf.edex.bmh.dao.TransmitterGroupDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.core.IContextStateProcessor;

/**
 * 
 * Generate config file for the comms manager based off the current state of the
 * db.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 04, 2014  3486     bsteffen    Initial creation
 * Aug 18, 2014  3532     bkowal      Include the transmitter decibel range in
 *                                    the configuration.
 * Aug 27, 2014  3486     bsteffen    Make Comms Configurator more robust.
 * Sep 4, 2014   3532     bkowal      Replace the decibel range with the decibel
 *                                    target in the configuration.
 * Sep 09, 2014  3554     bsteffen    Inject daos to better handle startup ordering
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class CommsConfigurator implements IContextStateProcessor {

    protected static final BMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(CommsConfigurator.class);

    private DacDao dacDao;

    private TransmitterGroupDao transmitterGroupDao;

    public CommsConfig configure() {
        List<Dac> dacs = dacDao.getAll();
        Map<Integer, DacConfig> dacMap = createDacConfigs(dacs);
        populateChannels(dacMap);
        Map<String, DacConfig> prevDacMap = new HashMap<>();

        Path configPath = CommsConfig.getDefaultPath();
        CommsConfig prevConfig = null;
        if (Files.exists(configPath)) {
            try {
                prevConfig = JAXB.unmarshal(configPath.toFile(),
                        CommsConfig.class);
            } catch (DataBindingException e) {
                statusHandler.error(BMH_CATEGORY.COMMS_CONFIGURATOR_ERROR,
                        "Cannot load existing comms config file.", e);
            }
        } else {
            try {
                Files.createDirectories(configPath.getParent());
            } catch (IOException e) {
                statusHandler.error(BMH_CATEGORY.COMMS_CONFIGURATOR_ERROR,
                        "Cannot save comms config file.", e);
                return null;
            }
        }
        CommsConfig config = new CommsConfig();
        if (prevConfig != null) {
            config.setDacTransmitPort(prevConfig.getDacTransmitPort());
            config.setLineTapPort(prevConfig.getLineTapPort());
            config.setDacTransmitStarter(prevConfig.getDacTransmitStarter());
            if (prevConfig.getDacs() != null) {
                for (DacConfig dconf : prevConfig.getDacs()) {
                    prevDacMap.put(dconf.getIpAddress(), dconf);
                }
            }
        }
        assignPorts(dacs, dacMap, prevDacMap);
        config.setJmsConnection(BMHConstants
                .getJmsConnectionString("commsmanager"));
        if (!dacMap.isEmpty()) {
            config.setDacs(new HashSet<>(dacMap.values()));
        }
        if (prevConfig == null || !prevConfig.equals(config)) {
            if (dacMap.isEmpty()) {
                statusHandler.warn(BMH_CATEGORY.COMMS_CONFIGURATOR_ERROR,
                        "Writing comms conf file with no dacs.");
            }
            try {
                JAXB.marshal(config, configPath.toFile());
            } catch (DataBindingException e) {
                statusHandler.error(BMH_CATEGORY.COMMS_CONFIGURATOR_ERROR,
                        "Cannot save comms config file.", e);
            }
        }
        return config;
    }

    /**
     * For each dac make a dac config
     * 
     * @param dacs
     * @return map of dac rcs to the dac config for that dac.
     */
    protected Map<Integer, DacConfig> createDacConfigs(List<Dac> dacs) {
        Map<Integer, DacConfig> dacMap = new HashMap<>(dacs.size(), 1.0f);
        for (Dac dac : dacs) {
            DacConfig dconf = new DacConfig();
            dconf.setIpAddress(dac.getAddress());
            dconf.setReceivePort(dac.getReceivePort());
            dacMap.put(dac.getId(), dconf);
        }
        return dacMap;
    }

    /**
     * Process transmitter groups to create channels for each group on the dac.
     * 
     * @param dacMap
     */
    protected void populateChannels(Map<Integer, DacConfig> dacMap) {
        for (TransmitterGroup group : transmitterGroupDao.getAll()) {
            Set<Transmitter> transmitters = group.getEnabledTransmitters();
            if (transmitters.isEmpty()) {
                continue;
            }
            DacConfig dconf = dacMap.get(group.getDac());
            if (dconf == null) {
                statusHandler.warn(BMH_CATEGORY.COMMS_CONFIGURATOR_ERROR,
                        "No dac with rcs of " + group.getDac() + " for group "
                                + group.getName());
                continue;
            }
            DacChannelConfig channel = new DacChannelConfig();
            channel.setTransmitterGroup(group.getName());
            channel.setDbTarget(group.getAudioDBTarget());

            int[] radios = new int[transmitters.size()];
            int rindex = 0;
            for (Transmitter transmitter : transmitters) {
                radios[rindex] = transmitter.getDacPort();
                rindex += 1;
            }
            Arrays.sort(radios);
            channel.setRadios(radios);
            dconf.addChannel(channel);
        }
    }

    /**
     * Assign ports to each channel on the dac. Ports are first assigned to try
     * to match the previous configuration, then arbitrarily assigned to the
     * remaining channels.
     * 
     * @param dacs
     * @param dacMap
     * @param prevDacMap
     */
    protected void assignPorts(List<Dac> dacs, Map<Integer, DacConfig> dacMap,
            Map<String, DacConfig> prevDacMap) {
        for (Dac dac : dacs) {
            DacConfig dconfig = dacMap.get(dac.getId());
            List<DacChannelConfig> channels = dconfig.getChannels();
            if (channels == null || channels.isEmpty()
                    || dac.getDataPorts().isEmpty()) {
                dacMap.remove(dac.getId());
                continue;
            }
            List<Integer> availablePorts = new ArrayList<>(dac.getDataPorts());
            if (prevDacMap.containsKey(dconfig.getIpAddress())) {
                DacConfig prevConfig = prevDacMap.get(dconfig.getIpAddress());
                if (prevConfig.getChannels() == null) {
                    continue;
                }
                for (DacChannelConfig channel : channels) {
                    String group = channel.getTransmitterGroup();
                    for (DacChannelConfig prevChannel : prevConfig
                            .getChannels()) {
                        if (group.equals(prevChannel.getTransmitterGroup())) {
                            Integer port = prevChannel.getDataPort();
                            if (availablePorts.remove(port) == true) {
                                channel.setDataPort(port);
                            }
                            break;
                        }
                    }
                }
            }
            Iterator<DacChannelConfig> channelsIt = channels.iterator();
            while (channelsIt.hasNext()) {
                DacChannelConfig channel = channelsIt.next();
                if (channel.getDataPort() == 0) {
                    if (availablePorts.isEmpty()) {
                        statusHandler.warn(
                                BMH_CATEGORY.COMMS_CONFIGURATOR_ERROR,
                                "Not enough data ports for all channels for dac "
                                        + dac.getId());
                        channelsIt.remove();
                    } else {
                        channel.setDataPort(availablePorts.remove(0));
                    }
                }
            }
        }
    }

    public void setDacDao(DacDao dacDao) {
        this.dacDao = dacDao;
    }

    public void setTransmitterGroupDao(TransmitterGroupDao transmitterGroupDao) {
        this.transmitterGroupDao = transmitterGroupDao;
    }

    /**
     * Validate all DAOs are set correctly and throw an exception if any are not
     * set.
     * 
     * @throws IllegalStateException
     */
    private void validateDaos() throws IllegalStateException {
        if (dacDao == null) {
            throw new IllegalStateException(
                    "DacDao has not been set on the CommsConfigurator");
        } else if (transmitterGroupDao == null) {
            throw new IllegalStateException(
                    "TransmitterGroupDao has not been set on the CommsConfigurator");
        }
    }

    @Override
    public void preStart() {
        validateDaos();
        configure();
    }

    @Override
    public void postStart() {
        /* Required to implement IContextStateProcessor but not used. */
    }

    @Override
    public void preStop() {
        /* Required to implement IContextStateProcessor but not used. */
    }

    @Override
    public void postStop() {
        /* Required to implement IContextStateProcessor but not used. */
    }

}
