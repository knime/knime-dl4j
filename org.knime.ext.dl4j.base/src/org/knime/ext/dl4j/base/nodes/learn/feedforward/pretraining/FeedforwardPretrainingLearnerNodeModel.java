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
package org.knime.ext.dl4j.base.nodes.learn.feedforward.pretraining;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.data.iter.PretrainingBufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.mln.MultiLayerNetFactory2;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.nodes.learn.AbstractDLLearnerNodeModel;
import org.knime.ext.dl4j.base.nodes.learn.view.UpdateLearnerViewIterationListener;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.knime.ext.dl4j.base.settings.enumerate.LayerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JActivationFunction;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JLossFunction;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels2;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels2;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import com.google.common.collect.Lists;

/**
 * Learner for feedforward networks of Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class FeedforwardPretrainingLearnerNodeModel extends AbstractDLLearnerNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(FeedforwardPretrainingLearnerNodeModel.class);

    /* SettingsModels */
    private LearnerParameterSettingsModels2 m_learnerParameterSettings;

    private DataParameterSettingsModels2 m_dataParameterSettings;

    private LayerParameterSettingsModels2 m_layerParameterSettings;

    /**
     * Constructor for the node model.
     */
    protected FeedforwardPretrainingLearnerNodeModel() {
        super(new PortType[]{DLModelPortObject.TYPE, BufferedDataTable.TYPE}, new PortType[]{DLModelPortObject.TYPE});
    }

    @Override
    protected DLModelPortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        final DLModelPortObject portObject = (DLModelPortObject)inData[0];
        final BufferedDataTable table = (BufferedDataTable)inData[1];

        //select columns from input table
        final List<String> selectedColumns = new ArrayList<>();
        SettingsModelColumnFilter2 featureColumnsFilter =
            (SettingsModelColumnFilter2)m_dataParameterSettings.getParameter(DataParameter.FEATURE_COLUMN_SELECTION2);
        String[] includes = featureColumnsFilter.applyTo(table.getSpec()).getIncludes();
        selectedColumns.addAll(Lists.newArrayList(includes));

        //select columns from table
        ColumnRearranger crr = new ColumnRearranger(table.getSpec());
        crr.keepOnly(selectedColumns.toArray(new String[selectedColumns.size()]));
        final BufferedDataTable selectedTable = exec.createColumnRearrangeTable(table, crr, exec);

        //create input iterator
        final int batchSize = m_dataParameterSettings.getInteger(DataParameter.BATCH_SIZE);

        TableUtils.checkForEmptyTable(selectedTable);
        DataSetIterator input = new PretrainingBufferedDataTableDataSetIterator(selectedTable, batchSize, true);

        //build multi layer net
        final List<Layer> layers = portObject.getLayers();
        final MultiLayerNetwork oldMln = portObject.getMultilayerLayerNetwork();

        //check if list of layers already contains output layer, happens if
        //several learners are used in sequence
        if (checkOutputLayer(layers)) {
            //if so first remove the old output layer
            layers.remove(layers.size() - 1);
        }
        //add the new output layer
        layers.add(createOutputLayer(m_layerParameterSettings));

        MultiLayerNetFactory2 mlnFactory;
        MultiLayerNetwork newMln;
        //network creation seems not to be thread safe
        //TODO review this
        synchronized (logger) {
            mlnFactory = new MultiLayerNetFactory2(input.inputColumns());
            newMln = mlnFactory.createMultiLayerNetwork(layers, m_learnerParameterSettings);
        }

        //attempt to transfer weights between nets
        final boolean useUpdater = m_learnerParameterSettings.getBoolean(LearnerParameter.USE_UPDATER);
        transferFullInitialization(oldMln, newMln, !useUpdater);

        //set listener that updates the view and the score of this model
        newMln.setListeners(new UpdateLearnerViewIterationListener(this));

        //train the network
        final int epochs = m_dataParameterSettings.getInteger(DataParameter.EPOCHS);
        trainNetwork(newMln, epochs, input, exec);

        final DLModelPortObject newPortObject = new DLModelPortObject(layers, newMln, m_outputSpec);
        return new DLModelPortObject[]{newPortObject};
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec tableSpec = (DataTableSpec)inSpecs[1];

        SettingsModelColumnFilter2 featureColumnsFilter =
                (SettingsModelColumnFilter2)m_dataParameterSettings.getParameter(DataParameter.FEATURE_COLUMN_SELECTION2);
        String[] includes = featureColumnsFilter.applyTo(tableSpec).getIncludes();

        final DLModelPortObjectSpec specWithoutLabels =
            (DLModelPortObjectSpec)configure(inSpecs, Lists.newArrayList(includes))[0];

        logger.info("Constructed network recognized as: "
            + ConfigurationUtils.typesToString(specWithoutLabels.getNeuralNetworkTypes()));

        //the network needs to contain layers which can be trained unsupervised
        if (!ConfigurationUtils.containsUnsupervised(specWithoutLabels.getLayerTypes())) {
            throw new InvalidSettingsException(
                "Can't perform pretraining because network does not contain layers that can be trained unsupervised.");
        }

        //if several learners are used in sequence the spec already contains an output layer
        final List<DNNLayerType> newLayerTypes = new ArrayList<DNNLayerType>();
        newLayerTypes.addAll(specWithoutLabels.getLayerTypes());
        if (newLayerTypes.isEmpty() || !newLayerTypes.get(newLayerTypes.size() - 1).equals(DNNLayerType.OUTPUT_LAYER)) {
            newLayerTypes.add(DNNLayerType.OUTPUT_LAYER);
        }

        //create new spec and set labels
        m_outputSpec = new DLModelPortObjectSpec(specWithoutLabels.getNeuralNetworkTypes(), newLayerTypes,
            specWithoutLabels.getLearnedColumns(), specWithoutLabels.getLabels(), specWithoutLabels.isTrained());

        return new DLModelPortObjectSpec[]{m_outputSpec};
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        final String momentumAfter = settings.getString(LearnerParameter.MOMENTUM_AFTER.toString().toLowerCase());
        ParameterUtils.validateMomentumAfterParameter(momentumAfter);

        super.validateSettings(settings);
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_dataParameterSettings = new DataParameterSettingsModels2();
        m_dataParameterSettings.setParameter(DataParameter.BATCH_SIZE);
        m_dataParameterSettings.setParameter(DataParameter.EPOCHS);
        m_dataParameterSettings.setParameter(DataParameter.FEATURE_COLUMN_SELECTION2);

        m_learnerParameterSettings = new LearnerParameterSettingsModels2();
        m_learnerParameterSettings.setParameter(LearnerParameter.ADADELTA_RHO);
        m_learnerParameterSettings.setParameter(LearnerParameter.ADAM_MEAN_DECAY);
        m_learnerParameterSettings.setParameter(LearnerParameter.ADAM_VAR_DECAY);
        m_learnerParameterSettings.setParameter(LearnerParameter.BIAS_INIT);
        m_learnerParameterSettings.setParameter(LearnerParameter.BIAS_LEARNING_RATE);
        m_learnerParameterSettings.setParameter(LearnerParameter.DISTRIBUTION);
        m_learnerParameterSettings.setParameter(LearnerParameter.DISTRIBUTION_BINOMIAL_PROBABILITY);
        m_learnerParameterSettings.setParameter(LearnerParameter.DISTRIBUTION_BINOMIAL_TRAILS);
        m_learnerParameterSettings.setParameter(LearnerParameter.DISTRIBUTION_LOWER_BOUND);
        m_learnerParameterSettings.setParameter(LearnerParameter.DISTRIBUTION_UPPER_BOUND);
        m_learnerParameterSettings.setParameter(LearnerParameter.DISTRIBUTION_MEAN);
        m_learnerParameterSettings.setParameter(LearnerParameter.DISTRIBUTION_STD);
        m_learnerParameterSettings.setParameter(LearnerParameter.GLOBAL_DROP_OUT);
        m_learnerParameterSettings.setParameter(LearnerParameter.GLOBAL_LEARNING_RATE);
        m_learnerParameterSettings.setParameter(LearnerParameter.GLOBAL_WEIGHT_INIT);
        m_learnerParameterSettings.setParameter(LearnerParameter.GRADIENT_NORMALIZATION);
        m_learnerParameterSettings.setParameter(LearnerParameter.GRADIENT_NORMALIZATION_THRESHOLD);
        m_learnerParameterSettings.setParameter(LearnerParameter.L1);
        m_learnerParameterSettings.setParameter(LearnerParameter.L2);
        m_learnerParameterSettings.setParameter(LearnerParameter.MAX_NUMBER_LINE_SEARCH_ITERATIONS);
        m_learnerParameterSettings.setParameter(LearnerParameter.MOMENTUM);
        m_learnerParameterSettings.setParameter(LearnerParameter.MOMENTUM_AFTER);
        m_learnerParameterSettings.setParameter(LearnerParameter.OPTIMIZATION_ALGORITHM);
        m_learnerParameterSettings.setParameter(LearnerParameter.RMS_DECAY);
        m_learnerParameterSettings.setParameter(LearnerParameter.SEED);
        m_learnerParameterSettings.setParameter(LearnerParameter.TRAINING_ITERATIONS);
        m_learnerParameterSettings.setParameter(LearnerParameter.UPDATER);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_BIAS_INIT);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_BIAS_LEARNING_RATE);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_DROP_CONNECT);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_GLOBAL_DROP_OUT);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_GLOBAL_LEARNING_RATE);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_GLOBAL_WEIGHT_INIT);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_GRADIENT_NORMALIZATION);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_REGULARIZATION);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_SEED);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_UPDATER);

        m_layerParameterSettings = new LayerParameterSettingsModels2();
        m_layerParameterSettings.setParameter(LayerParameter.LOSS_FUNCTION);
        m_layerParameterSettings.setParameter(LayerParameter.NUMBER_OF_OUTPUTS);
        m_layerParameterSettings.setParameter(LayerParameter.ACTIVATION);
        m_layerParameterSettings.setParameter(LayerParameter.WEIGHT_INIT);
        m_layerParameterSettings.setParameter(LayerParameter.LEARNING_RATE);

        final List<SettingsModel> settings = new ArrayList<>();
        settings.addAll(m_learnerParameterSettings.getAllInitializedSettings());
        settings.addAll(m_dataParameterSettings.getAllInitializedSettings());
        settings.addAll(m_layerParameterSettings.getAllInitializedSettings());

        return settings;
    }

    /**
     * Performs training of the specified {@link MultiLayerNetwork} (whether to do backprop or finetuning or pretraining
     * is set in model configuration) for the specified number of epochs using the specified {@link DataSetIterator} and
     * specified {@link ExecutionContext} for progress reporting and execution cancelling.
     *
     * @param mln the network to train
     * @param epochs the number of epochs to train
     * @param data the data to train on
     * @param exec
     * @throws Exception
     */
    private void trainNetwork(final MultiLayerNetwork mln, final int epochs, final DataSetIterator data,
        final ExecutionContext exec) throws Exception {
        // do only backprop for regression
        mln.getLayerWiseConfigurations().setPretrain(true);

        exec.setProgress(0.0);

        //calculate progress relative to number of epochs and what to train
        double maxProgress = epochs;

        logger.info("Pretrain Model for " + epochs + " epochs.");
        for (int i = 0; i < epochs; i++) {
            exec.checkCanceled();
            if (getLearningMonitor().checkStopLearning()) {
                break;
            }
            logger.info("Pretrain epoch: " + (i + 1) + " of: " + epochs);

            updateView(i + 1, epochs, "Pretrain");
            pretrainOneEpoch(mln, data, exec);

            logEpochScore(mln, (i + 1));
            data.reset();
            exec.setProgress((i + 1) / maxProgress);
        }
    }

    /**
     * Creates an new {@link OutputLayer} using the specified layer parameters. All settings are taken from the
     * specified {@link LayerParameterSettingsModels}.
     *
     * @param settings the parameter settings models object holding the layer parameter
     * @return a new output layer using the specified parameters
     * @throws InvalidSettingsException if supervised training mode but labels are not available, meaning null or empty
     */
    private OutputLayer createOutputLayer(final LayerParameterSettingsModels2 settings)
        throws InvalidSettingsException {
        int nOut = settings.getInteger(LayerParameter.NUMBER_OF_OUTPUTS);
        final WeightInit weight = WeightInit.valueOf(settings.getString(LayerParameter.WEIGHT_INIT));
        final String activation =
            DL4JActivationFunction.fromToString(settings.getString(LayerParameter.ACTIVATION)).getDL4JValue();
        final LossFunction loss =
            DL4JLossFunction.fromToString(settings.getString(LayerParameter.LOSS_FUNCTION)).getDL4JValue();
        final double learningRate = settings.getDouble(LayerParameter.LEARNING_RATE);

        return new OutputLayer.Builder(loss).nOut(nOut).activation(activation).weightInit(weight)
            .learningRate(learningRate).build();
    }
}
