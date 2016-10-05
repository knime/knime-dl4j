/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.org; Email: contact@knime.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.dl4j.base.nodes.learn.view;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.optimize.api.IterationListener;
import org.knime.ext.dl4j.base.nodes.learn.AbstractDLLearnerNodeModel;

/**
 * Implementation of {@link IterationListener} using a {@link AbstractDLLearnerNodeModel} for score reporting and view
 * communication. Also updates the score of the learner node model.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class UpdateLearnerViewIterationListener implements IterationListener {

    /**
     *
     */
    private static final long serialVersionUID = 825931436593012325L;

    private boolean m_invoked = false;

    private final AbstractDLLearnerNodeModel m_nodeModel;

    /**
     * Constructor for class UpdateLearnerViewIterationListener specifying the {@link AbstractDLLearnerNodeModel} it
     * should use for updating the view. After each iteration the method
     * {@link AbstractDLLearnerNodeModel#passObjToView(Object)} is called which in turn calls notifyView on the
     * NodeModel.
     *
     * @param nodeModel
     */
    public UpdateLearnerViewIterationListener(final AbstractDLLearnerNodeModel nodeModel) {
        m_nodeModel = nodeModel;
    }

    @Override
    public boolean invoked() {
        return m_invoked;
    }

    @Override
    public void invoke() {
        m_invoked = true;
    }

    @Override
    public void iterationDone(final Model model, final int iteration) {
        invoke();
        final double result = model.score();
        //pass the current score to the view
        m_nodeModel.passObjToView(new Double(result));
        //update score in node model
        m_nodeModel.setScore(result);
    }

}
