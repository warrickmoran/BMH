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
package com.raytheon.uf.viz.bmh.ui.common.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * A generic table class that doesn't add functionality outside the abstract
 * TableComp class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 17, 2014 3631/3611  lvenable    Initial creation while working DR3611.  This code
 *                                     will be checked in under 3611.
 * Oct 09, 2014 3646       rferrel     Constructors without size arguments.
 * Apr 04, 2016 5504       bkowal      Updated for compatibility with {@link TableComp}.
 * 
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */

public class GenericTable extends TableComp {

    private final static int DEFAULT_STYLE = SWT.BORDER | SWT.V_SCROLL
            | SWT.H_SCROLL | SWT.MULTI;

    /**
     * Constructor.
     * 
     * @param parentComp
     *            Parent composite.
     */
    public GenericTable(Composite parentComp, int desiredNumRows) {
        this(parentComp, DEFAULT_STYLE, desiredNumRows);
    }

    /**
     * Constructor.
     * 
     * @param parentComp
     * @param tableStyle
     */
    public GenericTable(Composite parentComp, int tableStyle, int desiredNumRows) {
        super(parentComp, tableStyle, desiredNumRows);
    }

    public GenericTable(Composite parentComp, int tableStyle,
            int desiredNumRows, int estimatedCharacterCount) {
        super(parentComp, tableStyle, desiredNumRows, estimatedCharacterCount);
    }

    @Override
    protected void handleTableMouseClick(MouseEvent event) {
        // Not used.
    }

    @Override
    protected void handleTableSelection(SelectionEvent e) {
        if (callbackAction != null) {
            callbackAction.tableSelectionChange(table.getSelectionCount());
        }
    }
}
