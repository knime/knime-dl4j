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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deeplearning4j.nn.weights.WeightInit;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JDistribution;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.EnumUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing weight init parameter.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class WeightInitParameterComponentGroup extends AbstractGridBagDialogComponentGroup {

    SettingsModelBoolean m_useGlobalWeightInitSettings;

    SettingsModelString m_globalWeightInitSettings;

    JXCollapsiblePane m_uniformDistCollapsible;

    JXCollapsiblePane m_normalDistCollapsible;

    JXCollapsiblePane m_binomialDistCollapsible;

    SettingsModelString m_distributionSettings;

    /**
     * Constructor for class WeightInitParameterComponentGroup using the specified
     * {@link LearnerParameterSettingsModels} to create settings for contained components.
     *
     * @param learnerSettings
     */
    public WeightInitParameterComponentGroup(final LearnerParameterSettingsModels2 learnerSettings) {
        m_useGlobalWeightInitSettings =
            (SettingsModelBoolean)learnerSettings.createParameter(LearnerParameter.USE_GLOBAL_WEIGHT_INIT);
        m_globalWeightInitSettings =
            (SettingsModelString)learnerSettings.createParameter(LearnerParameter.GLOBAL_WEIGHT_INIT);

        addToggleComboBoxRow(m_useGlobalWeightInitSettings, "", m_globalWeightInitSettings,
            EnumUtils.getStringCollectionFromToString(WeightInit.values()));

        m_useGlobalWeightInitSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateEnableStatus();
                updateCollapseStatus();
            }
        });

        startDynamicGroup();

        m_distributionSettings = (SettingsModelString)learnerSettings.createParameter(LearnerParameter.DISTRIBUTION);

        addComboBoxRow(m_distributionSettings, "Distribution",
            EnumUtils.getStringCollectionFromToString(DL4JDistribution.values()));

        DialogComponentNumberEdit distUpper = new DialogComponentNumberEdit(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.DISTRIBUTION_UPPER_BOUND),
            "Upper Bound");
        DialogComponentNumberEdit distLower = new DialogComponentNumberEdit(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.DISTRIBUTION_LOWER_BOUND),
            "Lower Bound");

        DialogComponentNumberEdit distMean = new DialogComponentNumberEdit(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.DISTRIBUTION_MEAN), "Mean");
        DialogComponentNumberEdit distSTD = new DialogComponentNumberEdit(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.DISTRIBUTION_STD),
            "Standard Deviation");

        DialogComponentNumberEdit distTrails = new DialogComponentNumberEdit(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.DISTRIBUTION_BINOMIAL_TRAILS),
            "Trails");
        DialogComponentNumberEdit distProb = new DialogComponentNumberEdit(
            (SettingsModelNumber)learnerSettings.createParameter(LearnerParameter.DISTRIBUTION_BINOMIAL_PROBABILITY),
            "Probability");

        List<DialogComponent> dialogComponents = getComponents();
        dialogComponents.add(distUpper);
        dialogComponents.add(distLower);
        dialogComponents.add(distMean);
        dialogComponents.add(distSTD);
        dialogComponents.add(distTrails);
        dialogComponents.add(distProb);

        //default is uniform so expand
        m_uniformDistCollapsible = wrapWithCollapsible(new DialogComponentNumberEdit[]{distUpper, distLower}, false);
        addComponent(m_uniformDistCollapsible);

        m_normalDistCollapsible = wrapWithCollapsible(new DialogComponentNumberEdit[]{distMean, distSTD}, true);
        addComponent(m_normalDistCollapsible);

        m_binomialDistCollapsible = wrapWithCollapsible(new DialogComponentNumberEdit[]{distTrails, distProb}, true);
        addComponent(m_binomialDistCollapsible);

        m_distributionSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateCollapseStatus();
            }
        });

        endDynamicGroup(true);

        m_globalWeightInitSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateCollapseStatus();
            }
        });
    }

    private JXCollapsiblePane wrapWithCollapsible(final DialogComponentNumberEdit[] comps, final boolean isCollapsed) {
        JXCollapsiblePane distributionPane = new JXCollapsiblePane(Direction.UP);
        distributionPane.setLayout(new GridBagLayout());
        distributionPane.setCollapsed(isCollapsed);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;

        for (DialogComponentNumberEdit comp : comps) {
            JLabel labelComp = getFirstComponent(comp, JLabel.class);
            JTextField textFieldComp = getFirstComponent(comp, JTextField.class);

            gbc.insets = new Insets(10, 0, 0, 20);
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;

            distributionPane.add(labelComp, gbc);

            gbc.insets = new Insets(10, 0, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.gridx++;

            distributionPane.add(textFieldComp, gbc);
            gbc.gridy++;
        }

        gbc.insets = new Insets(0, 0, 8, 0);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        distributionPane.add(new JPanel(), gbc);

        return distributionPane;
    }

    private void updateCollapseStatus() {
        WeightInit w = WeightInit.valueOf(m_globalWeightInitSettings.getStringValue());

        if (!m_useGlobalWeightInitSettings.getBooleanValue()) {
            collapseAll();
        } else if (w.equals(ParameterUtils.DISTRIBUTION_PARAMETER_CONDITION)) {
            DL4JDistribution d = DL4JDistribution.valueOf(m_distributionSettings.getStringValue());
            if (d.equals(DL4JDistribution.BINOMIAL)) {
                expandAll();
                m_binomialDistCollapsible.setCollapsed(false);
                m_normalDistCollapsible.setCollapsed(true);
                m_uniformDistCollapsible.setCollapsed(true);
            } else if (d.equals(DL4JDistribution.NORMAL)) {
                expandAll();
                m_binomialDistCollapsible.setCollapsed(true);
                m_normalDistCollapsible.setCollapsed(false);
                m_uniformDistCollapsible.setCollapsed(true);
            } else if (d.equals(DL4JDistribution.UNIFORM)) {
                expandAll();
                m_binomialDistCollapsible.setCollapsed(true);
                m_normalDistCollapsible.setCollapsed(true);
                m_uniformDistCollapsible.setCollapsed(false);
            }
        } else {
            collapseAll();
        }
    }

    private void updateEnableStatus() {
        m_globalWeightInitSettings.setEnabled(m_useGlobalWeightInitSettings.getBooleanValue());
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
