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
package com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.viz.bmh.ui.common.table.TableComp;

/**
 * Table to display the message types.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 14, 2014  #3330      lvenable     Initial creation
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class MsgTypeTable extends TableComp {

    public MsgTypeTable(Composite parentComp, int width, int height) {
        super(parentComp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI,
                width, height);
    }

    @Override
    protected void handleTableMouseClick(MouseEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void handleTableSelection(SelectionEvent e) {
        if (callbackAction != null) {
            callbackAction.tableSelectionChange(table.getSelectionCount());
        }
    }
}
