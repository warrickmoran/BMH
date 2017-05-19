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
package com.raytheon.uf.viz.bmh.ui.dialogs.wxmessages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.bmh.datamodel.msg.InputMessage;
import com.raytheon.uf.common.bmh.request.InputMessageRequest;
import com.raytheon.uf.common.bmh.request.InputMessageRequest.InputMessageAction;
import com.raytheon.uf.common.bmh.request.InputMessageResponse;
import com.raytheon.uf.common.bmh.same.SAMEToneTextBuilder;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.bmh.data.BmhUtils;
import com.raytheon.uf.viz.bmh.ui.common.table.GenericTable;
import com.raytheon.uf.viz.bmh.ui.common.table.ITableActionCB;
import com.raytheon.uf.viz.bmh.ui.common.table.TableCellData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableColumnData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableData;
import com.raytheon.uf.viz.bmh.ui.common.table.TableRowData;
import com.raytheon.uf.viz.bmh.ui.common.utility.FilterComp;
import com.raytheon.uf.viz.bmh.ui.common.utility.FilterComp.DateFilterChoice;
import com.raytheon.uf.viz.bmh.ui.common.utility.FilterComp.TextFilterChoice;
import com.raytheon.uf.viz.bmh.ui.common.utility.FilterData;
import com.raytheon.uf.viz.bmh.ui.common.utility.IFilterAction;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.MessageTypeDataManager;
import com.raytheon.uf.viz.bmh.ui.dialogs.msgtypes.SelectMessageTypeDlg;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to select input messages.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 16, 2014  #3728      lvenable    Initial creation
 * Oct 18, 2014  #3728      lvenable    Hooked into database and returns the selected
 *                                      input message.
 * Oct 23, 2014  #3728      lvenable    Updated to not display input messages for time
 *                                      announcement message types.
 * Oct 24, 2014  #3478      bkowal      Updated to retrieve a InputMessageAudioResponse.
 * Oct 26, 2014   #3728     lvenable    Updated to use new data object.
 * Nov 02, 2014   3785      mpduff      Set the Validated Message on inputAudioMessageData
 * Nov 03, 2014   3790      lvenable    Added Active to the table column and made the dialog
 *                                      resizable.
 * Nov 15, 2014   3832      mpduff      Make creation hours on the 24 hr clock
 * Dec 02, 2014   3877      lvenable    Added null checks.
 * Jan 02, 2014   3833      lvenable    Added filtering capability.
 * Feb 10, 2015   4085      bkowal      Filter out input messages associated with
 *                                      static message types.
 * Feb 12, 2015   4113      bkowal      Dialog will now return a {@link InputMessageSequence}.
 * May 20, 2015   4490      bkowal      Filter out input messages associated with demo message types.
 * Jun 12, 2015   4482      rjpeter     Fixed NPE.
 * Jul 14, 2015   4162      rferrel     The filterAction combines text and time filtering.
 * Apr 04, 2016   5504      bkowal      Fix GUI sizing issues.
 * May 17, 2017  19315      xwei        Added an extra column for Effective Time
 * </pre>
 * 
 * @author lvenable
 * @version 1.0
 */
public class SelectInputMsgDlg extends CaveSWTDialog implements IFilterAction {

    /** Status handler for reporting errors. */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SelectMessageTypeDlg.class);

    /** Table containing the emergency message types. */
    private GenericTable inputMsgTable;

    /** OK button. */
    private Button okBtn;

    /** Message Type table data. */
    private TableData inputMsgTableData = null;

    /** List of all the input messages. */
    private List<InputMessage> allInputMessages = null;

    /** List of input messages that have been filtered. */
    private List<InputMessage> filteredInputMessages = new ArrayList<>();

    /** Date format. */
    private final SimpleDateFormat dateFmt = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    /**
     * Constructor.
     * 
     * @param parentShell
     */
    public SelectInputMsgDlg(Shell parentShell) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.PRIMARY_MODAL,
                CAVE.DO_NOT_BLOCK | CAVE.PERSPECTIVE_INDEPENDENT);

    }

    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);

        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        return gd;
    }

    @Override
    protected void opened() {
        shell.setMinimumSize(shell.getSize());
    }

    @Override
    protected void initializeComponents(Shell shell) {
        dateFmt.setTimeZone(TimeUtil.GMT_TIME_ZONE);

        setText("Select Input Message");

        createFilterControls();
        createInputMsgTable();
        createOkCancelButtons();

        retrieveUnexpiredInputMessages();
        populateInputMsgTable();

        if (inputMsgTable.getItemCount() > 0) {
            okBtn.setEnabled(true);
        }
    }

    /**
     * Create the filtering controls.
     */
    private void createFilterControls() {
        new FilterComp(shell, true, dateFmt, this);
    }

    /**
     * Create the input message table.
     */
    private void createInputMsgTable() {

        final Button allInputMsgsChk = new Button(shell, SWT.CHECK);
        allInputMsgsChk
                .setText("Display all input messages (including expired)");
        allInputMsgsChk.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (allInputMsgsChk.getSelection() == true) {
                    retrieveAllInputMessages();
                } else {
                    retrieveUnexpiredInputMessages();
                }
                populateInputMsgTable();
            }
        });

        Group inputTableGroup = new Group(shell, SWT.SHADOW_OUT);
        GridLayout gl = new GridLayout(1, false);
        inputTableGroup.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        inputTableGroup.setLayoutData(gd);
        inputTableGroup.setText(" Input Messages: ");

        int tableStyle = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE;

        inputMsgTable = new GenericTable(inputTableGroup, tableStyle, 12);

        inputMsgTable.setCallbackAction(new ITableActionCB() {
            @Override
            public void tableSelectionChange(int selectionCount) {
                okBtn.setEnabled(selectionCount > 0);
            }
        });
    }

    /**
     * Create the OK and Cancel buttons.
     */
    private void createOkCancelButtons() {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(new GridLayout(2, true));
        buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        int buttonWidth = 80;
        GridData gd = new GridData(SWT.RIGHT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        okBtn = new Button(buttonComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setEnabled(false);
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleOkAction();
            }
        });

        gd = new GridData(SWT.LEFT, SWT.DEFAULT, true, false);
        gd.widthHint = buttonWidth;
        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });
    }

    /**
     * Handle the OK button action.
     */
    private void handleOkAction() {
        int index = inputMsgTable.getSelectedIndex();

        if (index < 0) {
            return;
        }

        /*
         * add input message sequencing information to the returned data.
         */
        int[] messageSequence = new int[this.filteredInputMessages.size()];
        for (int i = 0; i < this.filteredInputMessages.size(); i++) {
            messageSequence[i] = this.filteredInputMessages.get(i).getId();
        }
        InputMessageSequence inputMsgSequence = new InputMessageSequence(index,
                messageSequence);

        setReturnValue(inputMsgSequence);

        close();
    }

    /**
     * Populate the message type table.
     */
    private void populateInputMsgTable() {
        if (inputMsgTable.hasSelectedItems() == false) {
            List<TableColumnData> columnNames = new ArrayList<TableColumnData>();
            TableColumnData tcd = new TableColumnData("Name", 250);
            columnNames.add(tcd);
            tcd = new TableColumnData("AFOS ID", 100);
            columnNames.add(tcd);
            tcd = new TableColumnData("Active", 90);
            columnNames.add(tcd);
            tcd = new TableColumnData("Creation Date");
            columnNames.add(tcd);

            tcd = new TableColumnData("Effective Date");    
            columnNames.add(tcd);                       

            inputMsgTableData = new TableData(columnNames);
        } else {
            inputMsgTableData.deleteAllRows();
        }

        populateInputMsgTableData();

        inputMsgTable.populateTable(inputMsgTableData);

        if (inputMsgTable.getItemCount() > 0) {
            inputMsgTable.select(0);
            okBtn.setEnabled(true);
        } else {
            okBtn.setEnabled(false);
        }
    }

    /**
     * Populate the message type table data.
     */
    private void populateInputMsgTableData() {

        for (InputMessage im : filteredInputMessages) {

            TableRowData trd = new TableRowData();

            if (im.getName() == null) {
                trd.addTableCellData(new TableCellData("??????"));
            } else {
                trd.addTableCellData(new TableCellData(im.getName()));
            }

            if (im.getAfosid() == null) {
                trd.addTableCellData(new TableCellData("????"));
            } else {
                trd.addTableCellData(new TableCellData(im.getAfosid()));
            }

            if (im.getActive() == null) {
                trd.addTableCellData(new TableCellData("???"));
            } else {
                trd.addTableCellData(new TableCellData((im.getActive()) ? "Yes"
                        : "No"));
            }

            if (im.getCreationTime() == null) {
                trd.addTableCellData(new TableCellData("??????"));
            } else {
                trd.addTableCellData(new TableCellData(dateFmt.format(im
                        .getCreationTime().getTime())));
            }

            if (im.getEffectiveTime() == null) {
                trd.addTableCellData(new TableCellData("??????"));
            } else {
                trd.addTableCellData(new TableCellData(dateFmt.format(im
                        .getEffectiveTime().getTime())));
            }
            
            inputMsgTableData.addDataRow(trd);
        }
    }

    /**
     * Retrieve the Input Messages from the database with only the ID, Name,
     * Afos Id, and Creation times populate.
     */
    private void retrieveAllInputMessages() {

        InputMessageRequest imRequest = new InputMessageRequest();
        imRequest.setAction(InputMessageAction.ListIdNameAfosCreationActive);
        InputMessageResponse imResponse = null;
        List<InputMessage> tmpInputMsgList = null;

        try {
            imResponse = (InputMessageResponse) BmhUtils.sendRequest(imRequest);
            tmpInputMsgList = imResponse.getInputMessageList();

            if (tmpInputMsgList == null) {
                allInputMessages = Collections.emptyList();
                filteredInputMessages = Collections.emptyList();
                return;
            }

        } catch (Exception e) {
            statusHandler.error(
                    "Error retrieving all input messages from the database: ",
                    e);
            allInputMessages = Collections.emptyList();
            filteredInputMessages = Collections.emptyList();
            return;
        }

        addMessagesToLists(tmpInputMsgList);
    }

    /**
     * Retrieve the unexpired input messages.
     */
    private void retrieveUnexpiredInputMessages() {

        InputMessageRequest imRequest = new InputMessageRequest();
        imRequest.setAction(InputMessageAction.UnexpiredMessages);
        imRequest.setTime(TimeUtil.newGmtCalendar());
        InputMessageResponse imResponse = null;
        List<InputMessage> tmpInputMsgList = null;

        try {
            imResponse = (InputMessageResponse) BmhUtils.sendRequest(imRequest);
            tmpInputMsgList = imResponse.getInputMessageList();

            if (tmpInputMsgList == null) {
                allInputMessages = Collections.emptyList();
                filteredInputMessages = Collections.emptyList();
                return;
            }

        } catch (Exception e) {
            statusHandler
                    .error("Error retrieving unexpired input messages from the database: ",
                            e);
            allInputMessages = Collections.emptyList();
            filteredInputMessages = Collections.emptyList();
            return;
        }

        addMessagesToLists(tmpInputMsgList);
    }

    /**
     * Add the message to the filtered list and all message list that are not
     * time announcement messages.
     * 
     * @param tmpInputMsgList
     *            Temporary list of input messages.
     */
    private void addMessagesToLists(List<InputMessage> tmpInputMsgList) {
        final MessageTypeDataManager mtdm = new MessageTypeDataManager();
        final Set<String> staticAfosIds;
        try {
            staticAfosIds = mtdm.getStaticMessageAfosIds();
        } catch (Exception e) {
            statusHandler
                    .error("Failed to retrieve the afos ids associated with static message types.",
                            e);
            return;
        }

        Iterator<InputMessage> it = tmpInputMsgList.iterator();
        while (it.hasNext()) {
            final String afosId = it.next().getAfosid();
            /*
             * Also filter demo messages. They do not have a unique designation,
             * so they must be filtered using inspection of the afos id.
             */
            if (staticAfosIds.contains(afosId)
                    || (afosId != null && afosId.length() >= 6 && SAMEToneTextBuilder.DEMO_EVENT
                            .equals(afosId.substring(3, 6)))) {
                it.remove();
            }
        }

        allInputMessages = new ArrayList<InputMessage>(tmpInputMsgList);

        filteredInputMessages.clear();
        filteredInputMessages.addAll(allInputMessages);
    }

    @Override
    public void clearFilter() {
        filteredInputMessages.clear();
        filteredInputMessages.addAll(allInputMessages);

        populateInputMsgTable();
    }

    @Override
    public void filterAction(FilterData filterData) {

        /*
         * If we don't filter on the text or date then repopulate the table with
         * all of the items. Usually this can't happen but this is more of a
         * safety check.
         */
        if (filterData.filterOnText() == false
                && filterData.filterOnDate() == false) {
            filteredInputMessages.clear();
            filteredInputMessages.addAll(allInputMessages);

            populateInputMsgTable();

            return;
        }

        Set<Integer> matchingIndexes = new TreeSet<>();
        InputMessage im = null;

        for (int i = 0; i < allInputMessages.size(); i++) {
            im = allInputMessages.get(i);
            if (filterData.filterOnText() == true) {
                if (matchesTextFilter(filterData, im.getName())
                        && matchesDateFilter(filterData, im)) {
                    matchingIndexes.add(i);
                } else if (matchesTextFilter(filterData, im.getAfosid())
                        && matchesDateFilter(filterData, im)) {
                    matchingIndexes.add(i);
                } else if (matchesTextFilter(filterData,
                        (im.getActive() ? " Yes" : "No"))
                        && matchesDateFilter(filterData, im)) {
                    matchingIndexes.add(i);
                }
            } else if (matchesDateFilter(filterData, im)) {
                matchingIndexes.add(i);
            }
        }

        filteredInputMessages.clear();

        for (Integer i : matchingIndexes) {
            filteredInputMessages.add(allInputMessages.get(i));
        }

        populateInputMsgTable();

    }

    /**
     * Check if the text matches the filter.
     * 
     * @param filterData
     *            Filter data.
     * @param text
     *            Text to check against the filter.
     * @return True if the text matches the filter criteria.
     */
    private boolean matchesTextFilter(FilterData filterData, String text) {

        if (text == null) {
            return false;
        }

        if (filterData.getTextFilterChoice() == TextFilterChoice.STARTS_WITH) {
            if (filterData.isCaseSensitive()) {
                if (text.startsWith(filterData.getFilterText())) {
                    return true;
                }
            } else {
                if (text.toLowerCase().startsWith(
                        filterData.getFilterText().toLowerCase())) {
                    return true;
                }
            }
        } else if (filterData.getTextFilterChoice() == TextFilterChoice.ENDS_WITH) {
            if (filterData.isCaseSensitive()) {
                if (text.endsWith(filterData.getFilterText())) {
                    return true;
                }
            } else {
                if (text.toLowerCase().endsWith(
                        filterData.getFilterText().toLowerCase())) {
                    return true;
                }
            }
        } else if (filterData.getTextFilterChoice() == TextFilterChoice.CONTAINS) {
            if (filterData.isCaseSensitive()) {
                if (text.contains(filterData.getFilterText())) {
                    return true;
                }
            } else {
                if (text.toLowerCase().contains(
                        filterData.getFilterText().toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the date matches the filter.
     * 
     * @param filterData
     *            Filter data.
     * @param date
     *            Date.
     * @return True if the date matches the filter criteria.
     */
    private boolean matchesDateFilter(FilterData filterData, InputMessage im) {

        if (filterData.getDateFilterChoice() == DateFilterChoice.ALL) {
            return true;
        }

        if (im.getCreationTime() == null) {
            return false;
        }

        Date date = im.getCreationTime().getTime();

        if (filterData.getDateFilterChoice() == DateFilterChoice.AFTER) {
            if (date.after(filterData.getStartDate())) {
                return true;
            }
        } else if (filterData.getDateFilterChoice() == DateFilterChoice.BEFORE) {
            if (date.before(filterData.getStartDate())) {
                return true;
            }
        } else if (filterData.getDateFilterChoice() == DateFilterChoice.RANGE) {
            if (date.after(filterData.getStartDate())
                    && date.before(filterData.getEndDate())) {
                return true;
            }
        }

        return false;
    }
}
