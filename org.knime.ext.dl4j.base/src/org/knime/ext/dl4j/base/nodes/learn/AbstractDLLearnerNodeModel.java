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
package org.knime.ext.dl4j.base.nodes.learn;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.nn.api.Updater;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

/**
 * Abstract superclass for learner node models of Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public abstract class AbstractDLLearnerNodeModel extends AbstractDLNodeModel {

    private boolean isConvolutional;

    private boolean isRecurrent;

    private boolean inputTableContainsImg;

    protected DLModelPortObjectSpec m_outputSpec;

    /** the current score of this learner */
    private Double m_score = null;

    /** the current learning status */
    private LearningStatus m_learningStatus = null;

    /** a learning monitor for this learner storing if learning should be stopped */
    private final LearningMonitor m_learningMonitor = new LearningMonitor();

    protected AbstractDLLearnerNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
    }

    /**
     * Makes basic checks before a learner can be executed. Checks column selection, sets list of columns to be learned
     * on and sets learner flags.
     *
     * @param inSpecs the specs of the model to learn (index 0) and the specs of the table to learn on (index 1)
     * @param selectedColumns name of columns to use for learning
     * @param logger a logger to log errors
     * @return checked port object spec with set list of feature columns and empty list of labels
     * @throws InvalidSettingsException
     */
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs, final List<String> selectedColumns,
        final NodeLogger logger) throws InvalidSettingsException {
        final DLModelPortObjectSpec modelSpec = (DLModelPortObjectSpec)inSpecs[0];
        final DataTableSpec tableSpec = (DataTableSpec)inSpecs[1];

        final List<DNNLayerType> dnnLayerTypes = modelSpec.getLayerTypes();
        if (dnnLayerTypes.contains(DNNLayerType.CONVOLUTION_LAYER)) {
            isConvolutional = true;
        } else {
            isConvolutional = false;
        }

        if (dnnLayerTypes.contains(DNNLayerType.GRAVES_LSTM) || dnnLayerTypes.contains(DNNLayerType.GRU)) {
            isRecurrent = true;
        } else {
            isRecurrent = false;
        }

        inputTableContainsImg = ConfigurationUtils.containsImg(tableSpec);

        //validate column selection
        ConfigurationUtils.validateColumnSelection(tableSpec,
            selectedColumns.toArray(new String[selectedColumns.size()]));

        //check for spec sanity
        logWarnings(logger, ConfigurationUtils.validateSpec(modelSpec, modelSpec.getNeuralNetworkTypes()));

        //create list of input types of selected columns for column validation in predictor
        final List<Pair<String, String>> inputs =
            ConfigurationUtils.createNameTypeListOfSelectedCols(selectedColumns, tableSpec);

        m_outputSpec = new DLModelPortObjectSpec(modelSpec.getNeuralNetworkTypes(), modelSpec.getLayerTypes(), inputs,
            new ArrayList<>(), true);
        return new PortObjectSpec[]{m_outputSpec};
    }

    /**
     * Attempts to transfer the {@link Updater} from one {@link MultiLayerNetwork} to another. This is important if you
     * want to further train a pretrained net as some {@link Updater}s contain a history of gradients from training.
     *
     * @param from the network to get the updater from
     * @param to the network to transfer the updater to
     * @return a list of messages if the transfer was successful and if not a reason of the problem
     */
    protected List<String> transferUpdater(final MultiLayerNetwork from, final MultiLayerNetwork to) {
        final List<String> messages = new ArrayList<>();
        final org.deeplearning4j.nn.api.Updater updater = from.getUpdater();

        if (updater == null) {
            messages.add("Could not transfer updater between nets as there is no updater set in the source net");
        } else {
            to.setUpdater(updater);
            messages.add("Successfully transfered updater between nets.");
        }

        return messages;
    }

    /**
     * Attempts to transfer weights from one {@link MultiLayerNetwork} to another. The weights will be transfered layer
     * by layer. Weights will only be transfered between intersecting layers of both networks. Assumes that the order
     * and type of the layers to transfer the weights is the same.
     *
     * @param from the network to get the weights from
     * @param to the network to transfer the weights to
     * @return a list of messages if the transfer was successful and if not a reason of the problem
     */
    protected List<String> transferWeights(final MultiLayerNetwork from, final MultiLayerNetwork to) {
        final List<String> messages = new ArrayList<>();
        final List<INDArray> oldWeights = new ArrayList<>();

        for (final org.deeplearning4j.nn.api.Layer layer : from.getLayers()) {
            oldWeights.add(layer.params());
        }

        int i = 0;
        for (final org.deeplearning4j.nn.api.Layer layer : to.getLayers()) {
            if (i < oldWeights.size()) {
                try {
                    layer.setParams(oldWeights.get(i));
                    messages.add("Successfully transfered weights from layer: " + (i + 1) + " ("
                        + from.getLayers()[i].getClass().getName() + ") of old network to " + "layer: " + (i + 1) + " ("
                        + layer.getClass().getName() + ") of new network");
                } catch (final Exception e) {
                    messages.add("Could not transfer weights from layer: " + (i + 1) + " ("
                        + from.getLayers()[i].getClass().getName() + ") of old network to " + "layer: " + (i + 1) + " ("
                        + layer.getClass().getName() + ") of new network");
                    messages.add("Reason: " + e.getMessage());
                }
                i++;
            } else {
                break;
            }
        }

        return messages;
    }

    /**
     * Attempt to transfer initialisation of one {@link MultiLayerNetwork} to another meaning transferring the weights
     * and the {@link Updater}. Convenience method which calls
     * {@link AbstractDLLearnerNodeModel#transferWeights(MultiLayerNetwork, MultiLayerNetwork)} and
     * {@link AbstractDLLearnerNodeModel#transferUpdater(MultiLayerNetwork, MultiLayerNetwork)}.
     *
     * @param from the network to get initialisation from
     * @param to the network to transfer initialisation to
     * @param transferUpdater whether to transfer {@link Updater} or not
     * @return a list of messages if the transfer was successful and if not a reason of the problem
     */
    protected List<String> transferFullInitialization(final MultiLayerNetwork from, final MultiLayerNetwork to,
        final boolean transferUpdater) {
        final List<String> messages = new ArrayList<>();
        if (from != null) {
            messages.addAll(transferWeights(from, to));
            if (transferUpdater) {
                messages.addAll(transferUpdater(from, to));
            }
        }
        return messages;
    }

    /**
     * Performs one epoch of pretraining of the specified {@link MultiLayerNetwork}. Checks {@link LearningMonitor} of
     * this learner if learning should be prematurely stopped.
     *
     * @param mln the network to train
     * @param data the data to train on
     * @param exec used to check for cancelled execution and stop learning
     * @throws CanceledExecutionException
     */
    protected void pretrainOneEpoch(final MultiLayerNetwork mln, final DataSetIterator data,
        final ExecutionContext exec) throws CanceledExecutionException {
        exec.setMessage("Performing Pretraining");
        while (data.hasNext()) {
            exec.checkCanceled();
            if (m_learningMonitor.checkStopLearning()) {
                break;
            }

            mln.pretrain(data.next().getFeatureMatrix());
        }
    }

    /**
     * Performs one epoch of finetuning of the specified {@link MultiLayerNetwork}. Checks {@link LearningMonitor} of
     * this learner if learning should be prematurely stopped.
     *
     * @param mln the network to train
     * @param data the data to train on
     * @param exec used to check for cancelled execution and stop learning
     * @throws CanceledExecutionException
     */
    protected void finetuneOneEpoch(final MultiLayerNetwork mln, final DataSetIterator data,
        final ExecutionContext exec) throws CanceledExecutionException {
        exec.setMessage("Performing Finetuning");
        while (data.hasNext()) {
            exec.checkCanceled();
            if (m_learningMonitor.checkStopLearning()) {
                break;
            }

            final DataSet next = data.next();
            if ((next.getFeatureMatrix() == null) || (next.getLabels() == null)) {
                break;
            }
            mln.setInput(next.getFeatureMatrix());
            mln.setLabels(next.getLabels());
            mln.finetune();
        }
    }

    /**
     * Performs one epoch of backpropagation with gradient descent of the specified {@link MultiLayerNetwork}. Checks
     * {@link LearningMonitor} of this learner if learning should be prematurely stopped.
     *
     * @param mln the network to train
     * @param data the data to train on
     * @param exec used to check for cancelled execution and stop learning
     * @throws CanceledExecutionException
     */
    protected void backpropOneEpoch(final MultiLayerNetwork mln, final DataSetIterator data,
        final ExecutionContext exec) throws CanceledExecutionException {
        exec.setMessage("Performing Backpropagation");
        final boolean isPretrain = mln.getLayerWiseConfigurations().isPretrain();
        /**
         * Need to set pretrain to false here because mln.fit() on a DataSetIterator performs pretraining and
         * finetuneing if pretrain is set to true. Workaround because can't extract backprop procedure from
         * MultiLayerNetwork.fit(DataSetIterator) implementation as it depends on protected methods.
         */
        if (isPretrain) {
            mln.getLayerWiseConfigurations().setPretrain(false);
        }
        while (data.hasNext()) {
            exec.checkCanceled();
            if (m_learningMonitor.checkStopLearning()) {
                break;
            }

            mln.fit(data.next());
        }
        mln.getLayerWiseConfigurations().setPretrain(isPretrain);
    }

    /**
     * Call notifyViews on this {@link NodeModel} with the specified Object.
     *
     * @param obj the object to pass
     */
    public void passObjToView(final Object obj) {
        notifyViews(obj);
    }

    /**
     * Sets score and learning status to null and reset {@link LearningMonitor} of this Learner
     */
    @Override
    protected void reset() {
        m_score = null;
        m_learningStatus = null;
        m_learningMonitor.reset();
        super.reset();
    }

    public boolean isConvolutional() {
        return isConvolutional;
    }

    public boolean inputTableContainsImg() {
        return inputTableContainsImg;
    }

    public boolean isRecurrent() {
        return isRecurrent;
    }

    public Double getScore() {
        return m_score;
    }

    public void setScore(final Double score) {
        m_score = score;
    }

    public void setLearningStatus(final LearningStatus status) {
        m_learningStatus = status;
    }

    public LearningStatus getLearningStatus() {
        return m_learningStatus;
    }

    public LearningMonitor getLearningMonitor() {
        return m_learningMonitor;
    }
}
