/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   30.08.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.nodes.learn.dialog.component.group;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing a column selection for regression.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class RegressionColumnSelectionComponentGroup extends AbstractGridBagDialogComponentGroup {

    private SettingsModelColumnFilter2 m_targetColumnFilterSettings;

    private SettingsModelColumnFilter2 m_featureColumnFilterSettings;

    private JLabel m_indicatorLabel;

    private PortObjectSpec[] m_specs;

    /**
     * Constructor for class RegressionColumnSelectionComponentGroup using the specified
     * {@link DataParameterSettingsModels} to create settings for contained components and the specified port index to
     * use for the column selection.
     *
     * @param dataSettings
     * @param specIndex
     */
    public RegressionColumnSelectionComponentGroup(final DataParameterSettingsModels2 dataSettings,
        final int specIndex) {
        m_targetColumnFilterSettings =
            (SettingsModelColumnFilter2)dataSettings.createParameter(DataParameter.TARGET_COLUMN_SELECTION2);
        m_featureColumnFilterSettings =
            (SettingsModelColumnFilter2)dataSettings.createParameter(DataParameter.FEATURE_COLUMN_SELECTION2);

        addLabelRow("Target Column Selection", 15);
        addColumnFilterRowComponent(m_targetColumnFilterSettings, specIndex);
        addWhitespaceRow(15);

        m_indicatorLabel = new JLabel("");
        addComponent(m_indicatorLabel);

        addWhitespaceRow(15);
        addLabelRow("Feature Column Selection", 15);
        addColumnFilterRowComponent(m_featureColumnFilterSettings, specIndex);

        m_targetColumnFilterSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateIndicatorLabel(m_specs, specIndex);
            }
        });

        m_featureColumnFilterSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateIndicatorLabel(m_specs, specIndex);
            }
        });
    }

    /**
     * Updates the label that indicates whether the column selection of both column filters is mutually exclusive. If
     * not a corresponding warning is displayed.
     *
     * @param specs the specs array
     * @param specIndex the index of the spec in the array corresponding to the table containing the columns
     */
    private void updateIndicatorLabel(final PortObjectSpec[] specs, final int specIndex) {
        if (specs == null) {
            return;
        }

        DataTableSpec tableSpec = (DataTableSpec)specs[specIndex];

        String[] featureColumns = m_featureColumnFilterSettings.applyTo(tableSpec).getIncludes();
        String[] targetColumns = m_targetColumnFilterSettings.applyTo(tableSpec).getIncludes();

        try {
            ConfigurationUtils.validateMutuallyExclusive(featureColumns, targetColumns);
            ConfigurationUtils.validateColumnSelection(tableSpec, targetColumns);
            ConfigurationUtils.validateColumnSelection(tableSpec, featureColumns);

            m_indicatorLabel.setText("Column selection OK!");
            m_indicatorLabel.setForeground(Color.BLACK);
        } catch (InvalidSettingsException e) {
            m_indicatorLabel.setText(e.getMessage());
            m_indicatorLabel.setForeground(Color.RED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        m_specs = specs;
        super.loadSettingsFrom(settings, specs);
    }
}
