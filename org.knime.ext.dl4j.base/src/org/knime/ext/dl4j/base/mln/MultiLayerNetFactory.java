/*******************************************************************************
 * Copyright by KNIME AG, Zurich, Switzerland
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
package org.knime.ext.dl4j.base.mln;

import java.util.List;
import java.util.Map;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.BinomialDistribution;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.BaseLayer;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.IParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JDistribution;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JGradientNormalization;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JOptimizationAlgorithm;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.knime.ext.dl4j.base.util.DL4JVersionUtils;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;

/**
 * Factory class for creating {@link MultiLayerNetwork}s using {@link List} of {@link Layer}s and
 * {@link LearnerParameterSettingsModels}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
@Deprecated
public class MultiLayerNetFactory {

    private int m_seed = LearnerParameter.DEFAULT_INT;

    private int m_iterations = LearnerParameter.DEFAULT_INT;

    private int m_maxNumLineSearchIterations = LearnerParameter.DEFAULT_INT;

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

    private boolean m_useUpdater = LearnerParameter.DEFAULT_USE_UPDATER;

    private GradientNormalization m_gradientNormalization =
        DL4JGradientNormalization.fromToString(LearnerParameter.DEFAULT_GRADIENT_NORMALIZATION).getDL4JValue();

    private Map<Integer, Double> m_momentumAfter = ParameterUtils.convertStringToMap(LearnerParameter.DEFAULT_MAP);

    private Map<Integer, Double> m_learningRateAfter = ParameterUtils.convertStringToMap(LearnerParameter.DEFAULT_MAP);

    private Updater m_updater = Updater.valueOf(LearnerParameter.DEFAULT_UPDATER);

    private OptimizationAlgorithm m_optimization =
        DL4JOptimizationAlgorithm.fromToString(LearnerParameter.DEFAULT_OPTIMIZATION).getDL4JValue();

    private WeightInit m_weightInit = WeightInit.valueOf(LearnerParameter.DEFAULT_WEIGHT_INIT);

    private double m_gradientNormalizationThreshold = LearnerParameter.DEFAULT_GRADIENT_NORMALIZATION_THRESHOLD;

    private double m_l1 = LearnerParameter.DEFAULT_DOUBLE;

    private double m_l2 = LearnerParameter.DEFAULT_DOUBLE;

    private double m_momentum = LearnerParameter.DEFAULT_MOMENTUM;

    private double m_learningRate = LearnerParameter.DEFAULT_DOUBLE;

    private double m_dropOut = LearnerParameter.DEFAULT_DOUBLE;

    private double m_rmsDecay = LearnerParameter.DEFAULT_RMS_DECAY;

    private double m_adamMeanDecay = LearnerParameter.DEFAULT_ADAM_MEAN_DECAY;

    private double m_adamVarDecay = LearnerParameter.DEFAULT_ADAM_VAR_DECAY;

    private double m_adadeltaRho = LearnerParameter.DEFAULT_ADADELTA_RHO;

    private DL4JDistribution m_distribution = DL4JDistribution.valueOf(LearnerParameter.DEFAULT_DISTRIBUTION);

    private LearningRatePolicy m_lrPolicy = LearningRatePolicy.valueOf(LearnerParameter.DEFAULT_LEARNING_RATE_POLICY);

    private Integer m_distributionBinomialTrails = LearnerParameter.DEFAULT_INT;

    private double m_distributionBinomialProbability = LearnerParameter.DEFAULT_DOUBLE;

    private double m_distributionMean = LearnerParameter.DEFAULT_DOUBLE;

    private double m_distributionSTD = LearnerParameter.DEFAULT_DOUBLE;

    private double m_distributionUpperBound = LearnerParameter.DEFAULT_DOUBLE;

    private double m_distributionLowerBound = LearnerParameter.DEFAULT_DOUBLE;

    private double m_lrPolicyDecayRate = LearnerParameter.DEFAULT_DOUBLE;

    private double m_lrPolicyPower = LearnerParameter.DEFAULT_DOUBLE;

    private double m_lrPolicySteps = LearnerParameter.DEFAULT_DOUBLE;

    private double m_lrPolicyScoreDecayRate = LearnerParameter.DEFAULT_DOUBLE;

    private double m_biasLearningRate = LearnerParameter.DEFAULT_DOUBLE;

    private double m_biasInit = LearnerParameter.DEFAULT_DOUBLE;

    private boolean m_useAdvancedLearningRate = LearnerParameter.DEFAULT_BOOLEAN;

    private boolean m_useBiasLearningRate = LearnerParameter.DEFAULT_BOOLEAN;

    private boolean m_useBiasInit = LearnerParameter.DEFAULT_BOOLEAN;

    /**
     * The number of inputs of the first layer of the network. Used to set up input/output numbers of layers
     */
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
     * Creates a new {@link MultiLayerNetwork} based on the given list of {@link Layer}s and the given
     * {@link IParameterSettingsModels}. If the second parameter is null the network will be created using the DL4J
     * default parameters.
     *
     * @param layers the layers of the network
     * @param learnerParameters the parameters of the learner
     * @return {@link MultiLayerNetwork} consisting of the given layers and parameters
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
     * Creates a new {@link MultiLayerNetwork} based on the given list of {@link Layer}s without further specifying
     * network hyper parameter. This means the default values defined by DL4J will be taken.
     *
     * @param layers the layers of the network
     * @return {@link MultiLayerNetwork} consisting of the given layers and parameters
     */
    public MultiLayerNetwork createMultiLayerNetwork(final List<Layer> layers) {
        return createMlnWithoutLearnerParameters(layers);
    }

    /**
     * Helper method for creating a new {@link MultiLayerNetwork} using layers and parameters.
     *
     * @param layers
     * @return a network containing specified layers and learner parameter
     */
    protected MultiLayerNetwork createMlnWithLearnerParameters(final List<Layer> layers) {

        final NeuralNetConfiguration.ListBuilder listBuilder = createListBuilderWithLearnerParameters(layers);

        final MultiLayerConfiguration layerConf = listBuilder.build();
        final MultiLayerNetwork mln = new MultiLayerNetwork(layerConf);
        mln.init();

        return mln;
    }

    /**
     * Helper method for creating a new {@link MultiLayerNetwork} using only layers and no parameters, hence using DL4J
     * default parameters.
     *
     * @param layers
     * @return a network containing specified layers and default learner parameter
     */
    protected MultiLayerNetwork createMlnWithoutLearnerParameters(final List<Layer> layers) {
        final org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder listBuilder =
            new NeuralNetConfiguration.Builder().list();
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
     * Creates a {@link org.deeplearning4j.nn.conf.NeuralNetConfiguration.ListBuilder} using specified list of layers
     * and parameter values from members. Need to make sure that the <code>init()</code> methods was called before
     * calling this method if other values, than the default values, for the network parameters should be used.
     *
     * @param layers the layers which should be used to create the network
     * @return builder with set layers and parameters
     */
    protected NeuralNetConfiguration.ListBuilder createListBuilderWithLearnerParameters(final List<Layer> layers) {

        final NeuralNetConfiguration.Builder nnConfigBuilder = new NeuralNetConfiguration.Builder();

        /*
         * Need to overwrite global parameters for each layer separately as setting the parameter
         * in the NeuralNetConfiguration does not overwrite it when it was already set in the
         * specific layer. Hence, need to clone layers to not alter original layers.
         */
        final List<Layer> layersCopy = DLModelPortObjectUtils.cloneLayers(layers);
        if (m_useGlobalDropOut) {
            overwriteDropOut(layersCopy, m_dropOut);
            nnConfigBuilder.dropOut(m_dropOut);
        }
        if (m_useGlobalWeightInit) {
            overwriteWeightInit(layersCopy, m_weightInit);
            nnConfigBuilder.weightInit(m_weightInit);

            if (m_weightInit.equals(ParameterUtils.DISTRIBUTION_PARAMETER_CONDITION)) {
                Distribution dist;
                switch (m_distribution) {
                    case BINOMIAL:
                        dist =
                            new BinomialDistribution(m_distributionBinomialTrails, m_distributionBinomialProbability);
                        break;
                    case NORMAL:
                        dist = new NormalDistribution(m_distributionMean, m_distributionSTD);
                        break;
                    case UNIFORM:
                        dist = new UniformDistribution(m_distributionLowerBound, m_distributionUpperBound);
                        break;
                    default:
                        throw new IllegalArgumentException("No case defined for DL4JDistribution: " + m_distribution);
                }
                nnConfigBuilder.dist(dist);
            }
        }
        if (m_useGlobalLearningRate) {
            overwriteLearningRate(layersCopy, m_learningRate);
        }

        //setup number of input and output neurons
        ConfigurationUtils.setupLayers(layersCopy, m_nIn);

        if (m_useSeed) {
            nnConfigBuilder.seed(m_seed);
        }
        if (m_useGradientNormalization) {
            nnConfigBuilder.gradientNormalization(m_gradientNormalization);
            nnConfigBuilder.gradientNormalizationThreshold(m_gradientNormalizationThreshold);
        }
        if (m_useRegularization) {
            nnConfigBuilder.regularization(true);
            nnConfigBuilder.l1(m_l1);
            nnConfigBuilder.l2(m_l2);
        }

        // momentum moved to updaters, will only be used with NESTEROVS
        // if (m_useMomentum) {
        // nnConfigBuilder.momentum(m_momentum);
        // nnConfigBuilder.momentumAfter(m_momentumAfter);
        // }

        if (m_useDropConnect) {
            nnConfigBuilder.useDropConnect(true);
        }

        // the learning rate policy behaves strange, need to revise conditions if we want to add it
        //        if (m_useAdvancedLearningRate) {
        //            nnConfigBuilder.learningRateDecayPolicy(m_lrPolicy);
        //            nnConfigBuilder.lrPolicyDecayRate(m_lrPolicyDecayRate);
        //            if (m_lrPolicy.equals(ParameterUtils.LR_POWER_PARAMETER_CONDITION)) {
        //                nnConfigBuilder.lrPolicyPower(m_lrPolicyPower);
        //            } else if (m_lrPolicy.equals(ParameterUtils.LR_SCHEDULE_PARAMETER_CONDITION)) {
        //                nnConfigBuilder.learningRateSchedule(m_learningRateAfter);
        //            } else if (m_lrPolicy.equals(ParameterUtils.LR_SCORE_BASED_PARAMETER_CONDITION)) {
        //                nnConfigBuilder.learningRateScoreBasedDecayRate(m_lrPolicyScoreDecayRate);
        //            } else if (m_lrPolicy.equals(ParameterUtils.LR_STEPS_PARAMETER_CONDITION)) {
        //                nnConfigBuilder.lrPolicySteps(m_lrPolicySteps);
        //            } else if (m_lrPolicy.equals(ParameterUtils.LR_EXPONENTIAL_PARAMETER_CONDITION)) {
        //                //no extra param
        //            }
        //        }

        if (m_useBiasInit) {
            nnConfigBuilder.biasInit(m_biasInit);
        }
        if (m_useBiasLearningRate) {
            overwriteBiasLearningRate(layersCopy, m_biasLearningRate);
        }

        nnConfigBuilder.iterations(m_iterations);
        nnConfigBuilder.updater(m_updater);
        if (m_updater.equals(ParameterUtils.ADAM_PARAMETER_CONDITION)) {
            nnConfigBuilder.adamMeanDecay(m_adamMeanDecay);
            nnConfigBuilder.adamVarDecay(m_adamVarDecay);
        } else if (m_updater.equals(ParameterUtils.ADADELTA_PARAMETER_CONDITION)) {
            nnConfigBuilder.rho(m_adadeltaRho);
        } else if (m_updater.equals(ParameterUtils.RMSPROP_PARAMETER_CONDITION)) {
            nnConfigBuilder.rmsDecay(m_rmsDecay);
        } else if (m_updater.equals(ParameterUtils.NESTEROVS_PARAMETER_CONDITION)) {
            nnConfigBuilder.momentum(m_momentum);
            nnConfigBuilder.momentumAfter(m_momentumAfter);
        }
        //The new dialogs have a use updater checkbox which is not present in the old dialogs and is true by default. Therefore,
        //we overwrite the updater with the default values, if the checkbox is not checked. For the old dialog this will not happen
        //as the value is true by default.
        if (!m_useUpdater) {
            nnConfigBuilder.updater(Updater.valueOf(LearnerParameter.DEFAULT_UPDATER));
            nnConfigBuilder.momentum(LearnerParameter.DEFAULT_MOMENTUM);
        }

        nnConfigBuilder.optimizationAlgo(m_optimization);
        if (ParameterUtils.MAX_LINE_SEARCH_ITERATIONS_CONDITION.contains(m_optimization)) {
            nnConfigBuilder.maxNumLineSearchIterations(m_maxNumLineSearchIterations);
        }

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
     * Iterates over specified list of layers and overwrites the BiasLearningRate parameter with the specified value.
     *
     * @param layers
     * @param newBiasLearningRate
     */
    private void overwriteBiasLearningRate(final List<Layer> layers, final double newBiasLearningRate) {
        for (final BaseLayer l : DL4JVersionUtils.filterBaseLayers(layers)) {
            l.setBiasLearningRate(newBiasLearningRate);
        }
    }

    /**
     * Iterates over specified list of layers and overwrites the DropOut parameter with the specified value.
     *
     * @param layers
     * @param newDropOut
     */
    private void overwriteDropOut(final List<Layer> layers, final double newDropOut) {
        for (final Layer l : layers) {
            l.setDropOut(newDropOut);
        }
    }

    /**
     * Iterates over specified list of layers and overwrites the LearningRate parameter with the specified value.
     *
     * @param layers
     * @param newDropOut
     */
    private void overwriteLearningRate(final List<Layer> layers, final double newLearningRate) {
        for (final BaseLayer l : DL4JVersionUtils.filterBaseLayers(layers)) {
            l.setLearningRate(newLearningRate);
        }
    }

    /**
     * Iterates over specified list of layers and overwrites the WeightInit parameter with the specified value.
     *
     * @param layers
     * @param newDropOut
     */
    private void overwriteWeightInit(final List<Layer> layers, final WeightInit newWeightInit) {
        for (final BaseLayer l : DL4JVersionUtils.filterBaseLayers(layers)) {
            l.setWeightInit(newWeightInit);
        }
    }

    /**
     * Initialise members with values from {@link LearnerParameterSettingsModels}. {@link SettingsModel}s for some
     * parameters may not be present, hence all models are checked for null. If null a default value is used.
     *
     * @param learnerParameters the parameters to get the values from
     */
    private void init(final LearnerParameterSettingsModels learnerParameters) {
        SettingsModelIntegerBounded intSettings = learnerParameters.getSeed();
        if (intSettings != null) {
            m_seed = intSettings.getIntValue();
        }

        intSettings = learnerParameters.getTrainingIterations();
        if (intSettings != null) {
            m_iterations = intSettings.getIntValue();
        }

        intSettings = learnerParameters.getDistributionBinomialTrails();
        if (intSettings != null) {
            m_distributionBinomialTrails = intSettings.getIntValue();
        }

        intSettings = learnerParameters.getMaxNumLineSearchIterations();
        if (intSettings != null) {
            m_maxNumLineSearchIterations = intSettings.getIntValue();
        }

        SettingsModelBoolean booleanSettings = learnerParameters.getUseSeed();
        if (booleanSettings != null) {
            m_useSeed = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseGradientNormalization();
        if (booleanSettings != null) {
            m_useGradientNormalization = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseRegularization();
        if (booleanSettings != null) {
            m_useRegularization = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseMomentum();
        if (booleanSettings != null) {
            m_useMomentum = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseDropConnect();
        if (booleanSettings != null) {
            m_useDropConnect = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseGlobalLearningRate();
        if (booleanSettings != null) {
            m_useGlobalLearningRate = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseGlobalDropOut();
        if (booleanSettings != null) {
            m_useGlobalDropOut = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseGlobalWeightInit();
        if (booleanSettings != null) {
            m_useGlobalWeightInit = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseBackprop();
        if (booleanSettings != null) {
            m_useBackprop = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseUpdater();
        if (booleanSettings != null) {
            m_useUpdater = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUsePretrain();
        if (booleanSettings != null) {
            m_usePretrain = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseBiasInit();
        if (booleanSettings != null) {
            m_useBiasInit = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseBiasLearningRate();
        if (booleanSettings != null) {
            m_useBiasLearningRate = booleanSettings.getBooleanValue();
        }

        booleanSettings = learnerParameters.getUseAdvancedLearningRate();
        if (booleanSettings != null) {
            m_useAdvancedLearningRate = booleanSettings.getBooleanValue();
        }

        SettingsModelString stringSettings = learnerParameters.getGradientNormalization();
        if (stringSettings != null) {
            m_gradientNormalization =
                DL4JGradientNormalization.fromToString(stringSettings.getStringValue()).getDL4JValue();
        }

        stringSettings = learnerParameters.getMomentumAfter();
        if (stringSettings != null) {
            m_momentumAfter = ParameterUtils.convertStringToMap(stringSettings.getStringValue());
        }

        stringSettings = learnerParameters.getDistribution();
        if (stringSettings != null) {
            m_distribution = DL4JDistribution.valueOf(stringSettings.getStringValue());
        }

        stringSettings = learnerParameters.getLrPolicy();
        if (stringSettings != null) {
            m_lrPolicy = LearningRatePolicy.valueOf(stringSettings.getStringValue());
        }

        stringSettings = learnerParameters.getLearningRateAfter();
        if (stringSettings != null) {
            m_learningRateAfter = ParameterUtils.convertStringToMap(stringSettings.getStringValue());
        }

        stringSettings = learnerParameters.getUpdater();
        if (stringSettings != null) {
            m_updater = Updater.valueOf(stringSettings.getStringValue());
        }

        stringSettings = learnerParameters.getOptimizationAlgorithm();
        if (stringSettings != null) {
            m_optimization = DL4JOptimizationAlgorithm.fromToString(stringSettings.getStringValue()).getDL4JValue();
        }

        stringSettings = learnerParameters.getGobalWeightInit();
        if (stringSettings != null) {
            m_weightInit = WeightInit.valueOf(stringSettings.getStringValue());
        }

        SettingsModelDouble doubleSettings = learnerParameters.getGradientNormalizationThreshold();
        if (doubleSettings != null) {
            m_gradientNormalizationThreshold = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getL1();
        if (doubleSettings != null) {
            m_l1 = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getL2();
        if (doubleSettings != null) {
            m_l2 = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getMomentum();
        if (doubleSettings != null) {
            m_momentum = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getGlobalLearningRate();
        if (doubleSettings != null) {
            m_learningRate = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getGlobalDropOut();
        if (doubleSettings != null) {
            m_dropOut = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getAdadeltaRho();
        if (doubleSettings != null) {
            m_adadeltaRho = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getAdamMeanDecay();
        if (doubleSettings != null) {
            m_adamMeanDecay = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getAdamVarDecay();
        if (doubleSettings != null) {
            m_adamVarDecay = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getRmsDecay();
        if (doubleSettings != null) {
            m_rmsDecay = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getDistributionBinomialProbability();
        if (doubleSettings != null) {
            m_distributionBinomialProbability = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getDistributionMean();
        if (doubleSettings != null) {
            m_distributionMean = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getDistributionSTD();
        if (doubleSettings != null) {
            m_distributionSTD = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getDistributionUpperBound();
        if (doubleSettings != null) {
            m_distributionUpperBound = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getDistributionLowerBound();
        if (doubleSettings != null) {
            m_distributionLowerBound = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getLrPolicyDecayRate();
        if (doubleSettings != null) {
            m_lrPolicyDecayRate = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getLrPolicyPower();
        if (doubleSettings != null) {
            m_lrPolicyPower = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getLrPolicyScoreDecayRate();
        if (doubleSettings != null) {
            m_lrPolicyScoreDecayRate = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getLrPolicySteps();
        if (doubleSettings != null) {
            m_lrPolicySteps = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getBiasInit();
        if (doubleSettings != null) {
            m_biasInit = doubleSettings.getDoubleValue();
        }

        doubleSettings = learnerParameters.getBiasLearningRate();
        if (doubleSettings != null) {
            m_biasLearningRate = doubleSettings.getDoubleValue();
        }
    }
}
