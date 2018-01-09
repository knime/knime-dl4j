/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
package org.knime.ext.dl4j.base.nodes.learn.dialog;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.dl4j.base.nodes.dialog.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.util.EnumUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing learning rate policy parameter. WIP DO NOT USE!
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class LearningRatePolicyParameterComponentGroup extends AbstractGridBagDialogComponentGroup {

    SettingsModelBoolean m_useAdvancedLearningRateSettings;

    SettingsModelString m_lrPolicySettings;

    SettingsModelNumber m_lrPolicyDecayRate;

    public LearningRatePolicyParameterComponentGroup(final LearnerParameterSettingsModels learnerSettings) {
        m_useAdvancedLearningRateSettings =
            (SettingsModelBoolean)learnerSettings.createParameter(LearnerParameter.USE_ADVANCED_LEARNING_RATE);
        m_lrPolicySettings = (SettingsModelString)learnerSettings.createParameter(LearnerParameter.LR_POLICY);

        addToggleComboBoxRow(m_useAdvancedLearningRateSettings, "", m_lrPolicySettings,
            EnumUtils.getStringCollectionFromToString(LearningRatePolicy.values()));

        m_useAdvancedLearningRateSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateEnableStatus();
                updateCollapseStatus();
            }
        });

        //TODO These conditions seem not to be 100% correct
        //Remember this settings model because we use it in two different dynamic groups.
        m_lrPolicyDecayRate =
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.LR_POLICY_DECAY_RATE);

        //group 0
        startDynamicGroup();
        addNumberEditRowComponent(m_lrPolicyDecayRate, "Decay Rate");
        addNumberEditRowComponent(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.LR_POLICY_POWER), "Power");
        endDynamicGroup(true);

        //group 1
        startDynamicGroup();
        addNumberEditRowComponent(m_lrPolicyDecayRate, "Decay Rate");
        addStringEditRowComponent(
            (SettingsModelString)learnerSettings.createParameter(LearnerParameter.LEARNING_RATE_AFTER), "Schedule");
        endDynamicGroup(true);

        //group 2
        startDynamicGroup();
        addNumberEditRowComponent(m_lrPolicyDecayRate, "Decay Rate");
        addNumberEditRowComponent(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.LR_POLICY_SCORE_DECAY),
            "Score Decay Rate");
        endDynamicGroup(true);

        //group 3
        startDynamicGroup();
        addNumberEditRowComponent(m_lrPolicyDecayRate, "Decay Rate");
        addNumberEditRowComponent(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.LR_POLICY_STEPS), "Steps");
        endDynamicGroup(true);

        //group 4
        startDynamicGroup();
        addNumberEditRowComponent(m_lrPolicyDecayRate, "Decay Rate");
        endDynamicGroup(true);

        m_lrPolicySettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateCollapseStatus();
            }
        });
    }

    private void updateCollapseStatus() {
        boolean useAdvancedSettings = m_useAdvancedLearningRateSettings.getBooleanValue();
        LearningRatePolicy lrp = LearningRatePolicy.valueOf(m_lrPolicySettings.getStringValue());
        if (!useAdvancedSettings) {
            collapseAll();
        } else if (lrp.equals(ParameterUtils.LR_POWER_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(0);
        } else if (lrp.equals(ParameterUtils.LR_SCHEDULE_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(1);
        } else if (lrp.equals(ParameterUtils.LR_SCORE_BASED_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(2);
        } else if (lrp.equals(ParameterUtils.LR_STEPS_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(3);
        } else if (lrp.equals(ParameterUtils.LR_EXPONENTIAL_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(4);
        } else {
            collapseAll();
        }
    }

    private void updateEnableStatus() {
        m_lrPolicySettings.setEnabled(m_useAdvancedLearningRateSettings.getBooleanValue());
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
