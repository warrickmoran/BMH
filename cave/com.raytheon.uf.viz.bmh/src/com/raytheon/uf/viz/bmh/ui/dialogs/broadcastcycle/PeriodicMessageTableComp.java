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
package com.raytheon.uf.viz.bmh.ui.dialogs.broadcastcycle;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.viz.bmh.ui.common.table.TableComp;

/**
 * Periodic Message dialog's table composite.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 18, 2014   3432     mpduff      Initial creation
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class PeriodicMessageTableComp extends TableComp {

    public PeriodicMessageTableComp(Composite parent, int tableStyle,
            boolean displayLines, boolean displayHeader) {
        super(parent, tableStyle, displayLines, displayHeader);
    }

    @Override
    protected void handleTableMouseClick(MouseEvent event) {
        // no-op
    }

    @Override
    protected void handleTableSelection(SelectionEvent e) {
        // no-op
    }
}
