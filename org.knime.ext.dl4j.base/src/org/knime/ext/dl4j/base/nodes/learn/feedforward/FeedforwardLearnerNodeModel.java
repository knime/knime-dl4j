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
package org.knime.ext.dl4j.base.nodes.learn.feedforward;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.knime.base.data.filter.column.FilterColumnTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.data.iter.BufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.exception.UnsupportedDataTypeException;
import org.knime.ext.dl4j.base.mln.ConvMultiLayerNetFactory;
import org.knime.ext.dl4j.base.mln.MultiLayerNetFactory;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.nodes.learn.AbstractDLLearnerNodeModel;
import org.knime.ext.dl4j.base.nodes.learn.LearningStatus;
import org.knime.ext.dl4j.base.nodes.learn.UpdateLearnerViewIterationListener;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.knime.ext.dl4j.base.settings.enumerate.LayerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.TrainingMode;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JActivationFunction;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JLossFunction;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 * Learner for feedforward networks of Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class FeedforwardLearnerNodeModel extends AbstractDLLearnerNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(FeedforwardLearnerNodeModel.class);

    /* SettingsModels */
    private LearnerParameterSettingsModels m_learnerParameterSettings;

    private DataParameterSettingsModels m_dataParameterSettings;

    private LayerParameterSettingsModels m_layerParameterSettings;

    private List<String> m_labels = new ArrayList<>();

    /**
     * Constructor for the node model.
     */
    protected FeedforwardLearnerNodeModel() {
        super(new PortType[]{DLModelPortObject.TYPE, BufferedDataTable.TYPE}, new PortType[]{DLModelPortObject.TYPE});
    }

    @Override
    protected DLModelPortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        final DLModelPortObject portObject = (DLModelPortObject)inData[0];
        final BufferedDataTable table = (BufferedDataTable)inData[1];

        final TrainingMode trainingMode =
            TrainingMode.valueOf(m_learnerParameterSettings.getTrainingsMode().getStringValue());
        //select columns from input table
        final List<String> selectedColumns = new ArrayList<>();
        selectedColumns.addAll(m_dataParameterSettings.getColumnSelection().getIncludeList());
        final String labelColumnName = m_dataParameterSettings.getLabelColumn().getStringValue();

        if ((labelColumnName != null) && !labelColumnName.isEmpty() && trainingMode.equals(TrainingMode.SUPERVISED)) {
            selectedColumns.add(labelColumnName);
        }

        final FilterColumnTable selectedTable =
            new FilterColumnTable(table, selectedColumns.toArray(new String[selectedColumns.size()]));
        final BufferedDataTable bufferedSelectedTable = exec.createBufferedDataTable(selectedTable, exec);

        //create input iterator
        final int batchSize = m_dataParameterSettings.getBatchSize().getIntValue();
        DataSetIterator input;
        if (trainingMode.equals(TrainingMode.SUPERVISED)) {
            input =
                new BufferedDataTableDataSetIterator(bufferedSelectedTable, labelColumnName, batchSize, m_labels, true);
        } else {
            input = new BufferedDataTableDataSetIterator(bufferedSelectedTable, batchSize);
        }

        //build multi layer net
        //List<Layer> layers = DLModelPortObjectUtils.cloneLayers(portObject.getLayers());
        final List<Layer> layers = portObject.getLayers();
        final MultiLayerNetwork oldMln = portObject.getMultilayerLayerNetwork();
        MultiLayerNetFactory mlnFactory;

        //check if list of layers already contains output layer, happens if
        //several learners are used in sequence
        if (checkOutputLayer(layers)) {
            //if so first remove the old output layer
            layers.remove(layers.size() - 1);
        }
        //add the new output layer
        layers.add(createOutputLayer(m_layerParameterSettings));

        MultiLayerNetwork newMln;
        //network creation seems not to be thread safe
        //TODO review this
        synchronized (logger) {
            if (isConvolutional()) {
                final String imageSizeString = m_dataParameterSettings.getImageSize().getStringValue();
                final int[] xyc = ParameterUtils.convertIntsAsStringToInts(imageSizeString);
                //number of channels is set to one because only two dimensional images are currently supported
                mlnFactory = new ConvMultiLayerNetFactory(xyc[0], xyc[1], xyc[2]);
            } else {
                mlnFactory = new MultiLayerNetFactory(input.inputColumns());
            }
            newMln = mlnFactory.createMultiLayerNetwork(layers, m_learnerParameterSettings);
        }

        //attempt to transfer weights between nets
        final boolean usePretrainedUpdater = m_learnerParameterSettings.getUsePretrainedUpdater().getBooleanValue();
        logWarnings(logger, transferFullInitialization(oldMln, newMln, usePretrainedUpdater));

        //set listener that updates the view and the score of this model
        newMln.setListeners(new UpdateLearnerViewIterationListener(this));

        //train the network
        final int epochs = m_dataParameterSettings.getEpochs().getIntValue();
        trainNetwork(newMln, epochs, input, exec);

        final DLModelPortObject newPortObject = new DLModelPortObject(layers, newMln, m_outputSpec);
        return new DLModelPortObject[]{newPortObject};
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final DLModelPortObjectSpec specWithoutLabels = (DLModelPortObjectSpec)configure(inSpecs,
            m_dataParameterSettings.getColumnSelection().getIncludeList(), logger)[0];
        final DataTableSpec tableSpec = (DataTableSpec)inSpecs[1];

        final TrainingMode trainingMode =
            TrainingMode.valueOf(m_learnerParameterSettings.getTrainingsMode().getStringValue());
        final String labelColumnName = m_dataParameterSettings.getLabelColumn().getStringValue();

        m_labels = new ArrayList<String>();

        if (trainingMode.equals(TrainingMode.UNSUPERVISED)) {
            final boolean doBackprop = m_learnerParameterSettings.getUseBackprop().getBooleanValue();
            if (doBackprop) {
                throw new InvalidSettingsException("Backpropagation not available in UNSUPERVISED training mode");
            }
        } else if (trainingMode.equals(TrainingMode.SUPERVISED)) {
            try {
                for (final DataCell cell : tableSpec.getColumnSpec(labelColumnName).getDomain().getValues()) {
                    m_labels.add(ConverterUtils.convertDataCellToJava(cell, String.class));
                }
            } catch (final NullPointerException e) {
                throw new InvalidSettingsException(
                    "Label column not available or not yet selected for SUPERVISED training. "
                        + "Domain of Label column may not be available.");
            } catch (final UnsupportedDataTypeException e) {
                throw new InvalidSettingsException(e);
            }
        }

        logger.info("Constructed network recognized as: "
            + ConfigurationUtils.typesToString(specWithoutLabels.getNeuralNetworkTypes()));

        //if several learners are used in sequence the spec already contains a output layer
        final List<DNNLayerType> newLayerTypes = new ArrayList<DNNLayerType>();
        newLayerTypes.addAll(specWithoutLabels.getLayerTypes());
        if (newLayerTypes.isEmpty() || !newLayerTypes.get(newLayerTypes.size() - 1).equals(DNNLayerType.OUTPUT_LAYER)) {
            newLayerTypes.add(DNNLayerType.OUTPUT_LAYER);
        }

        //create new spec and set labels
        m_outputSpec = new DLModelPortObjectSpec(specWithoutLabels.getNeuralNetworkTypes(), newLayerTypes,
            specWithoutLabels.getLearnedColumns(), m_labels, specWithoutLabels.isTrained());

        return new DLModelPortObjectSpec[]{m_outputSpec};
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        final String momentumAfter = settings.getString(LearnerParameter.MOMENTUM_AFTER.toString().toLowerCase());
        final String imageSize = settings.getString(DataParameter.IMAGE_SIZE.toString().toLowerCase());

        ParameterUtils.validateMomentumAfterParameter(momentumAfter);
        ParameterUtils.validateImageSizeParameter(imageSize, isConvolutional());

        super.validateSettings(settings);
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_dataParameterSettings = new DataParameterSettingsModels();
        m_dataParameterSettings.setParameter(DataParameter.BATCH_SIZE);
        m_dataParameterSettings.setParameter(DataParameter.EPOCHS);
        m_dataParameterSettings.setParameter(DataParameter.COLUMN_SELECTION);
        m_dataParameterSettings.setParameter(DataParameter.LABEL_COLUMN);
        m_dataParameterSettings.setParameter(DataParameter.IMAGE_SIZE);

        m_learnerParameterSettings = new LearnerParameterSettingsModels();
        m_learnerParameterSettings.setParameter(LearnerParameter.TRAINING_MODE);
        m_learnerParameterSettings.setParameter(LearnerParameter.SEED);
        m_learnerParameterSettings.setParameter(LearnerParameter.TRAINING_ITERATIONS);
        m_learnerParameterSettings.setParameter(LearnerParameter.OPTIMIZATION_ALGORITHM);
        m_learnerParameterSettings.setParameter(LearnerParameter.GRADIENT_NORMALIZATION);
        m_learnerParameterSettings.setParameter(LearnerParameter.UPDATER);
        m_learnerParameterSettings.setParameter(LearnerParameter.MOMENTUM_AFTER);
        m_learnerParameterSettings.setParameter(LearnerParameter.GLOBAL_LEARNING_RATE);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_GLOBAL_LEARNING_RATE);
        m_learnerParameterSettings.setParameter(LearnerParameter.L1);
        m_learnerParameterSettings.setParameter(LearnerParameter.L2);
        m_learnerParameterSettings.setParameter(LearnerParameter.GRADIENT_NORMALIZATION_THRESHOLD);
        m_learnerParameterSettings.setParameter(LearnerParameter.MOMENTUM);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_BACKPROP);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_DROP_CONNECT);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_GRADIENT_NORMALIZATION);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_MOMENTUM);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_PRETRAIN);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_REGULARIZATION);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_SEED);
        m_learnerParameterSettings.setParameter(LearnerParameter.GLOBAL_DROP_OUT);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_GLOBAL_DROP_OUT);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_GLOBAL_WEIGHT_INIT);
        m_learnerParameterSettings.setParameter(LearnerParameter.GLOBAL_WEIGHT_INIT);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_PRETRAINED_UPDATER);
        m_learnerParameterSettings.setParameter(LearnerParameter.USE_FINETUNE);

        m_layerParameterSettings = new LayerParameterSettingsModels();
        m_layerParameterSettings.setParameter(LayerParameter.LOSS_FUNCTION);
        m_layerParameterSettings.setParameter(LayerParameter.NUMBER_OF_OUTPUTS);
        /* disable number of outputs dialog option here because supervised learning
         * is default where the number of outputs is specified by the number of distinct
         * labels */
        m_layerParameterSettings.getNumberOfOutputs().setEnabled(false);
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
        final boolean isPretrain = mln.getLayerWiseConfigurations().isPretrain();
        final boolean isBackprop = mln.getLayerWiseConfigurations().isBackprop();
        final boolean isFinetune = m_learnerParameterSettings.getUseFinetune().getBooleanValue();

        exec.setProgress(0.0);

        //calculate progress relative to number of epochs and what to train
        double maxProgress = 0.0;
        if (isBackprop) {
            maxProgress += epochs;
        }
        if (isFinetune) {
            maxProgress += epochs;
        }
        if (isPretrain) {
            maxProgress += epochs;
        }

        if (isPretrain) {
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
        if (isFinetune) {
            logger.info("Finetune Model for " + epochs + " epochs.");
            for (int i = 0; i < epochs; i++) {
                exec.checkCanceled();
                if (getLearningMonitor().checkStopLearning()) {
                    break;
                }
                logger.info("Finetune epoch: " + (i + 1) + " of: " + epochs);

                updateView(i + 1, epochs, "Finetune");
                finetuneOneEpoch(mln, data, exec);

                logEpochScore(mln, (i + 1));
                data.reset();
                exec.setProgress((i + 1) / maxProgress);
            }
        }
        if (isBackprop) {
            logger.info("Do Backpropagation for " + epochs + " epochs.");
            for (int i = 0; i < epochs; i++) {
                exec.checkCanceled();
                if (getLearningMonitor().checkStopLearning()) {
                    break;
                }
                logger.info("Backprop epoch: " + (i + 1) + " of: " + epochs);

                updateView(i + 1, epochs, "Backprop");
                backpropOneEpoch(mln, data, exec);

                logEpochScore(mln, (i + 1));
                data.reset();
                exec.setProgress((i + 1) / maxProgress);
            }
        }
    }

    /**
     * Updates the view using the specified values.
     *
     * @param currentEpoch the current training epoch
     * @param maxEpochs the maximum number of epochs
     * @param trainingMethod a string describing the training method
     */
    private void updateView(final int currentEpoch, final int maxEpochs, final String trainingMethod) {
        final LearningStatus currentStatus = new LearningStatus(currentEpoch, maxEpochs, getScore(), trainingMethod);
        notifyViews(currentStatus);
        setLearningStatus(currentStatus);
    }

    @Override
    protected void reset() {
        //reset the view, if view receives null it resets to default values
        notifyViews(null);
        super.reset();
    }

    /**
     * Logs score of specified model at specified epoch.
     *
     * @param m the model to get score from
     * @param epoch the epoch number to print into log message
     */
    private void logEpochScore(final MultiLayerNetwork m, final int epoch) {
        logger.info("Loss after epoch " + epoch + " is " + m.score());
    }

    /**
     * Creates an new {@link OutputLayer} using the specified layer parameters. If training modes is supervised the
     * number of outputs is automatically set to the number of distinct labels of the input table. If unsupervised the
     * value from the node dialog is taken.
     *
     * @param settings the parameter settings models object holding the layer parameter
     * @return a new output layer using the specified parameters
     * @throws InvalidSettingsException if supervised training mode but labels are not available, meaning null or empty
     */
    private OutputLayer createOutputLayer(final LayerParameterSettingsModels settings) throws InvalidSettingsException {
        final TrainingMode trainingMode =
            TrainingMode.valueOf(m_learnerParameterSettings.getTrainingsMode().getStringValue());
        int nOut;
        if (trainingMode.equals(TrainingMode.SUPERVISED)) {
            if ((m_labels == null) || m_labels.isEmpty()) {
                throw new InvalidSettingsException("Labels not available for SUPERVISED training.");
            }
            nOut = m_labels.size();
        } else {
            nOut = settings.getNumberOfOutputs().getIntValue();
        }
        final WeightInit weight = WeightInit.valueOf(settings.getWeightInit().getStringValue());
        final String activation =
            DL4JActivationFunction.fromToString(settings.getActivation().getStringValue()).getDL4JValue();
        final LossFunction loss =
            DL4JLossFunction.fromToString(settings.getLossFunction().getStringValue()).getDL4JValue();
        final double learningRate = settings.getLearningRate().getDoubleValue();

        final OutputLayer outputLayer = new OutputLayer.Builder(loss).nOut(nOut).activation(activation)
            .weightInit(weight).learningRate(learningRate).build();
        return outputLayer;
    }

    /**
     * Checks if the last layer in the specified list of layers is a {@link OutputLayer}.
     *
     * @param layers the list of layers to check
     * @return true if the last layer is of type {@link OutputLayer}, false if not
     */
    private boolean checkOutputLayer(final List<Layer> layers) {
        if (layers.isEmpty()) {
            return false;
        }
        if (layers.get(layers.size() - 1) instanceof OutputLayer) {
            logger.debug("Last layer is output layer. Should be replaced in Learner Node for uptraining.");
            return true;
        } else {
            for (final Layer l : layers) {
                if (l instanceof OutputLayer) {
                    logger.coding("The Output Layer is not the last layer of the network");
                    return false;
                }
            }
            return false;
        }
    }

}
