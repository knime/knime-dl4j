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

import java.util.Arrays;
import java.util.Collections;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.nodes.dialog.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.PredictorPrameter;
import org.knime.ext.dl4j.base.settings.impl.PredictorParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.EnumUtils;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing bias parameter.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class LayerSelectionComponentGroup extends AbstractGridBagDialogComponentGroup {

    private SettingsModelString m_layerSelection;

    private DLModelPortObjectSpec m_modelPortSpecs;

    private final int m_modelPortIndex;

    /**
     * Constructor for class BiasParameterComponentGroup using the specified {@link PredictorParameterSettingsModels2}
     * to create settings for contained components.
     *
     * @param predictorSettings
     * @param modelPortIndex the index of the dl model port
     */
    public LayerSelectionComponentGroup(final PredictorParameterSettingsModels2 predictorSettings,
        final int modelPortIndex) {
        m_modelPortIndex = modelPortIndex;

        m_layerSelection = (SettingsModelString)predictorSettings.createParameter(PredictorPrameter.LAYER_SELECTION);

        addComboBoxRow(m_layerSelection, "", Collections.singletonList("- no layer available -"));
    }

    /**
     * Update the string list shown in the combo-box in the dialog to display the layers contained in the net.
     */
    private void updateStringList() {
        if (m_modelPortSpecs != null) {
            String[] layers = EnumUtils.getStringListFromEnumCollection(m_modelPortSpecs.getLayerTypes());
            DialogComponentStringSelection selectionComp = getStringSelectionComponent();
            selectionComp.replaceListItems(Arrays.asList(addLayerNum(layers)), null);
        }
    }

    /**
     * Adds the index to the list of layer names.
     *
     * @param layerNames
     * @return
     */
    private String[] addLayerNum(final String[] layerNames) {
        String[] layerNamesWithIndex = new String[layerNames.length];
        int i = 0;
        for (String layerName : layerNames) {
            layerNamesWithIndex[i] = i + ":" + layerName;
            i++;
        }
        return layerNamesWithIndex;
    }

    /**
     * Get the {@link DialogComponentStringSelection} from the dialog to update the combo-box.
     *
     * @return
     */
    private DialogComponentStringSelection getStringSelectionComponent() {
        for (DialogComponent comp : getComponents()) {
            if (comp instanceof DialogComponentStringSelection) {
                return (DialogComponentStringSelection)comp;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        //set model specs used to get the list of layers in the net and update the combo-box
        m_modelPortSpecs = (DLModelPortObjectSpec)specs[m_modelPortIndex];
        updateStringList();
        super.loadSettingsFrom(settings, specs);
    }
}
