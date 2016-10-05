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

import org.knime.core.data.NominalValue;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels2;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing a column selection for classification.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class ClassificationColumnSelectionComponentGroup extends AbstractGridBagDialogComponentGroup {

    SettingsModelString m_labelColumnSettings;

    SettingsModelColumnFilter2 m_columnFilterSettings;

    /**
     * Constructor for class ClassificationColumnSelectionComponentGroup using the specified
     * {@link DataParameterSettingsModels} to create settings for contained components and the specified port index to
     * use for the column selection.
     *
     * @param dataSettings
     * @param specIndex
     */
    public ClassificationColumnSelectionComponentGroup(final DataParameterSettingsModels2 dataSettings,
        final int specIndex) {
        m_labelColumnSettings = (SettingsModelString)dataSettings.createParameter(DataParameter.LABEL_COLUMN);

        addWhitespaceRow(15);

        addColumnNameSelectionRowComponent(m_labelColumnSettings, "Lable Column", specIndex, NominalValue.class);

        addWhitespaceRow(15);
        addLabelRow("Feature Column Selection", 15);

        m_columnFilterSettings =
            (SettingsModelColumnFilter2)dataSettings.createParameter(DataParameter.FEATURE_COLUMN_SELECTION2);

        addColumnFilterRowComponent(m_columnFilterSettings, specIndex);
    }
}
