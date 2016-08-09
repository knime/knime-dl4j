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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.knime.ext.dl4j.base.util.NDArrayUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Implementation of {@link AbstractDataRowToDataSetConverter} for rows containing features and one or more real valued
 * target columns for regression.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class RealValueTargetDataRowToDataSetConverter extends AbstractDataRowToDataSetConverter {

    private final List<Integer> m_targetColumnsIndices;

    /**
     * Constructor for class RealValueTargetDataRowToDataSetConverter specifying a reference row for expected feature
     * length calculation, a list of indices of the target columns, and if in train mode.
     *
     * @param referenceRow a reference row
     * @param targetColumnsIndices the list of indices of the target columns
     * @param isTrain if in train mode or not
     * @throws Exception
     */
    public RealValueTargetDataRowToDataSetConverter(final DataRow referenceRow,
        final List<Integer> targetColumnsIndices, final boolean isTrain) throws Exception {
        super(isTrain);
        if (!isTrain) {
            m_targetColumnsIndices = new ArrayList<Integer>();
            setTargetLength(-1);
        } else if (targetColumnsIndices == null || targetColumnsIndices.isEmpty()) {
            throw new IllegalArgumentException("List of target columns indices must not be null or empty.");
        } else {
            m_targetColumnsIndices = targetColumnsIndices;
            setTargetLength(TableUtils.calculateFeatureVectorLengthOnlyIndices(referenceRow, m_targetColumnsIndices));
        }
        setFeatureLength(TableUtils.calculateFeatureVectorLengthExcludingIndices(referenceRow, m_targetColumnsIndices));
    }

    /**
     * Convenience constructor for class RealValueTargetDataRowToDataSetConverter specifying only a reference row for
     * expected feature length calculation, hence test mode is used. Equal to calling
     * <code>RealValueTargetDataRowToDataSetConverter(referenceRow, null, false)</code>.
     *
     * @param referenceRow a reference row
     * @throws Exception
     */
    public RealValueTargetDataRowToDataSetConverter(final DataRow referenceRow) throws Exception {
        this(referenceRow, null, false);
    }

    /**
     * {@inheritDoc} Features are converted to a flat vector and label to a flat vector containing target. In test mode
     * label will be empty.
     */
    @Override
    public DataSet convert(final DataRow row) throws Exception {
        INDArray featureVector = null;
        INDArray targetVector = null;
        List<INDArray> featureVectorElements = new ArrayList<INDArray>();
        List<INDArray> targetVectorElements = new ArrayList<INDArray>();

        for (int i = 0; i < row.getNumCells(); i++) {
            final DataCell cell = row.getCell(i);
            // convert cell and add either to feature or target depending on column index
            if (cell.getType().isCollectionType()) {
                final INDArray[] convertedCollction = ConverterUtils.convertDataCellToJava(cell, INDArray[].class);

                if ((m_targetColumnsIndices.contains(i)) && isTrain()) {
                    targetVectorElements.addAll(Arrays.asList(convertedCollction));
                } else {
                    featureVectorElements.addAll(Arrays.asList(convertedCollction));
                }

            } else {
                final INDArray convertedCell = ConverterUtils.convertDataCellToJava(cell, INDArray.class);

                if ((m_targetColumnsIndices.contains(i)) && isTrain()) {
                    targetVectorElements.add(convertedCell);
                } else {
                    featureVectorElements.add(convertedCell);
                }

            }
        }
        featureVector = NDArrayUtils.linearHConcat(featureVectorElements);
        if (isTrain()) {
            targetVector = NDArrayUtils.linearHConcat(targetVectorElements);
        } else {
            //if in test mode create empty array
            targetVector = Nd4j.create(1);
        }

        validateDataSet(featureVector, targetVector, row.getKey());
        return new DataSet(featureVector, targetVector);
    }
}
