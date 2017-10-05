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
package org.knime.ext.dl4j.base.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.vector.doublevector.DenseDoubleVectorCell;
import org.knime.core.data.vector.doublevector.DoubleVectorCellFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Utility class for {@link INDArray}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class NDArrayUtils {

    private NDArrayUtils() {
        // Utility class
    }

    /**
     * Converts a INDArray to a List of Double Cells.
     *
     * @param array the INDArray to convert
     * @return List of DoubleCells containing the values from the specified array
     */
    public static List<DoubleCell> toListOfDoubleCells(final INDArray array) {
        final List<DoubleCell> cells = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            cells.add(new DoubleCell(array.getDouble(i)));
        }
        return cells;
    }

    /**
     * Converts a INDArray to a DenseDoubleVectorCell.
     *
     * @param array the INDArray to convert
     * @return a DoubleVector containing the values from the specified array
     */
    public static DenseDoubleVectorCell toDoubleVector(final INDArray array) {
        double[] doubleArray = new double[array.length()];
        for (int i = 0; i < array.length(); i++) {
            doubleArray[i] = array.getDouble(i);
        }
        return DoubleVectorCellFactory.createCell(doubleArray);
    }

    /**
     * Copies {@link DoubleValue}s contained in {@link CollectionDataValue} to {@link INDArray}.
     *
     * @param cell the collection cell to convert
     * @return INDArray containing double values of collection cell
     */
    public static INDArray fromListOfDoubleValues(final CollectionDataValue cell) {
        final Iterator<DataCell> iter = cell.iterator();
        final INDArray doubles = Nd4j.create(cell.size());

        int i = 0;
        while (iter.hasNext()) {
            final DoubleValue doubleValue = (DoubleValue)iter.next();
            doubles.putScalar(i, doubleValue.getDoubleValue());
            i++;
        }

        return doubles;
    }

    /**
     * Corresponds a INDArray containing a softmax activation array with the label with the highest probability. Expects
     * one dimensional INDArray with length equal to the number of specified labels. It is expected that the INDArray
     * holds class probabilities, which are mapped to each of the specified labels by index.
     *
     * @param labels labels corresponding to positions in softmax activation array
     * @param softmaxActivation softmax activation array
     * @return the label corresponding to the highest probability
     * @throws IllegalArgumentException if number of labels doesn't match length of softmax activation array
     */
    public static String softmaxActivationToLabel(final List<String> labels, final INDArray softmaxActivation)
        throws IllegalArgumentException {
        if (labels.size() != softmaxActivation.length()) {
            throw new IllegalArgumentException("The number of labels: " + labels.size()
                + " does not match the length of the softmaxActivation " + "vector: " + softmaxActivation.length());
        }
        final List<Double> classProbabilities = new ArrayList<>();
        for (int i = 0; i < softmaxActivation.length(); i++) {
            classProbabilities.add(softmaxActivation.getDouble(i));
        }
        final double max = softmaxActivation.max(1).getDouble(0);
        final int indexOfMax = classProbabilities.indexOf(max);

        return labels.get(indexOfMax);
    }

    /**
     * Horizontally concatenates arrays contained in the specified list.
     *
     * @param arrs the arrays which should be concatenated
     * @return array concatenated array or null if list is empty
     */
    public static INDArray linearHConcat(final List<INDArray> arrs) {
        if (arrs.isEmpty()) {
            return null;
        }
        return Nd4j.hstack(arrs);
    }
}
