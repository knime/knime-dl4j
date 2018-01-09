/*******************************************************************************
 * Copyright by KNIME AG, Zurich, Switzerland
 * Website: http://www.knime.com; Email: contact@knime.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.dl4j.base;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.exception.DL4JOutOfMemoryException;
import org.nd4j.linalg.api.memory.MemoryWorkspaceManager;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Abstract node model for nodes of Depplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public abstract class AbstractDLNodeModel extends NodeModel {
    /**
     * Instantiates a new abstract node model for nodes of KNIME Deeplearning4J integration.
     *
     * @param inPortTypes Input Port Types
     * @param outPortTypes Output Port Types
     * @see NodeModel#NodeModel(PortType[], PortType[])
     */
    protected AbstractDLNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
        m_settingsModels = initSettingsModels();
    }

    /** The list of Settings Models. */
    private List<SettingsModel> m_settingsModels;

    /**
     * Initialises member settings models of the current node. Will be called when the NodeModel is created.
     *
     * @return a List of SettingsModels corresponding to this node
     */
    protected abstract List<SettingsModel> initSettingsModels();

    /**
     * Logs warnings.
     *
     * @param logger the logger instance
     * @param warnings warnings as list of strings
     */
    protected void logWarnings(final NodeLogger logger, final List<String> warnings) {
        for (final String w : warnings) {
            if ((w != null) && !w.isEmpty()) {
                logger.warn(w);
            }
        }
    }

    /**
     * Logs debugs.
     *
     * @param logger the logger instance
     * @param debug debug messages as list of strings
     */
    protected void logDebug(final NodeLogger logger, final List<String> debug) {
        for (final String w : debug) {
            if ((w != null) && !w.isEmpty()) {
                logger.debug(w);
            }
        }
    }

    /**
     * Helper which calls executeDL4JMemorySafe() and destroys DL4J workspaces before and after execute.
     */
    private PortObject[] executeWithMemoryCleanup(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        MemoryWorkspaceManager mwsm = Nd4j.getWorkspaceManager();
        //Pre-destroy in case there are any workspaces still left
        mwsm.destroyAllWorkspacesForCurrentThread();
        try {
            return executeDL4JMemorySafe(inObjects, exec);
        } catch (OutOfMemoryError oom) {
            throw new DL4JOutOfMemoryException("Not enough memory available for DL4J. Please consider increasing the "
                + "'Off Heap Memory Limit' in the DL4J Prefernce Page.", oom);
        } finally {
            mwsm.destroyAllWorkspacesForCurrentThread();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
        return (BufferedDataTable[])executeWithMemoryCleanup(inData, exec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        try {
            //If all input objects are tables
            return execute(toBDTArray(inObjects), exec);
        } catch (ClassCastException e) {
            return executeWithMemoryCleanup(inObjects, exec);
        }
    }

    /**
     * Execute method if DL4J memory needs to be released after the execute method. All DL4J workspaces created by the current
     * thread will be destroyed before and after the call of this method.
     *
     * For a general description of the execute method refer to the description of
     * {@link #execute(PortObject[], ExecutionContext)} methods.
     */
    @SuppressWarnings("javadoc")
    protected PortObject[] executeDL4JMemorySafe(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        throw new UnsupportedOperationException("AbstractDLNodeModel.executeDL4JMemorySafe() implementation missing!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        for (final SettingsModel model : m_settingsModels) {
            model.saveSettingsTo(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        for (final SettingsModel model : m_settingsModels) {
            model.validateSettings(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        for (final SettingsModel model : m_settingsModels) {
            model.loadSettingsFrom(settings);
        }
    }

    /**
     * No-op version of {@link NodeModel#loadInternals}.
     *
     * @param nodeInternDir the node intern dir
     * @param exec the exec
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CanceledExecutionException the canceled execution exception
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do here
    }

    /**
     * No-op version of {@link NodeModel#saveInternals}.
     *
     * @param nodeInternDir the node intern dir
     * @param exec the exec
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CanceledExecutionException the canceled execution exception
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        //nothing to do here
    }

    protected List<SettingsModel> getSettingsModels() {
        return m_settingsModels;
    }

    protected void setSettingsModels(final List<SettingsModel> settingsModels) {
        this.m_settingsModels = settingsModels;
    }

    protected void addToSettingsModels(final SettingsModel setting) {
        m_settingsModels.add(setting);
    }

    static BufferedDataTable[] toBDTArray(final PortObject[] inObjects) throws IOException {
        BufferedDataTable[] inTables = new BufferedDataTable[inObjects.length];
        for (int i = 0; i < inObjects.length; i++) {
            inTables[i] = (BufferedDataTable)inObjects[i];
        }
        return inTables;
    }
}
