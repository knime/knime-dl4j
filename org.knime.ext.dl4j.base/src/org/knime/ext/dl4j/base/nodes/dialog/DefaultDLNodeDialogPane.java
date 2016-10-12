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
 *   24.08.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.nodes.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Implementation of NodeDialogPane for Learner Nodes that allows to add {@link IDialogComponentGroup}s to the dialog.
 * Settings will be automatically saved and loaded.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class DefaultDLNodeDialogPane extends NodeDialogPane {

    private static final String TAB_TITLE = "Options";

    private String m_defaultTabTitle = TAB_TITLE;

    private final List<IDialogComponentGroup> m_componentGroups;

    private final List<DialogComponent> m_additionalComponents;

    private JPanel m_wrapperPanel;

    private JPanel m_alignTopPanel;

    private final GridBagConstraints m_wrapperConstraints;

    /**
     * Constructor for class DefaultLearnerNodeDialogPane.
     */
    public DefaultDLNodeDialogPane() {
        super();
        m_componentGroups = new ArrayList<IDialogComponentGroup>();
        m_additionalComponents = new ArrayList<DialogComponent>();
        m_wrapperConstraints = new GridBagConstraints();

        resetWrapperContraints();
        resetPanels();

        super.addTab(m_defaultTabTitle, m_alignTopPanel);
    }

    /**
     * Resets GridBagContraints for the global panel.
     */
    private void resetWrapperContraints() {
        m_wrapperConstraints.gridx = 0;
        m_wrapperConstraints.gridy = 0;
        m_wrapperConstraints.weightx = 1;
        m_wrapperConstraints.fill = GridBagConstraints.HORIZONTAL;
        m_wrapperConstraints.anchor = GridBagConstraints.WEST;
    }

    /**
     * Sets constraints to the next row.
     */
    private void nextRow() {
        m_wrapperConstraints.gridy++;
    }

    /**
     * Creates new panels.
     */
    private void resetPanels() {
        m_wrapperPanel = new JPanel(new GridBagLayout());
        //wrap in border layout to align at the top of the view
        m_alignTopPanel = new JPanel(new BorderLayout());
        m_alignTopPanel.add(m_wrapperPanel, BorderLayout.PAGE_START);
    }

    /**
     * Adds the specified panel to the global panel using the current constraints.
     *
     * @param panel the panel to add
     */
    private void addPanelToWrapper(final JPanel panel) {
        m_wrapperPanel.add(panel, m_wrapperConstraints);
        nextRow();
    }

    /**
     * Adds the specified {@link IDialogComponentGroup} to the panel.
     *
     * @param group the group to add
     */
    public void addDialogComponentGroup(final IDialogComponentGroup group) {
        m_componentGroups.add(group);
        addPanelToWrapper(group.getComponentGroupPanel());
    }

    /**
     * Wraps the specified {@link IDialogComponentGroup} with a border and adds it to the panel.
     *
     * @param group the group to add
     * @param borderTitle the label displayed in the border
     */
    public void addDialogComponentGroupWithBorder(final IDialogComponentGroup group, final String borderTitle) {
        JPanel componentGroupBorder = wrapWithBorderPanel(group.getComponentGroupPanel(), borderTitle);
        m_componentGroups.add(group);
        addPanelToWrapper(componentGroupBorder);
    }

    /**
     * Wraps the specified {@link IDialogComponentGroup} with a border and a collapsible panel. Adds a checkbox to the
     * top of the panel which trigges the collapse action of the collapsible panel.
     *
     * @param group the group to add
     * @param borderTitle the label displayed in the border
     * @param groupSwitchSettings the settings for the checkbox
     * @param switchLabel the label of the checkbox
     */
    public void addCollapsibleDialogComponentGroupWithBorder(final IDialogComponentGroup group,
        final String borderTitle, final SettingsModelBoolean groupSwitchSettings, final String switchLabel) {

        // GridBag to for group switch (DialogComponentBoolean) and the component group
        JPanel componentGroupWrapper = new JPanel(new GridBagLayout());
        GridBagConstraints gbc;

        // add collapsible component group
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        JXCollapsiblePane collapsibleComponentGroup =
            wrapWithCollapsibleBorderPanel(group.getComponentGroupPanel(), borderTitle);
        componentGroupWrapper.add(collapsibleComponentGroup, gbc);

        // sync collapse state with settings model
        collapsibleComponentGroup.setCollapsed(!groupSwitchSettings.getBooleanValue());

        // add group switch
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        DialogComponentBoolean groupSwitchComponent = new DialogComponentBoolean(groupSwitchSettings, switchLabel);
        JPanel groupSwitchPanel = groupSwitchComponent.getComponentPanel();
        componentGroupWrapper.add(groupSwitchPanel, gbc);

        // add toggle action to collapse/show component group
        groupSwitchSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                Action toggleAction = collapsibleComponentGroup.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
                toggleAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        // add all components to component lists for saving/loading
        m_additionalComponents.add(groupSwitchComponent);
        m_componentGroups.add(group);

        m_wrapperPanel.add(componentGroupWrapper, m_wrapperConstraints);
        nextRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        // save settings from component groups
        for (IDialogComponentGroup group : m_componentGroups) {
            group.saveSettingsTo(settings);
        }
        // save settings from components not contained in groups
        for (DialogComponent component : m_additionalComponents) {
            component.saveSettingsTo(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        assert settings != null;
        assert specs != null;
        // load settings from component groups
        for (IDialogComponentGroup group : m_componentGroups) {
            group.loadSettingsFrom(settings, specs);
        }
        // load settings from components not contained in groups
        for (DialogComponent component : m_additionalComponents) {
            component.loadSettingsFrom(settings, specs);
        }
    }

    /**
     * Sets the title of the default tab that is created and used until you call {@link #createNewTab}.
     *
     * @param tabTitle the new title of the first tab. Can't be null or empty.
     * @throws IllegalArgumentException if the title is already used by another tab, or if the specified title is null
     *             or empty.
     */
    public void setDefaultTabTitle(final String tabTitle) {
        if ((tabTitle == null) || (tabTitle.length() == 0)) {
            throw new IllegalArgumentException("The title of a tab can't be " + "null or empty.");
        }
        if (tabTitle.equals(m_defaultTabTitle)) {
            return;
        }
        // check if we already have a tab with the new title
        if (super.getTab(tabTitle) != null) {
            throw new IllegalArgumentException(
                "A tab with the specified new" + " name (" + tabTitle + ") already exists.");
        }
        super.renameTab(m_defaultTabTitle, tabTitle);
        m_defaultTabTitle = tabTitle;
    }

    /**
     * Creates a new tab in the dialog. All components added from now on are placed in that new tab. After creating a
     * new tab the previous tab is no longer accessible. If a tab with the same name was created before an Exception is
     * thrown. The new panel in the new tab has no group set (i.e. has no border). The new tab is placed at the
     * specified position (or at the right most position, if the index is too big).
     *
     * @param tabTitle the title of the new tab to use from now on. Can't be null or empty.
     * @param index the index to place the new tab at. Can't be negative.
     * @throws IllegalArgumentException if you specify a title that is already been used by another tab. Or if the
     *             specified title is null or empty.
     * @see #setDefaultTabTitle(String)
     */
    public void createNewTabAt(final String tabTitle, final int index) {
        if ((tabTitle == null) || (tabTitle.length() == 0)) {
            throw new IllegalArgumentException("The title of a tab can't be " + "null nor empty.");
        }
        // check if we already have a tab with the new title
        if (super.getTab(tabTitle) != null) {
            throw new IllegalArgumentException(
                "A tab with the specified new" + " name (" + tabTitle + ") already exists.");
        }
        resetPanels();
        resetWrapperContraints();
        super.addTabAt(index, tabTitle, m_alignTopPanel);
    }

    /**
     * Creates a new tab in the dialog. All components added from now on are placed in that new tab. After creating a
     * new tab the previous tab is no longer accessible. If a tab with the same name was created before an Exception is
     * thrown. The new panel in the new tab has no group set (i.e. has no border). The tab is placed at the right most
     * position.
     *
     * @param tabTitle the title of the new tab to use from now on. Can't be null or empty.
     * @throws IllegalArgumentException if you specify a title that is already been used by another tab. Or if the
     *             specified title is null or empty.
     * @see #setDefaultTabTitle(String)
     */
    public void createNewTab(final String tabTitle) {
        createNewTabAt(tabTitle, Integer.MAX_VALUE);
    }

    /**
     * Wraps the specified panel with a border with the specified label.
     *
     * @param panel the panel to wrap
     * @param borderTitle the border label
     * @return the wrapped panel
     */
    private JPanel wrapWithBorderPanel(final JPanel panel, final String borderTitle) {
        JPanel panelWithBorder = new JPanel(new GridLayout(1, 1));
        panelWithBorder.setBorder(BorderFactory.createTitledBorder(borderTitle));
        panelWithBorder.add(panel);
        return panelWithBorder;
    }

    /**
     * Wraps the specified panel with a collapsible panel with the specified border label .
     *
     * @param panel the panel to wrap
     * @param borderTitle the border label
     * @return the wrapped panel
     */
    private JXCollapsiblePane wrapWithCollapsibleBorderPanel(final JPanel panel, final String borderTitle) {
        JXCollapsiblePane cp = new JXCollapsiblePane(Direction.UP);
        cp.setAnimated(true);
        cp.add(wrapWithBorderPanel(panel, borderTitle));
        return cp;
    }
}
