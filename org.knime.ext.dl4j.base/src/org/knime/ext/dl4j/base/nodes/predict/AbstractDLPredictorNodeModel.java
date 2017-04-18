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
package org.knime.ext.dl4j.base.nodes.predict;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.nn.conf.layers.FeedForwardLayer;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.exception.DL4JVersionCompatibilityException;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JActivationFunction;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Abstract superclass for predictor node models of Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public abstract class AbstractDLPredictorNodeModel extends AbstractDLNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(AbstractDLPredictorNodeModel.class);

    private boolean m_inputTableContainsImg;

    private boolean m_containsLabels;

    private boolean m_containsTargetNames;

    /**
     * Super constructor for class AbstractDLPredictorNodeModel passing through parameters to node model class.
     *
     * @param inPortTypes
     * @param outPortTypes
     */
    protected AbstractDLPredictorNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final DLModelPortObjectSpec modelSpec = (DLModelPortObjectSpec)inSpecs[0];
        final DataTableSpec predictTableSpec = (DataTableSpec)inSpecs[1];

        if (!modelSpec.isTrained()) {
            throw new InvalidSettingsException("Model not yet trained. Can't predict with untrained model.");
        }

        m_inputTableContainsImg = ConfigurationUtils.containsImg(predictTableSpec);

        if ((modelSpec.getLabels() == null) || modelSpec.getLabels().isEmpty()) {
            m_containsLabels = false;
        } else {
            m_containsLabels = true;
        }

        if (modelSpec.getTargetColumnNames() == null || modelSpec.getTargetColumnNames().isEmpty()) {
            m_containsTargetNames = false;
        } else {
            m_containsTargetNames = true;
        }

        checkInputTableForFeatureColumns(predictTableSpec, modelSpec.getLearnedColumns());

        //check for spec sanity
        logWarnings(logger, ConfigurationUtils.validateSpec(modelSpec, modelSpec.getNeuralNetworkTypes()));
        return inSpecs;
    }

    /**
     * @return the contains image flag
     */
    protected boolean inputTableContainsImg() {
        return m_inputTableContainsImg;
    }

    /**
     * @return the contains labels flag
     */
    protected boolean containsLabels() {
        return m_containsLabels;
    }

    /**
     * @return the contains target names flag
     */
    protected boolean containsTargetNames() {
        return m_containsTargetNames;
    }

    /**
     * Checks if the specified list of feature columns is contained in the specified spec.
     *
     * @param spec the table spec to check for feature columns
     * @param expectedCols the feature columns
     * @throws InvalidSettingsException if spec does not contain a column of the feature columns if it contains the
     *             columns but one of it is not of the same type
     */
    private void checkInputTableForFeatureColumns(final DataTableSpec spec,
        final List<Pair<String, String>> expectedCols) throws InvalidSettingsException {
        for (final Pair<String, String> c : expectedCols) {
            if (spec.containsName(c.getFirst())) {
                final DataColumnSpec colSpec = spec.getColumnSpec(c.getFirst());

                String specType = colSpec.getType().getName();
                String expectedType = c.getSecond();

                // Backwards compatibility, Collections and Lists are interchangeable
                if(expectedType.equals("Collection")){
                    expectedType = "List";
                }

                if (!specType.equals(expectedType)) {
                    throw new InvalidSettingsException(
                        "Table contains column: " + c.getFirst() + " but the column was not of expected type. Expected type: '"
                            + c.getSecond() + "' but was: '" + colSpec.getType().getName() + "'. Maybe the column was renamed.");
                }
            } else {
                throw new InvalidSettingsException("Table does not contain expected input column: " + c.getFirst());
            }
        }
    }

    /**
     * Checks if the last layer of the specified list of layers has the specified activation function.
     *
     * @param layers the list of layers to check
     * @param activation the activation function to check for
     * @return true if activation of the last layer equals the specified activation, false if not
     */
    protected boolean isOutActivation(final List<Layer> layers, final DL4JActivationFunction activation) {
        final Layer outputLayer = layers.get(layers.size() - 1);
        IActivation activationFn = outputLayer.getActivationFn();

        /* Compatibility issue between dl4j 0.6 and 0.8 due to API change of DL4J. Activations changed from
         * Strings to an interface. Therefore, if a model was saved with 0.6 the corresponding member
         * of the layer object will contain null. Old method to retrieve String representation of the
         * activation function was removed. */
        if (activationFn == null) {
            String msg = "DL4J version compatibility problem. Provided model may be "
                + "trained with an older version of DL4J. Please re-execute the Learner Node.";
            logger.debug("Activation function of output layer is null.");
            throw new DL4JVersionCompatibilityException(msg);
        }

        return outputLayer.getActivationFn().equals(activation.getDL4JValue().getActivationFunction());
    }

    /**
     * Determines the number of outputs of a {@link MultiLayerNetwork}, hence the number of outputs of the last layer
     * which must be a {@link OutputLayer}.
     *
     * @param mln the network to use
     * @return number of outputs of the network
     * @throws RuntimeException if the last layer is not an output layer
     */
    protected int getNumberOfOutputs(final MultiLayerNetwork mln) {
        final int numberOfLayers = mln.getLayerWiseConfigurations().getConfs().size();

        final Layer l = mln.getLayerWiseConfigurations().getConf(numberOfLayers - 1).getLayer();
        if (l instanceof OutputLayer) {
            return ((OutputLayer)l).getNOut();
        } else {
            throw new RuntimeException("Last layer is not a Output Layer");
        }
    }

    /**
     * Determines the number of outputs of the layer with specified index contained in the specified network.
     *
     * @param mln the network to use
     * @param layerIndex the index of the layer to get the number of outputs from
     * @return the number of output neurons of the specified layer
     */
    protected int getNumberOfOutputs(final MultiLayerNetwork mln, final int layerIndex) {
        final int numberOfLayers = mln.getLayerWiseConfigurations().getConfs().size();
        if (layerIndex > numberOfLayers - 1) {
            throw new ArrayIndexOutOfBoundsException("No layer with index " + layerIndex + " available!");
        }
        Layer l = mln.getLayerWiseConfigurations().getConf(layerIndex).getLayer();
        if (l instanceof FeedForwardLayer) {
            return ((FeedForwardLayer)l).getNOut();
        } else {
            throw new RuntimeException("Can't get number of outputs from non FeedforwardLayer.");
        }
    }

    /**
     * Creates output for an input {@link INDArray}. The input array must contain each example to predict in a row.
     * Returns a {@link INDArray} with 'number of outputs' columns and 'number of examples' rows, whereby the number of
     * examples is the number of rows of the input array.
     *
     * @param mln the network to use for prediction
     * @param input the input used to create output
     * @return array containing the output of the network for each row of the input
     */
    protected INDArray predict(final MultiLayerNetwork mln, final INDArray input) {
        final INDArray output = Nd4j.create(input.rows(), getNumberOfOutputs(mln));
        for (int i = 0; i < input.rows(); i++) {
            output.putRow(i, mln.output(input.getRow(i), false));
        }
        return output;
    }

    /**
     * Activates the specified layer in the specified network with the specified input. The inpu array should contain
     * one example per row.
     *
     * @param mln the network to use
     * @param layer the layer to activate
     * @param input the inputs to use
     * @return the activations of the layer for the input
     */
    protected INDArray activate(final MultiLayerNetwork mln, final int layer, final INDArray input) {
        final List<INDArray> output = new ArrayList<INDArray>();
        for (int i = 0; i < input.rows(); i++) {
            List<INDArray> activations = mln.feedForward(input.getRow(i), false);
            output.add(activations.get(layer + 1));
        }
        return Nd4j.hstack(output);
    }

    /**
     * Creates an empty table using the specified ExecutionContext and TableSpec.
     *
     * @param exec the context to use for table creation
     * @param spec the spec of the empty table
     * @return PortObject array containing one empty table with specified spec
     */
    protected PortObject[] createEmptyTable(final ExecutionContext exec, final DataTableSpec spec) {
        logger.warn("Can't predict with empty inpup table!");
        final BufferedDataContainer emptyContainer = exec.createDataContainer(spec);
        emptyContainer.close();
        return new PortObject[]{emptyContainer.getTable()};
    }
}
