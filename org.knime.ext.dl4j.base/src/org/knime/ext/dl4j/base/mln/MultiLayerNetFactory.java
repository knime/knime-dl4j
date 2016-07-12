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
package org.knime.ext.dl4j.base.mln;

import java.util.List;
import java.util.Map;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.IParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JGradientNormalization;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JOptimizationAlgorithm;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;

/**
 * Factory class for creating {@link MultiLayerNetwork}s using {@link List} of
 * {@link Layer}s and {@link LearnerParameterSettingsModels}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class MultiLayerNetFactory {

    private int m_seed = LearnerParameter.DEFAULT_INT;
    private int m_iterations = LearnerParameter.DEFAULT_INT;

    private boolean m_useSeed = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_useGradientNormalization = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_useRegularization = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_useMomentum = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_useDropConnect = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_useGlobalLearningRate = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_useGlobalDropOut = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_useGlobalWeightInit = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_useBackprop = LearnerParameter.DEFAULT_BOOLEAN;
    private boolean m_usePretrain = LearnerParameter.DEFAULT_BOOLEAN;

    private GradientNormalization m_gradientNormalization = DL4JGradientNormalization
            .fromToString(LearnerParameter.DEFAULT_GRADIENTNORM).getDL4JValue();
    private Map<Integer,Double> m_momentumAfter = ParameterUtils.convertStringToMap(LearnerParameter.DEFAULT_MAP);
    private Updater m_updater = Updater.valueOf(LearnerParameter.DEFAULT_UPDATER);
    private OptimizationAlgorithm m_optimization = DL4JOptimizationAlgorithm
            .fromToString(LearnerParameter.DEFAULT_OPTIMIZATION).getDL4JValue();
    private WeightInit m_weightInit = WeightInit.valueOf(LearnerParameter.DEFAULT_WEIGHT_INIT);

    private double m_gradientNormalizationThreshold = LearnerParameter.DEFAULT_DOUBLE;
    private double m_l1 = LearnerParameter.DEFAULT_DOUBLE;
    private double m_l2 = LearnerParameter.DEFAULT_DOUBLE;
    private double m_momentum = LearnerParameter.DEFAULT_DOUBLE;
    private double m_learningRate = LearnerParameter.DEFAULT_DOUBLE;
    private double m_dropOut = LearnerParameter.DEFAULT_DOUBLE;

    /** The number of inputs of the first layer of the network. Used to set up input/output
     *  numbers of layers */
    private final int m_nIn;

    /**
     * Constructor for class MultiLayerNetFactory.
     *
     * @param numInputs the number of inputs which should be used for the first layer of the network
     */
    public MultiLayerNetFactory(final int numInputs) {
        m_nIn = numInputs;
    }

    /**
     * Creates a new {@link MultiLayerNetwork} based on the given list of
     * {@link Layer}s and the given {@link IParameterSettingsModels}. If the
     * second parameter is null the network will be created using the DL4J
     * default parameters.
     *
     * @param layers
     *            the layers of the network
     * @param learnerParameters
     *            the parameters of the learner
     * @return {@link MultiLayerNetwork} consisting of the given layers and
     *         parameters
     */
    public MultiLayerNetwork createMultiLayerNetwork(final List<Layer> layers,
        final LearnerParameterSettingsModels learnerParameters) {
        if (learnerParameters == null) {
            return createMlnWithoutLearnerParameters(layers);
        }
        init(learnerParameters);
        return createMlnWithLearnerParameters(layers);
    }

    /**
     * Creates a new {@link MultiLayerNetwork} based on the given list of
     * {@link Layer}s without further specifying network hyper parameter. This
     * means the default values defined by DL4J will be taken.
     *
     * @param layers
     *            the layers of the network
     * @param learnerParameters
     *            the parameters of the learner
     * @return {@link MultiLayerNetwork} consisting of the given layers and
     *         parameters
     */
    public MultiLayerNetwork createMultiLayerNetwork(final List<Layer> layers) {
        return createMlnWithoutLearnerParameters(layers);
    }

    /**
     * Helper method for creating a new {@link MultiLayerNetwork} using layers
     * and parameters.
     *
     * @param layers
     * @param learnerParameters
     * @return
     */
    protected MultiLayerNetwork createMlnWithLearnerParameters(final List<Layer> layers) {

        final NeuralNetConfiguration.ListBuilder listBuilder = createListBuilderWithLearnerParameters(layers);

        final MultiLayerConfiguration layerConf = listBuilder.build();
        final MultiLayerNetwork mln = new MultiLayerNetwork(layerConf);
        mln.init();

        return mln;
    }

    /**
     * Helper method for creating a new {@link MultiLayerNetwork} using only
     * layers and no parameters, hence using DL4J default parameters.
     *
     * @param layers
     * @param learnerParameters
     * @return
     */
    protected MultiLayerNetwork createMlnWithoutLearnerParameters(final List<Layer> layers) {
        final NeuralNetConfiguration.ListBuilder listBuilder = new NeuralNetConfiguration.Builder().list();
        int currentLayerIndex = 0;
        for (final Layer layer : layers) {
            listBuilder.layer(currentLayerIndex, layer);
            currentLayerIndex++;
        }
        final MultiLayerConfiguration layerConf = listBuilder.build();
        final MultiLayerNetwork mln = new MultiLayerNetwork(layerConf);
        mln.init();
        return mln;
    }

    /**
     * Creates a {@link NeuralNetConfiguration.ListBuilder} using specified list of layers and parameter values from
     * members. Need to make sure that the <code>init()</code> methods was called before calling this method if other
     * values, than the default values, for the network parameters should be used.
     *
     * @param layers the layers which should be used to create the network
     * @return builder with set layers and parameters
     */
    protected NeuralNetConfiguration.ListBuilder createListBuilderWithLearnerParameters(final List<Layer> layers){


        final NeuralNetConfiguration.Builder nnConfigBuilder = new NeuralNetConfiguration.Builder();

        /*
         * Need to overwrite global parameters for each layer separately as setting the parameter
         * in the NeuralNetConfiguration does not overwrite it when it was already set in the
         * specific layer. Hence, need to clone layers to not alter original layers.
         */
        final List<Layer> layersCopy = DLModelPortObjectUtils.cloneLayers(layers);
        if(m_useGlobalDropOut){
            overwriteDropOut(layersCopy, m_dropOut);
        }
        if(m_useGlobalWeightInit){
            overwriteWeightInit(layersCopy, m_weightInit);
        }
        if(m_useGlobalLearningRate){
            overwriteLearningRate(layersCopy, m_learningRate);
        }

        //setup number of input and output neurons
        ConfigurationUtils.setupLayers(layersCopy, m_nIn);

        if(m_useSeed) {
            nnConfigBuilder.seed(m_seed);
        }
        if(m_useGradientNormalization) {
            nnConfigBuilder.gradientNormalization(m_gradientNormalization);
            nnConfigBuilder.gradientNormalizationThreshold(m_gradientNormalizationThreshold);
        }
        if(m_useRegularization) {
            nnConfigBuilder.regularization(true);
            nnConfigBuilder.l1(m_l1);
            nnConfigBuilder.l2(m_l2);
        }
        if(m_useMomentum) {
            nnConfigBuilder.momentum(m_momentum);
            nnConfigBuilder.momentumAfter(m_momentumAfter);
        }
        if(m_useDropConnect) {
            nnConfigBuilder.useDropConnect(true);
        }

        nnConfigBuilder.iterations(m_iterations);
        nnConfigBuilder.updater(m_updater);
        nnConfigBuilder.optimizationAlgo(m_optimization);

        //very strange dl4j behaviour, for unsupervised layers (RBM, Autoencoder) we need
        //to modify the step function elswise learning does not work (error does not decrease)
        //nnConfigBuilder.stepFunction(new DefaultStepFunction());

        final NeuralNetConfiguration.ListBuilder listBuilder = nnConfigBuilder.list();

        int currentLayerIndex = 0;
        for (final Layer layer : layersCopy) {
            listBuilder.layer(currentLayerIndex, layer);
            currentLayerIndex++;
        }
        listBuilder.pretrain(m_usePretrain);
        listBuilder.backprop(m_useBackprop);

        return listBuilder;
    }

    /**
     * Iterates over specified list of layers and overwrites the DropOut
     * parameter with the specified value.
     *
     * @param layers
     * @param newDropOut
     */
    private void overwriteDropOut(final List<Layer> layers, final double newDropOut){
        for(final Layer l : layers){
            l.setDropOut(newDropOut);
        }
    }

    /**
     * Iterates over specified list of layers and overwrites the LearningRate
     * parameter with the specified value.
     *
     * @param layers
     * @param newDropOut
     */
    private void overwriteLearningRate(final List<Layer> layers, final double newLearningRate){
        for(final Layer l : layers){
            l.setLearningRate(newLearningRate);
        }
    }

    /**
     * Iterates over specified list of layers and overwrites the WeightInit
     * parameter with the specified value.
     *
     * @param layers
     * @param newDropOut
     */
    private void overwriteWeightInit(final List<Layer> layers, final WeightInit newWeightInit){
        for(final Layer l : layers){
            l.setWeightInit(newWeightInit);
        }
    }

    /**
     * Initialise members with values from {@link LearnerParameterSettingsModels}.
     * {@link SettingsModel}s for some parameters may not be present, hence
     * all models are checked for null. If null a default value is used.
     *
     * @param learnerParameters the parameters to get the values from
     */
    private void init(final LearnerParameterSettingsModels learnerParameters){
        SettingsModelIntegerBounded intSettings = learnerParameters.getSeed();
        if(intSettings != null) {
            m_seed = intSettings.getIntValue();
        }

        intSettings = learnerParameters.getTrainingIterations();
        if(intSettings != null) {
            m_iterations = intSettings.getIntValue();
        }

        SettingsModelBoolean booleanSettings = learnerParameters.getUseSeed();
        if(booleanSettings != null) {
            m_useSeed = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseGradientNormalization();
        if(booleanSettings != null) {
            m_useGradientNormalization = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseRegularization();
        if(booleanSettings != null) {
            m_useRegularization = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseMomentum();
        if(booleanSettings != null) {
            m_useMomentum = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseDropConnect();
        if(booleanSettings != null) {
            m_useDropConnect = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseGlobalLearningRate();
        if(booleanSettings != null) {
            m_useGlobalLearningRate = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseGlobalDropOut();
        if(booleanSettings != null) {
            m_useGlobalDropOut = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseGlobalWeightInit();
        if(booleanSettings != null) {
            m_useGlobalWeightInit = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseBackprop();
        if(booleanSettings != null) {
            m_useBackprop = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUsePretrain();
        if(booleanSettings != null) {
            m_usePretrain = booleanSettings.getBooleanValue();
        }

        SettingsModelString stringSettings = learnerParameters.getGradientNormalization();
        if(stringSettings != null) {
            m_gradientNormalization =
                    DL4JGradientNormalization.fromToString(stringSettings.getStringValue()).getDL4JValue();
        }

        stringSettings = learnerParameters.getMomentumAfter();
        if(stringSettings != null) {
            m_momentumAfter = ParameterUtils.convertStringToMap(stringSettings.getStringValue());
        }

        stringSettings = learnerParameters.getUpdater();
        if(stringSettings != null) {
            m_updater = Updater.valueOf(stringSettings.getStringValue());
        }

        stringSettings = learnerParameters.getOptimizationAlgorithm();
        if(stringSettings != null) {
            m_optimization =
                    DL4JOptimizationAlgorithm.fromToString(stringSettings.getStringValue()).getDL4JValue();
        }

        stringSettings = learnerParameters.getGobalWeightInit();
        if(stringSettings != null) {
            m_weightInit = WeightInit.valueOf(stringSettings.getStringValue());
        }

        SettingsModelDoubleBounded doubleSettings = learnerParameters.getGradientNormalizationThreshold();
        if(doubleSettings != null) {
            m_gradientNormalizationThreshold = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getL1();
        if(doubleSettings != null) {
            m_l1 = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getL2();
        if(doubleSettings != null) {
            m_l2 = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getMomentum();
        if(doubleSettings != null) {
            m_momentum = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getGlobalLearningRate();
        if(doubleSettings != null) {
            m_learningRate = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getGlobalDropOut();
        if(doubleSettings != null) {
            m_dropOut = doubleSettings.getDoubleValue();
        }
    }
}
