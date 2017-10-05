/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   27.07.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.data.convert.row;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Implementation of {@link AbstractDataRowToDataSetConverter} for rows containing features and no label or target
 * columns. If in train mode the target will be the same as the feature.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class ReconstructionTargetDataRowToDataSetConverter extends AbstractDataRowToDataSetConverter {

    /**
     * Constructor for class ReconstructionTargetDataRowToDataSetConverter specifying a reference row for expected
     * feature length calculation and if in train mode.
     *
     * @param referenceRow a reference row
     * @param isTrain if in train mode or not
     * @throws Exception
     */
    public ReconstructionTargetDataRowToDataSetConverter(final DataRow referenceRow, final boolean isTrain)
        throws Exception {
        super(isTrain);

        final int featureLength = TableUtils.calculateFeatureVectorLength(referenceRow);
        setFeatureLength(featureLength);

        if (!isTrain) {
            setTargetLength(-1);
        } else {
            //set target length to the same value as feature length in train mode as target
            //will contain the same values as feature
            setTargetLength(featureLength);
        }
    }

    /**
     * {@inheritDoc} Features and label are converted to a flat vector. In test mode label will be empty.
     */
    @Override
    public DataSet convert(final DataRow row) throws Exception {
        INDArray featureVector = Nd4j.create(featureLength());
        int featureVectorCursor = 0;

        INDArray targetVector = null;

        for (DataCell cell : row) {
            int newCursorPos = putCellToVector(featureVector, cell, featureVectorCursor);
            featureVectorCursor = newCursorPos;
        }

        //array not fully filled with data
        if (featureVectorCursor != featureLength()) {
            throw new IllegalArgumentException(
                "Length of current input does not match expected length. Possible images or collections "
                    + "may not be of same size.");
        }
        if (isTrain()) {
            targetVector = featureVector;
        } else {
            //if in test mode create empty array
            targetVector = Nd4j.create(1);
        }

        validateDataSet(featureVector, targetVector);
        return new DataSet(featureVector, targetVector);
    }

}
