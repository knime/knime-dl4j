/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.com; Email: contact@knime.com
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

/**
 * Storage class for learning status information.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class LearningStatus {
    private final int m_currentEpoch;

    private final int m_maxEpochs;

    private final String m_trainingMethod;

    private final Double m_score;

    /**
     * Constructor for class LearningStatus specifying the status which should be stored.
     *
     * @param currentEpoch the current epoch number
     * @param maxEpochs the maximum number of epochs
     * @param score the current score
     * @param trainingMethod string describing training method
     */
    public LearningStatus(final int currentEpoch, final int maxEpochs, final Double score,
        final String trainingMethod) {
        m_currentEpoch = currentEpoch;
        m_maxEpochs = maxEpochs;
        m_score = score;
        m_trainingMethod = trainingMethod;
    }

    public int getCurrentEpoch() {
        return m_currentEpoch;
    }

    public int getMaxEpochs() {
        return m_maxEpochs;
    }

    public String getTrainingsMethod() {
        return m_trainingMethod;
    }

    public Double getScore() {
        return m_score;
    }

    /**
     * Creates string description of this learning status containing training method, current epoch, maximum number of
     * epochs and current score.
     *
     * @return description of this learning status
     */
    public String getStatusDescription() {
        return m_trainingMethod + " on epoch: " + m_currentEpoch + " of " + m_maxEpochs + " epochs with score: "
            + m_score;
    }

    /**
     * Creates string description of the current epoch status containing what training method is used and how many
     * epochs there are to go.
     *
     * @return description of this epoch status
     */
    public String getEpochDescription() {
        return m_trainingMethod + " on epoch: " + m_currentEpoch + " of " + m_maxEpochs + " epochs";
    }

}
