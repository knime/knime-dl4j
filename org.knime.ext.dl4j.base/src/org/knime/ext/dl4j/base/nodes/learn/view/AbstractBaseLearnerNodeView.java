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
 *   26.07.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.nodes.learn.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeView;
import org.knime.ext.dl4j.base.nodes.learn.AbstractDLLearnerNodeModel;
import org.knime.ext.dl4j.base.nodes.learn.LearningStatus;
import org.knime.ext.dl4j.base.nodes.learn.feedforward.FeedforwardLearnerNodeModel;

/**
 *
 * @author David Kolb
 * @param <T>
 */
public abstract class AbstractBaseLearnerNodeView<T extends AbstractDLLearnerNodeModel> extends NodeView<T> {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(AbstractBaseLearnerNodeView.class);

    private final JLabel m_scoreDisplay = new JLabel("Not available");

    private final JLabel m_learningInfo = new JLabel("No data available");

    private final JButton m_stopButton = new JButton("Stop Learning");

    /**
     * Creates a new view.
     *
     * @param nodeModel The model (class: {@link FeedforwardLearnerNodeModel})
     */
    protected AbstractBaseLearnerNodeView(final T nodeModel) {
        super(nodeModel);
        //set font sizes of labels
        m_scoreDisplay.setFont(new Font(m_scoreDisplay.getFont().getName(), m_scoreDisplay.getFont().getStyle(), 18));
        m_learningInfo.setFont(new Font(m_learningInfo.getFont().getName(), m_learningInfo.getFont().getStyle(), 25));

        setShowNODATALabel(false);
        GridBagConstraints c;

        //create top label showing epoch information and training method
        final JPanel p_top = new JPanel(new GridLayout(0, 1));
        p_top.setBorder(BorderFactory.createTitledBorder("Learning Information:"));
        p_top.add(m_learningInfo);

        //create middle label showing score
        final JPanel p_middle = new JPanel(new GridLayout(0, 1));
        p_middle.setBorder(BorderFactory.createTitledBorder("Current Loss:"));
        p_middle.add(m_scoreDisplay);

        //create bottom button for stopping
        final JPanel p_bottom = new JPanel(new GridLayout(0, 1));
        m_stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                nodeModel.getLearningMonitor().stopLearning();
            }
        });
        m_stopButton.setSize(new Dimension(200, 50));
        m_stopButton.setPreferredSize(new Dimension(200, 50));
        m_stopButton.setMinimumSize(new Dimension(200, 50));
        p_bottom.add(m_stopButton);

        //pack into one GridBad and style
        final JPanel p_wrapper = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.ipadx = 350;
        c.insets = new Insets(20, 5, 0, 5);
        c.anchor = GridBagConstraints.WEST;
        p_wrapper.add(p_top, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(20, 5, 0, 5);
        p_wrapper.add(p_middle, c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(20, 5, 20, 5);
        p_wrapper.add(p_bottom, c);

        setComponent(p_wrapper);

        //get last values from model if available
        if (nodeModel.getScore() == null) {
            m_scoreDisplay.setText("Not available");
        } else {
            m_scoreDisplay.setText(nodeModel.getScore() + "");
        }
        if (nodeModel.getLearningStatus() == null) {
            m_learningInfo.setText("No data available");
        } else {
            m_learningInfo.setText(nodeModel.getLearningStatus().getEpochDescription());
        }
    }

    /**
     * Updates the label fields of the view. Expects either a String containing the current score or a
     * {@link LearningStatus} object. If the passed argument is null the view will be reset.
     */
    @Override
    protected void updateModel(final Object arg) {
        if (arg == null) {
            resetView();
        } else if (arg instanceof String) {
            m_scoreDisplay.setText((String)arg);
        } else if (arg instanceof LearningStatus) {
            final LearningStatus status = (LearningStatus)arg;
            m_learningInfo.setText(status.getEpochDescription());
        } else {
            logger.coding("Unrecognized argument passed to view during execution: " + arg.getClass().getSimpleName());
            setShowNODATALabel(true);
        }
    }

    /**
     * Resets view labels to default values.
     */
    private void resetView() {
        m_scoreDisplay.setText("Not available");
        m_learningInfo.setText("No data available");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        //nothing to do here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        //nothing to do here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        //nothing to do here
    }

}
