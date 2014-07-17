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
package com.raytheon.uf.viz.bmh.ui.dialogs.listening;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.raytheon.uf.common.bmh.StateXML;
import com.raytheon.uf.common.bmh.StatesXML;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Area;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Transmitter;
import com.raytheon.uf.common.bmh.datamodel.transmitter.Zone;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest;
import com.raytheon.uf.common.bmh.request.ZoneAreaRequest.ZoneAreaAction;
import com.raytheon.uf.common.bmh.request.ZoneAreaResponse;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

/**
 * Data access class for the Listening Area and Listening Zone dialogs
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 15, 2014    3406    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class ZonesAreasDataManager {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ZonesAreasDataManager.class);

    private final String STATE_FILE = "states.xml";

    /** JAXB context */
    private JAXBContext jax;

    /** Unmarshaller object */
    private Unmarshaller unmarshaller;

    /** Set of 2-letter State Abbreviations */
    private Set<String> statesSet;

    /**
     * Constructor.
     */
    public ZonesAreasDataManager() {
        Class[] classes = new Class[] { StatesXML.class, StateXML.class };

        try {
            jax = JAXBContext.newInstance(classes);
            this.unmarshaller = jax.createUnmarshaller();
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(
                    "Error creating context for SiteMap");
        }
    }

    /**
     * Get the {@link Zone}s defined in the bmh db.
     * 
     * @return List of defined zones
     * @throws VizException
     */
    public List<Zone> getZones() throws VizException {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.GetZones);

        ZoneAreaResponse response = (ZoneAreaResponse) ThriftClient
                .sendRequest(req);
        return response.getZoneList();
    }

    /**
     * Get the {@link Area}s defined in the bmh db.
     * 
     * @return List of defined areas
     * @throws VizException
     */
    public List<Area> getAreas() throws VizException {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.GetAreas);

        ZoneAreaResponse response = (ZoneAreaResponse) ThriftClient
                .sendRequest(req);
        return response.getAreaList();
    }

    /**
     * Get the {@link Transmitter}s defined in the bmh db.
     * 
     * @return List of defined transmitters
     * @throws VizException
     */
    public List<Transmitter> getTransmitters() throws VizException {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.GetTransmitters);

        ZoneAreaResponse response = (ZoneAreaResponse) ThriftClient
                .sendRequest(req);
        return response.getTransmitterList();
    }

    /**
     * Persist the {@link Zone} to the bmh db.
     * 
     * @throws VizException
     */
    public ZoneAreaResponse saveZone(Zone z) throws VizException {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.SaveZones);
        req.addZone(z);
        ZoneAreaResponse response = (ZoneAreaResponse) ThriftClient
                .sendRequest(req);

        return response;
    }

    /**
     * Persist the {@link Area} to the bmh db.
     * 
     * @throws VizException
     */
    public ZoneAreaResponse saveArea(Area a) throws VizException {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.SaveAreas);
        req.addArea(a);
        ZoneAreaResponse response = (ZoneAreaResponse) ThriftClient
                .sendRequest(req);

        return response;
    }

    /**
     * Delete the {@link Area} from the bmh db.
     * 
     * @throws VizException
     */
    public void deleteArea(Area a) throws VizException {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.DeleteArea);
        req.addArea(a);
        ThriftClient.sendRequest(req);
    }

    /**
     * Delete the {@link Zoine} from the bmh db.
     * 
     * @throws VizException
     */
    public void deleteZone(Zone z) throws VizException {
        ZoneAreaRequest req = new ZoneAreaRequest();
        req.setAction(ZoneAreaAction.DeleteZone);
        req.addZone(z);
        ThriftClient.sendRequest(req);
    }

    public Set<String> getStateAbbreviations() {
        if (statesSet == null) {
            IPathManager pathMgr = PathManagerFactory.getPathManager();
            LocalizationContext lc = pathMgr.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
            File file = pathMgr.getFile(lc, STATE_FILE);

            if (file != null && file.exists()) {
                try {
                    StatesXML statesXml = (StatesXML) unmarshaller
                            .unmarshal(file);
                    List<StateXML> stateList = statesXml.getStates();
                    statesSet = new HashSet<String>(stateList.size());
                    for (StateXML state : stateList) {
                        statesSet.add(state.getAbbreviation());
                    }
                } catch (JAXBException e) {
                    statusHandler.error("Error reading state file.", e);
                }
            }
        }

        return statesSet;
    }
}
