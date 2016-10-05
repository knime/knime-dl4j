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

import org.deeplearning4j.nn.weights.WeightInit;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.LayerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JActivationFunction;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JLossFunction;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.EnumUtils;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing output layer parameter.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class OutputLayerParameterComponentGroup extends AbstractGridBagDialogComponentGroup {

    SettingsModelNumber m_numOutputsSettings;

    SettingsModelNumber m_learningRateSettings;

    SettingsModelString m_weightInitSettings;

    SettingsModelString m_activationSettings;

    SettingsModelString m_lossSettings;

    /**
     * Constructor for class OutputLayerParameterComponentGroup using the specified {@link LayerParameterSettingsModels}
     * to create settings for contained components and specifying whether to add a panel for the activation and the
     * number of outputs parameter.
     *
     * @param layerSettings
     * @param addActivation
     * @param addNumOutputs
     */
    public OutputLayerParameterComponentGroup(final LayerParameterSettingsModels2 layerSettings,
        final boolean addActivation, final boolean addNumOutputs) {

        if (addNumOutputs) {
            m_numOutputsSettings = (SettingsModelNumber)layerSettings.createParameter(LayerParameter.NUMBER_OF_OUTPUTS);
            addNumberSpinnerRowComponent(m_numOutputsSettings, "Number of Output Units", 1);
        }

        m_learningRateSettings = (SettingsModelNumber)layerSettings.createParameter(LayerParameter.LEARNING_RATE);
        m_weightInitSettings = (SettingsModelString)layerSettings.createParameter(LayerParameter.WEIGHT_INIT);
        m_lossSettings = (SettingsModelString)layerSettings.createParameter(LayerParameter.LOSS_FUNCTION);

        addNumberEditRowComponent(m_learningRateSettings, "Learning Rate");
        addComboBoxRow(m_weightInitSettings, "Weight Initialisation Strategy",
            EnumUtils.getStringCollectionFromToString(WeightInit.values()));
        addComboBoxRow(m_lossSettings, "Loss Function",
            EnumUtils.getStringCollectionFromToString(DL4JLossFunction.values()));

        if (addActivation) {
            m_activationSettings = (SettingsModelString)layerSettings.createParameter(LayerParameter.ACTIVATION);
            addComboBoxRow(m_activationSettings, "Activation Function",
                EnumUtils.getStringCollectionFromToString(DL4JActivationFunction.values()));
        }

    }
}
