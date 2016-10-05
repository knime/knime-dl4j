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
package org.knime.ext.dl4j.base.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deeplearning4j.nn.conf.layers.FeedForwardLayer;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.LocalResponseNormalization;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.nodes.layer.DNNType;

import com.google.common.collect.Lists;

/**
 * Utility class to validate deep neural network configurations and {@link DLModelPortObjectSpec}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class ConfigurationUtils {

    private ConfigurationUtils() {
        // Utility class
    }

    /**
     * Validates the given specification. Checks if the given {@link DNNType}s are compatible with the spec. Checks if
     * the layer types contained in the spec are compatible with each other. Checks if the number of outputs of each
     * layer is the same as the number of inputs of the next layer. If no exception is thrown the specification seems to
     * be valid, however there may be warning messages.
     *
     * @param spec the spec which we want to validate
     * @param types the {@link DNNType} of the current layer
     * @return warning messages giving possible problems of spec, returns empty list if no problems were discovered
     * @throws InvalidSettingsException
     */
    public static List<String> validateSpec(final DLModelPortObjectSpec spec, final List<DNNType> types)
        throws InvalidSettingsException {
        final List<String> warnings = new ArrayList<>();

        warnings.addAll(validateType(spec, types));
        return warnings;
    }

    /**
     * Checks if the specified column selection is present in the specified {@link DataTableSpec} and if the selection
     * is empty.
     *
     * @param tableSpec the spec of the table
     * @param columnSelection the selected column/s
     * @throws InvalidSettingsException if no columns are selected if selected columns are not available in the table
     */
    public static void validateColumnSelection(final DataTableSpec tableSpec,
        final SettingsModelFilterString columnSelection) throws InvalidSettingsException {
        final List<String> selectedColumns = columnSelection.getIncludeList();
        for (final String columnName : selectedColumns) {
            validateColumnSelection(tableSpec, columnName);
        }
    }

    /**
     * Checks if the specified column selection is present in the specified {@link DataTableSpec} and if the selection
     * is empty.
     *
     * @param tableSpec the spec of the table
     * @param columnSelection the selected column
     * @throws InvalidSettingsException if no columns are selected if selected columns are not available in the table
     */
    public static void validateColumnSelection(final DataTableSpec tableSpec, final SettingsModelString columnSelection)
        throws InvalidSettingsException {
        final String selectedColumn = columnSelection.getStringValue();
        validateColumnSelection(tableSpec, selectedColumn);
    }

    /**
     * Checks if the specified column selection is present in the specified {@link DataTableSpec} and if the selection
     * is empty.
     *
     * @param tableSpec the spec of the table
     * @param columnSelection the selected column
     * @throws InvalidSettingsException if no columns are selected if selected columns are not available in the table
     */
    public static void validateColumnSelection(final DataTableSpec tableSpec, final String columnSelection)
        throws InvalidSettingsException {
        if (columnSelection.isEmpty()) {
            throw new InvalidSettingsException("No input columns selected");
        }
        if (!tableSpec.containsName(columnSelection)) {
            throw new InvalidSettingsException("Input column not available in table");
        }
    }

    /**
     * Checks if the specified column selection is present in the specified {@link DataTableSpec} and if the selection
     * is empty.
     *
     * @param tableSpec the spec of the table
     * @param columnSelection the selected column/s
     * @throws InvalidSettingsException if no columns are selected if selected columns are not available in the table
     */
    public static void validateColumnSelection(final DataTableSpec tableSpec, final String[] columnSelection)
        throws InvalidSettingsException {
        if (columnSelection.length == 0) {
            throw new InvalidSettingsException("No input columns selected");
        }
        for (final String columnName : columnSelection) {
            validateColumnSelection(tableSpec, columnName);
        }
    }

    /**
     * Checks if the specified column selection is present in the specified {@link DataTableSpec} and if the selection
     * is empty.
     *
     * @param tableSpec the spec of the table
     * @param columnSelection the selected column/s
     * @throws InvalidSettingsException if no columns are selected if selected columns are not available in the table
     */
    public static void validateColumnSelection(final DataTableSpec tableSpec, final List<String> columnSelection)
        throws InvalidSettingsException {
        if (columnSelection.isEmpty()) {
            throw new InvalidSettingsException("No input columns selected");
        }
        for (final String columnName : columnSelection) {
            validateColumnSelection(tableSpec, columnName);
        }
    }

    /**
     * Checks two {@link SettingsModelFilterString} if the include lists contain duplicate columns.
     *
     * @param fs1
     * @param fs2
     * @throws InvalidSettingsException if the lists contain duplicate columns
     */
    public static void validateMutuallyExclusive(final SettingsModelFilterString fs1,
        final SettingsModelFilterString fs2) throws InvalidSettingsException {
        validateMutuallyExclusive(fs1.getIncludeList(), fs2.getIncludeList());
    }

    /**
     * Checks two arrays if they contain duplicate columns.
     *
     * @param cols1
     * @param cols2
     * @throws InvalidSettingsException if the lists contain duplicate columns
     */
    public static void validateMutuallyExclusive(final String[] cols1, final String[] cols2)
        throws InvalidSettingsException {
        validateMutuallyExclusive(Lists.newArrayList(cols1), Lists.newArrayList(cols2));
    }

    /**
     *
     * Checks two lists if they contain duplicate columns.
     *
     * @param cols1
     * @param cols2
     * @throws InvalidSettingsException if the lists contain duplicate columns
     */
    public static void validateMutuallyExclusive(final List<String> cols1, final List<String> cols2)
        throws InvalidSettingsException {
        for (String s : cols1) {
            if (cols2.contains(s)) {
                throw new InvalidSettingsException(
                    "The lists of feature and target columns must be mutually exclusive. Offending column: " + s);
            }
        }
    }

    /**
     * Checks if the {@link DNNType}s contained in the spec are compatible with the {@link DNNType}s of this node.
     *
     * @param spec
     * @param types of this node
     * @return list of warnings
     */
    private static List<String> validateType(final DLModelPortObjectSpec spec, final List<DNNType> types) {
        final List<String> warnings = new ArrayList<>();
        final List<DNNType> intersectOfTypes = new ArrayList<>(spec.getNeuralNetworkTypes());

        //calc intersection between types in spec and types of this node
        intersectOfTypes.retainAll(types);
        if (intersectOfTypes.isEmpty() && !spec.getNeuralNetworkTypes().contains(DNNType.EMPTY)) {
            warnings.add(typesToString(types) + " may be incompatible with the " + "current network architecture. "
                + "this architecture: " + typesToString(types) + " current architecture: "
                + typesToString(spec.getNeuralNetworkTypes()));
        }
        return warnings;
    }

    /**
     * Converts list of enums to single string where every string representation of the enum is separated by a "OR" in
     * the returned string.
     *
     * @param types
     * @return string containing string representation of enum separated by "OR"
     */
    public static <E extends Enum<E>> String typesToString(final List<E> types) {
        String typesToString = "";
        for (int i = 0; i < types.size(); i++) {
            typesToString += types.get(i).toString();
            if ((i + 1) != types.size()) {
                typesToString += " or ";
            }
        }
        return typesToString;
    }

    /**
     * Creates a list of pairs (name,type) containing the column names and type description of selected columns from a
     * table.
     *
     * @param featureColumns names of selected columns
     * @param tableSpec the table spec corresponding to the table where the columns are selected from
     * @return pairs containing name and type of selected columns
     * @throws InvalidSettingsException
     */
    public static List<Pair<String, String>> createNameTypeListOfSelectedCols(final List<String> featureColumns,
        final DataTableSpec tableSpec) throws InvalidSettingsException {
        final List<Pair<String, String>> inputs = new ArrayList<>();
        for (final String colName : featureColumns) {
            final DataColumnSpec colSpec = tableSpec.getColumnSpec(colName);
            final String type = colSpec.getType().getName();
            inputs.add(new Pair<String, String>(colName, type));
        }
        return inputs;
    }

    /**
     * Check if the specified spec contains an image column.
     *
     * @param spec
     * @return true if the spec contains a column whose type name contains the string "Image", false if not
     */
    public static boolean containsImg(final DataTableSpec spec) {
        final Iterator<DataColumnSpec> colSpecs = spec.iterator();
        while (colSpecs.hasNext()) {
            final DataColumnSpec colSpec = colSpecs.next();
            if (colSpec.getType().getName().contains("Image")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets up the input/output numbers of the specified list of layers. The number of inputs of the first layer will be
     * set to the specified value. Usually this value can be calculated from the data. The numbers for possible more
     * layers will adjusted such that the number of outputs from one layer matches the number of inputs from the next.
     * It is expected that the number of outputs is correctly set for each layer so the number of inputs can be
     * inferred.<br>
     * <br>
     *
     * SubsamplingLayer and LocalResponseNormalization will be left untouched as they are no FeedForwardLayer (strange
     * layer hierarchy from DL4J, e.g. recurrent layers extend {@link FeedForwardLayer}. May cause problems with next
     * DL4J versions).
     *
     * @param layers the list of layers to set up
     * @param numberOfInputs the number of inputs for the first layer
     * @deprecated use 'org.deeplearning4j.nn.conf.MultiLayerConfiguration.Builder.setInputType(InputType inputType)' instead
     */
    @Deprecated
    public static void setupLayers(final List<Layer> layers, final int numberOfInputs) {
        FeedForwardLayer ffl = null;
        if (layers.isEmpty()) {
            return;
        } else {
            ffl = (FeedForwardLayer)layers.get(0);
        }
        //set the number of inputs to the inferred number of inputs from the data for the
        //first layer
        ffl.setNIn(numberOfInputs);
        //get user specified number of outputs from first layer
        int previousOutNum = ffl.getNOut();
        //start from second layer
        for (int i = 1; i < layers.size(); i++) {
            //SubsamplingLayer and LocalResponseNormalization are not FeedForwardLayer so skip
            if ((layers.get(i) instanceof SubsamplingLayer) || (layers.get(i) instanceof LocalResponseNormalization)) {
                continue;
            }
            ffl = (FeedForwardLayer)layers.get(i);
            //set number of inputs to number of outputs of previous layer
            ffl.setNIn(previousOutNum);
            //save number of outputs of current layer
            previousOutNum = ffl.getNOut();
        }
    }

    /**
     * Check if the specified list of layer types contains a layer that can be trained unsupervised.
     *
     * @param layerTypes the list of layer types to check
     * @return true if a layer can be trained unsupervised, else false
     */
    public static boolean containsUnsupervised(final List<DNNLayerType> layerTypes) {
        if (layerTypes.contains(DNNLayerType.AUTOENCODER)) {
            return true;
        } else if (layerTypes.contains(DNNLayerType.RBM_LAYER)) {
            return true;
        }
        return false;
    }

    /**
     * Check if the specified list of layer types contains a layer that can be trained supervised.
     *
     * @param layerTypes the list of layer types to check
     * @return true if a layer can be trained supervised, else false
     */
    public static boolean containsSupervised(final List<DNNLayerType> layerTypes) {
        if (layerTypes.contains(DNNLayerType.CONVOLUTION_LAYER)) {
            return true;
        } else if (layerTypes.contains(DNNLayerType.DENSE_LAYER)) {
            return true;
        } else if (layerTypes.contains(DNNLayerType.GRAVES_LSTM)) {
            return true;
        } else if (layerTypes.contains(DNNLayerType.GRU)) {
            return true;
        }
        return false;
    }
}
