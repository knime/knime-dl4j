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

import org.apache.commons.lang3.ArrayUtils;
import org.knime.core.data.DataCell;
import org.knime.ext.dl4j.base.data.convert.framework.CachedConverter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

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

    private final CachedConverter m_cachedConverter;

    /**
     * Super constructor for class AbstractDataRowToDataSetConverter specifying if the converter should convert in train
     * or test mode.
     *
     * @param isTrain
     */
    public AbstractDataRowToDataSetConverter(final boolean isTrain) {
        m_isTrain = isTrain;
        m_cachedConverter = new CachedConverter();

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
     * @throws Exception if features are null, if the length of the feature vector does not match the expected feature
     *             vector length of this converter, if in train mode and target is null
     */
    protected void validateDataSet(final INDArray features, final INDArray targets) throws Exception {
        if (features == null) {
            throw new Exception("Features expected but was null.");
        }
        if (features.length() != m_featureLength) {
            throw new Exception(
                "Length of current input does not match expected length. Possible images or collections "
                    + "may not be of same size.");
        }
        if (isTrain()) {
            if (targets == null) {
                throw new Exception("Labels expected in train mode but was null.");
            }
        }

    }

    /**
     * Converts the specified cell to a flattened vector and appends it to the specified vector at the specified start
     * index.
     *
     * @param array the array to append to
     * @param cell the cell to convert and append
     * @param putIndex the start index to put the cell
     * @return the position where the last value was put plus one, so the next free position
     * @throws Exception if the specified array is no row vector, if there is a problem with conversion
     */
    protected int putCellToVector(final INDArray array, final DataCell cell, final int putIndex) throws Exception {
        if (!array.isRowVector()) {
            throw new IllegalArgumentException("Expected specified array to be a row vector.");
        }
        final Double[] convertedCell = m_cachedConverter.convertDataCellToJava(cell, Double[].class);
        //if we have a scalar we do not need to convert to primitive and can put directly
        if (isScalar(convertedCell)) {
            array.put(0, putIndex, convertedCell[0]);
            return putIndex + 1;
            //else convert to primitive and put whole array
        } else {
            final double[] primitive = ArrayUtils.toPrimitive(convertedCell);
            //write to interval exclusive last index so point to next free position of interval end
            int intervalEnd = putIndex + primitive.length;

            //we can't write outside of our array
            if (intervalEnd > array.length()) {
                throw new IllegalArgumentException(
                    "Length of current input does not match expected length. Possible images or collections "
                        + "may not be of same size.");
            }

            array.put(new INDArrayIndex[]{NDArrayIndex.interval(putIndex, intervalEnd, false)}, Nd4j.create(primitive));
            return intervalEnd;
        }
    }

    /**
     * Checks if the specified array is an scalar, meaning that its length is equal to one.
     *
     * @param array the array to check
     * @return true if the length of the array is one, else false
     */
    private boolean isScalar(final Double[] array) {
        if (array.length == 1) {
            return true;
        }
        return false;
    }

}
