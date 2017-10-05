/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *   29.08.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.nodes.predict.dialog;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.dl4j.base.nodes.dialog.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.PredictorPrameter;
import org.knime.ext.dl4j.base.settings.impl.PredictorParameterSettingsModels2;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing bias parameter.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class AppendProbabilityColumnComponentGroup extends AbstractGridBagDialogComponentGroup {

    SettingsModelBoolean m_appendProbability;

    SettingsModelString m_probabilityColumnSuffix;

    /**
     * Constructor for class BiasParameterComponentGroup using the specified {@link PredictorParameterSettingsModels2}
     * to create settings for contained components.
     *
     * @param predictorSettings
     */
    public AppendProbabilityColumnComponentGroup(final PredictorParameterSettingsModels2 predictorSettings) {
        m_appendProbability =
            (SettingsModelBoolean)predictorSettings.createParameter(PredictorPrameter.APPEND_PROBABILITY);
        m_probabilityColumnSuffix =
            (SettingsModelString)predictorSettings.createParameter(PredictorPrameter.PROBABILITY_COLUMN_SUFFIX);

        addCheckboxRow(m_appendProbability, "Append columns with class probabilities?", true);
        addStringEditRowComponent(m_probabilityColumnSuffix, "Suffix for probability columns");

        m_appendProbability.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateEnableStatus();
            }
        });
    }

    private void updateEnableStatus() {
        m_probabilityColumnSuffix.setEnabled(m_appendProbability.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        super.loadSettingsFrom(settings, specs);
        updateEnableStatus();
    }
}
