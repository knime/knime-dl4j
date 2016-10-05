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
 *   25.08.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.nodes.learn.dialog.component.group;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JOptimizationAlgorithm;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.EnumUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing training methods parameter.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class TrainingMethodParametersComponentGroup extends AbstractGridBagDialogComponentGroup {

    SettingsModelNumber m_trainingIterationsSettings;

    SettingsModelString m_optimizitaionAlorithmSettings;

    /**
     * Constructor for class TrainingMethodParametersComponentGroup using the specified
     * {@link LearnerParameterSettingsModels} to create settings for contained components and specifying whether to add
     * a panel for the finetune parameter.
     *
     * @param learnerSettings
     * @param addFinetune
     */
    public TrainingMethodParametersComponentGroup(final LearnerParameterSettingsModels2 learnerSettings,
        final boolean addFinetune) {
        m_trainingIterationsSettings =
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.TRAINING_ITERATIONS);
        m_optimizitaionAlorithmSettings =
            (SettingsModelString)learnerSettings.createParameter(LearnerParameter.OPTIMIZATION_ALGORITHM);

        addNumberSpinnerRowComponent(m_trainingIterationsSettings, "Number of Training Iterations", 1);
        addComboBoxRow(m_optimizitaionAlorithmSettings, "Optimization Algorithm",
            EnumUtils.getStringCollectionFromToString(DL4JOptimizationAlgorithm.values()));

        startDynamicGroup();
        addNumberSpinnerRowComponent(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.MAX_NUMBER_LINE_SEARCH_ITERATIONS),
            "Max Number of Line Search Iterations", 1);
        endDynamicGroup(true);

        m_optimizitaionAlorithmSettings.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                String selection = ((SettingsModelString)e.getSource()).getStringValue();
                OptimizationAlgorithm oa = DL4JOptimizationAlgorithm.fromToString(selection).getDL4JValue();
                if (ParameterUtils.MAX_LINE_SEARCH_ITERATIONS_CONDITION.contains(oa)) {
                    expandDynamicGroup(0);
                } else {
                    collapseDynamicGroup(0);
                }
            }
        });

        if (addFinetune) {
            addHorizontalSeparator();

            SettingsModelBoolean finetuneSettings =
                (SettingsModelBoolean)learnerSettings.createParameter(LearnerParameter.USE_FINETUNE);
            addCheckboxRow(finetuneSettings, "Do Finetuning?");
        }
    }
}
