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
package com.raytheon.uf.common.bmh.datamodel.msg;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Suite object. Contains the priority of the suite and the list of message
 * types that belong to the suite.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * May 30, 2014  3175     rjpeter     Initial creation
 * Jul 10, 2014  3283     bsteffen    Eagerly fetch suites.
 * 
 * 
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@Entity
@DynamicSerialize
@Table(name = "suite", schema = "bmh", uniqueConstraints = { @UniqueConstraint(columnNames = {
        "programName", "suiteType", "programPosition" }) })
public class Suite {
    public enum SuiteType {
        GENERAL, HIGH, EXCLUSIVE, INTERRUPT;
    }

    @Id
    @Column(length = 20)
    @DynamicSerializeElement
    private String suiteName;

    @Enumerated(EnumType.STRING)
    @Column(length = 9, nullable = false)
    @DynamicSerializeElement
    private SuiteType suiteType = SuiteType.GENERAL;

    // position within the program
    @Column(nullable = false, insertable = false, updatable = false)
    @DynamicSerializeElement
    private Integer programPosition;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "suite", orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "suitePosition", nullable = false)
    @DynamicSerializeElement
    private List<SuiteMessage> suiteMessages;

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public SuiteType getSuiteType() {
        return suiteType;
    }

    public void setSuiteType(SuiteType suiteType) {
        this.suiteType = suiteType;
    }

    public Integer getProgramPosition() {
        return programPosition;
    }

    public void setProgramPosition(Integer programPosition) {
        this.programPosition = programPosition;
    }

    public List<SuiteMessage> getSuiteMessages() {
        return suiteMessages;
    }

    public void setSuiteMessages(List<SuiteMessage> suiteMessages) {
        this.suiteMessages = suiteMessages;
    }

}
