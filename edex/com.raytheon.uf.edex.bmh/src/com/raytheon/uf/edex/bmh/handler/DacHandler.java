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
import com.raytheon.uf.common.bmh.request.DacRequest;
import com.raytheon.uf.common.bmh.request.DacResponse;
import com.raytheon.uf.edex.bmh.BmhMessageProducer;
import com.raytheon.uf.edex.bmh.dao.DacChannelDao;
import com.raytheon.uf.edex.bmh.dao.DacDao;

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
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class DacHandler extends AbstractBMHServerRequestHandler<DacRequest> {

    @Override
    public Object handleRequest(DacRequest request) throws Exception {
        DacResponse response = new DacResponse();

        switch (request.getAction()) {
        case GetAllDacs:
            response = getAllDacs(request);
            break;
        case SaveDac:
            response = saveDac(request);
            BmhMessageProducer.sendConfigMessage(new DacConfigNotification(
                    ConfigChangeType.Update, request.getDac(), request),
                    request.isOperational());
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
