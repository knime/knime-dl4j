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
 *   15.07.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.data.convert.row;

import org.knime.core.data.RowKey;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Abstract superclass for data row to data set converters which have a train(convert with target value) or test(convert
 * without target value) mode.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public abstract class AbstractDataRowToDataSetConverter implements IDataRowToDataSetConverter {

    private int m_featureLength;

    private int m_targetLength;

    private final boolean m_isTrain;

    /**
     * Super constructor for class AbstractDataRowToDataSetConverter specifying if the converter should convert in train
     * or test mode.
     *
     * @param isTrain
     */
    public AbstractDataRowToDataSetConverter(final boolean isTrain) {
        m_isTrain = isTrain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int featureLength() {
        return m_featureLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int targetLength() {
        return m_targetLength;
    }

    /**
     * Set the feature vector length.
     *
     * @param featureLength
     */
    public void setFeatureLength(final int featureLength) {
        m_featureLength = featureLength;
    }

    /**
     * Set the target vector length.
     *
     * @param targetLength
     */
    public void setTargetLength(final int targetLength) {
        m_targetLength = targetLength;
    }

    /**
     * Return boolean indicating if in train mode or not.
     *
     * @return if in train mode or not
     */
    public boolean isTrain() {
        return m_isTrain;
    }

    /**
     * Performs checks on converted feature and target vector.
     *
     * @param features the feature vector to check
     * @param targets the target vector to check
     * @param rowKey the {@link RowKey} of the current row for error reporting
     * @throws Exception if features are null, if the length of the feature vector does not match the expected feature
     *             vector length of this converter, if in train mode and target is null
     */
    protected void validateDataSet(final INDArray features, final INDArray targets, final RowKey rowKey)
        throws Exception {
        if (features == null) {
            throw new Exception("Features expected but was null.");
        }
        if (features.length() != m_featureLength) {
            throw new Exception("Length of current input in row: " + rowKey
                + " does not match expected length. Possible images or collections " + "may not be of same size.");
        }
        if (isTrain()) {
            if (targets == null) {
                throw new Exception("Labels expected in train mode but was null.");
            }
        }

    }

}
