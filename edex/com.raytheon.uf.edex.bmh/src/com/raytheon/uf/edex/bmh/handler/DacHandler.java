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
package com.raytheon.uf.edex.bmh.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.raytheon.uf.common.bmh.datamodel.dac.Dac;
import com.raytheon.uf.common.bmh.notify.config.ConfigNotification.ConfigChangeType;
import com.raytheon.uf.common.bmh.notify.config.DacConfigNotification;
import com.raytheon.uf.common.bmh.request.DacConfigRequest;
import com.raytheon.uf.common.bmh.request.DacConfigResponse;
import com.raytheon.uf.common.bmh.request.DacRequest;
import com.raytheon.uf.common.bmh.request.DacResponse;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dac.OrionPost;
import com.raytheon.uf.edex.bmh.dao.DacChannelDao;
import com.raytheon.uf.edex.bmh.dao.DacDao;
import com.raytheon.uf.edex.bmh.status.BMHStatusHandler;
import com.raytheon.uf.edex.bmh.status.IBMHStatusHandler;

/**
 * Handles any requests to get or modify the state of {@link Dac}s
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Aug 27, 2014  3137     mpduff      Initial creation
 * Oct 07, 2014  3687     bsteffen    Handle non-operational requests.
 * Oct 13, 2014  3413     rferrel     Implement User roles.
 * Oct 19, 2014  3699     mpduff      Added save and delete actions
 * Oct 22, 2014  3687     bsteffen    Send notifications.
 * May 28, 2015  4429     rjpeter     Add ITraceable
 * Nov 09, 2015  5113     bkowal      Added {@link #validateUnique(DacRequest)}.
 * Nov 12, 2015  5113     bkowal      Handle {@link DacConfigRequest}s.
 * Nov 23, 2015  5113     bkowal      Maintain sync with DAC sync information.
 * May 09, 2016  5630     rjpeter     Remove DAC Sync.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DacHandler extends AbstractBMHServerRequestHandler<DacRequest> {

    private static final IBMHStatusHandler statusHandler = BMHStatusHandler
            .getInstance(DacHandler.class);

    @Override
    public Object handleRequest(DacRequest request) throws Exception {
        DacResponse response = new DacResponse();

        switch (request.getAction()) {
        case GetAllDacs:
            response = getAllDacs(request);
            break;
        case SaveDac:
            if (request instanceof DacConfigRequest) {
                DacConfigResponse configResponse = configureSaveDac((DacConfigRequest) request);
                if (configResponse.isSuccess() && (request.getDac() != null)) {
                    BmhMessageProducer.sendConfigMessage(
                            new DacConfigNotification(ConfigChangeType.Update,
                                    request.getDac(), request), request
                                    .isOperational());
                }
                return configResponse;
            } else {
                response = saveDac(request);
                BmhMessageProducer.sendConfigMessage(new DacConfigNotification(
                        ConfigChangeType.Update, request.getDac(), request),
                        request.isOperational());
            }
            break;
        case DeleteDac:
            deleteDac(request);
            BmhMessageProducer.sendConfigMessage(new DacConfigNotification(
                    ConfigChangeType.Delete, request.getDac(), request),
                    request.isOperational());
            break;
        case ValidateUnique:
            response = validateUnique(request);
            break;
        default:
            throw new UnsupportedOperationException(this.getClass()
                    .getSimpleName()
                    + " cannot handle action "
                    + request.getAction());
        }

        return response;
    }

    private DacResponse getAllDacs(DacRequest request) {
        DacResponse response = new DacResponse();
        DacDao dao = new DacDao(request.isOperational());
        List<Dac> dacList = dao.getAll();

        response.setDacList(dacList);
        return response;
    }

    private DacResponse saveDac(DacRequest request) {
        DacResponse response = new DacResponse();
        DacDao dao = new DacDao(request.isOperational());
        Dac dac = request.getDac();
        dao.saveOrUpdate(dac);
        response.addDac(dac);

        return response;
    }

    private DacConfigResponse configureSaveDac(DacConfigRequest request)
            throws Exception {
        if (request.isOperational() == false) {
            throw new IllegalStateException(
                    "A DAC cannot be configured in PRACTICE mode.");
        }

        if (request.getDac() == null) {
            statusHandler.info("Handling request to reboot DAC at IP: "
                    + request.getConfigAddress() + " ...");
        } else {
            final String action = "configure";
            final String address = request.getConfigAddress();
            statusHandler.info("Handling request to " + action + " DAC: "
                    + request.getDac().getName() + " at IP: " + address
                    + " ...");
        }

        final OrionPost orionPost = new OrionPost(request.getDac(),
                request.getConfigAddress());
        DacConfigResponse response = new DacConfigResponse();
        response.setSuccess(orionPost.configureDAC(request.isReboot()));
        response.setEvents(orionPost.getEvents());
        if (response.isSuccess() && (request.getDac() != null)) {
            /*
             * Persist the {@link Dac} record to the database.
             */
            final DacResponse dacResponse = this.saveDac(request);
            response.setDac(dacResponse.getDacList().get(0));
        }

        return response;
    }

    private void deleteDac(DacRequest request) {
        DacDao dao = new DacDao(request.isOperational());
        dao.delete(request.getDac());
    }

    private DacResponse validateUnique(DacRequest request) {
        DacResponse response = new DacResponse();

        final Dac dac = request.getDac();
        DacDao dacDao = new DacDao(request.isOperational());
        DacChannelDao dacChannelDao = new DacChannelDao(request.isOperational());

        Set<Dac> conflictDacs = new HashSet<>();
        Dac conflictDac = dacDao.validateDacUniqueness(dac.getId(),
                dac.getName(), dac.getAddress(), dac.getReceiveAddress(),
                dac.getReceivePort());
        if (conflictDac != null) {
            conflictDacs.add(conflictDac);
        }
        conflictDacs.addAll(dacChannelDao.validateChannelsUniqueness(
                dac.getId(), new HashSet<Integer>(dac.getDataPorts())));
        response.setDacList(new ArrayList<>(conflictDacs));

        return response;
    }
}
