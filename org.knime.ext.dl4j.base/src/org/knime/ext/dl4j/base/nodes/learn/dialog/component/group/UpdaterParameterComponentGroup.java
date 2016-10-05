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
 *   26.08.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.nodes.learn.dialog.component.group;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deeplearning4j.nn.conf.Updater;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JUpdater;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.EnumUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing updater parameter.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class UpdaterParameterComponentGroup extends AbstractGridBagDialogComponentGroup {

    SettingsModelBoolean m_useUpdaterSettings;

    SettingsModelString m_updaterSettings;

    /**
     * Constructor for class UpdaterParameterComponentGroup using the specified {@link LearnerParameterSettingsModels}
     * to create settings for contained components.
     *
     * @param learnerSettings
     */
    public UpdaterParameterComponentGroup(final LearnerParameterSettingsModels2 learnerSettings) {
        m_useUpdaterSettings = (SettingsModelBoolean)learnerSettings.createParameter(LearnerParameter.USE_UPDATER);
        m_updaterSettings = (SettingsModelString)learnerSettings.createParameter(LearnerParameter.UPDATER);
        addToggleComboBoxRow(m_useUpdaterSettings, "", m_updaterSettings,
            EnumUtils.getStringCollectionFromToString(DL4JUpdater.values()));

        m_useUpdaterSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateCollapseStatus();
                updateEnableStatus();
            }
        });

        //group 0
        startDynamicGroup();
        addNumberEditRowComponent(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.ADAM_MEAN_DECAY), "ADAM Mean Decay");
        addNumberEditRowComponent((SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.ADAM_VAR_DECAY),
            "ADAM Variance Decay");
        endDynamicGroup(true);

        //group 1
        startDynamicGroup();
        addNumberEditRowComponent((SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.ADADELTA_RHO),
            "RHO");
        endDynamicGroup(true);

        //group 2
        startDynamicGroup();
        addNumberEditRowComponent((SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.RMS_DECAY),
            "RMS Decay");
        endDynamicGroup(true);

        //group 3
        startDynamicGroup();
        SettingsModelNumber m_momentumRateSettings =
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.MOMENTUM);
        SettingsModelString m_momentumScheduleSettings =
            (SettingsModelString)learnerSettings.createParameter(LearnerParameter.MOMENTUM_AFTER);
        addNumberEditRowComponent(m_momentumRateSettings, "Momentum");
        addStringEditRowComponent(m_momentumScheduleSettings, "Schedule");
        endDynamicGroup(false);

        m_updaterSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateCollapseStatus();
            }
        });
    }

    private void updateCollapseStatus() {
        boolean useUpdater = m_useUpdaterSettings.getBooleanValue();
        Updater u = Updater.valueOf(m_updaterSettings.getStringValue());

        if (!useUpdater) {
            collapseAll();
        } else if (u.equals(ParameterUtils.ADAM_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(0);
        } else if (u.equals(ParameterUtils.ADADELTA_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(1);
        } else if (u.equals(ParameterUtils.RMSPROP_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(2);
        } else if (u.equals(ParameterUtils.NESTEROVS_PARAMETER_CONDITION)) {
            collapseAll();
            expandDynamicGroup(3);
        } else {
            collapseAll();
        }
    }

    private void updateEnableStatus() {
        m_updaterSettings.setEnabled(m_useUpdaterSettings.getBooleanValue());
    }
}
