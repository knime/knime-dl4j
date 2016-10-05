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
import org.deeplearning4j.nn.conf.distribution.BinomialDistribution;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.knime.ext.dl4j.base.settings.IParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JDistribution;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JGradientNormalization;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JOptimizationAlgorithm;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;
import org.knime.ext.dl4j.base.util.ParameterUtils;

/**
 * Factory class for creating {@link MultiLayerNetwork}s using {@link List} of {@link Layer}s and
 * {@link LearnerParameterSettingsModels2}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class MultiLayerNetFactory2 {

    /**
     * The number of inputs of the first layer of the network. Used to set up input/output numbers of layers
     */
    private final int m_nIn;

    /**
     * Constructor for class MultiLayerNetFactory.
     *
     * @param numInputs the number of inputs which should be used for the first layer of the network
     */
    public MultiLayerNetFactory2(final int numInputs) {
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
        final LearnerParameterSettingsModels2 learnerParameters) {
        if (learnerParameters == null) {
            return createMlnWithoutLearnerParameters(layers);
        }
        return createMlnWithLearnerParameters(layers, learnerParameters);
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
     * @param learnerParameters
     * @return a network containing specified layers and learner parameter
     */
    protected MultiLayerNetwork createMlnWithLearnerParameters(final List<Layer> layers,
        final LearnerParameterSettingsModels2 learnerParameters) {

        final NeuralNetConfiguration.ListBuilder listBuilder =
            createListBuilderWithLearnerParameters(layers, learnerParameters);

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
     * and parameter.
     *
     * @param layers the layers which should be used to create the network
     * @param lp
     * @return builder with set layers and parameters
     */
    protected NeuralNetConfiguration.ListBuilder createListBuilderWithLearnerParameters(final List<Layer> layers,
        final LearnerParameterSettingsModels2 lp) {

        //boolean
        boolean m_useGlobalDropOut =
            lp.getBoolean(LearnerParameter.USE_GLOBAL_DROP_OUT, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useGlobalWeightInit =
            lp.getBoolean(LearnerParameter.USE_GLOBAL_WEIGHT_INIT, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useGlobalLearningRate =
            lp.getBoolean(LearnerParameter.USE_GLOBAL_LEARNING_RATE, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useDropConnect = lp.getBoolean(LearnerParameter.USE_DROP_CONNECT, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useBackprop = lp.getBoolean(LearnerParameter.USE_BACKPROP, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useSeed = lp.getBoolean(LearnerParameter.USE_SEED, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useGradientNormalization =
            lp.getBoolean(LearnerParameter.USE_GRADIENT_NORMALIZATION, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useRegularization =
            lp.getBoolean(LearnerParameter.USE_REGULARIZATION, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useBiasInit = lp.getBoolean(LearnerParameter.USE_BIAS_INIT, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useBiasLearningRate =
            lp.getBoolean(LearnerParameter.USE_BIAS_LEARNING_RATE, LearnerParameter.DEFAULT_BOOLEAN);
        boolean m_useUpdater = lp.getBoolean(LearnerParameter.USE_UPDATER, LearnerParameter.DEFAULT_USE_UPDATER);
        boolean m_usePretrain = lp.getBoolean(LearnerParameter.USE_PRETRAIN, LearnerParameter.DEFAULT_BOOLEAN);

        //double
        double m_dropOut = lp.getDouble(LearnerParameter.GLOBAL_DROP_OUT, LearnerParameter.DEFAULT_DOUBLE);
        double m_distributionBinomialProbability =
            lp.getDouble(LearnerParameter.DISTRIBUTION_BINOMIAL_PROBABILITY, LearnerParameter.DEFAULT_DOUBLE);
        double m_distributionMean = lp.getDouble(LearnerParameter.DISTRIBUTION_MEAN, LearnerParameter.DEFAULT_DOUBLE);
        double m_distributionSTD = lp.getDouble(LearnerParameter.DISTRIBUTION_STD, LearnerParameter.DEFAULT_DOUBLE);
        double m_distributionLowerBound =
            lp.getDouble(LearnerParameter.DISTRIBUTION_LOWER_BOUND, LearnerParameter.DEFAULT_DOUBLE);
        double m_distributionUpperBound =
            lp.getDouble(LearnerParameter.DISTRIBUTION_UPPER_BOUND, LearnerParameter.DEFAULT_DOUBLE);
        double m_learningRate =
            lp.getDouble(LearnerParameter.GLOBAL_LEARNING_RATE, LearnerParameter.DEFAULT_LEARNING_RATE);
        double m_gradientNormalizationThreshold = lp.getDouble(LearnerParameter.GRADIENT_NORMALIZATION_THRESHOLD,
            LearnerParameter.DEFAULT_GRADIENT_NORMALIZATION_THRESHOLD);
        double m_l1 = lp.getDouble(LearnerParameter.L1, LearnerParameter.DEFAULT_DOUBLE);
        double m_l2 = lp.getDouble(LearnerParameter.L2, LearnerParameter.DEFAULT_DOUBLE);
        double m_biasInit = lp.getDouble(LearnerParameter.BIAS_INIT, LearnerParameter.DEFAULT_DOUBLE);
        double m_biasLearningRate = lp.getDouble(LearnerParameter.BIAS_LEARNING_RATE, LearnerParameter.DEFAULT_DOUBLE);
        double m_adamMeanDecay =
            lp.getDouble(LearnerParameter.ADAM_MEAN_DECAY, LearnerParameter.DEFAULT_ADAM_MEAN_DECAY);
        double m_adamVarDecay = lp.getDouble(LearnerParameter.ADAM_VAR_DECAY, LearnerParameter.DEFAULT_ADAM_VAR_DECAY);
        double m_adadeltaRho = lp.getDouble(LearnerParameter.ADADELTA_RHO, LearnerParameter.DEFAULT_ADADELTA_RHO);
        double m_rmsDecay = lp.getDouble(LearnerParameter.RMS_DECAY, LearnerParameter.DEFAULT_RMS_DECAY);
        double m_momentum = lp.getDouble(LearnerParameter.MOMENTUM, LearnerParameter.DEFAULT_MOMENTUM);

        //int
        Integer m_distributionBinomialTrails =
            lp.getInteger(LearnerParameter.DISTRIBUTION_BINOMIAL_TRAILS, LearnerParameter.DEFAULT_INT);
        Integer m_seed = lp.getInteger(LearnerParameter.SEED, LearnerParameter.DEFAULT_INT);
        Integer m_iterations = lp.getInteger(LearnerParameter.TRAINING_ITERATIONS, LearnerParameter.DEFAULT_INT);
        Integer m_maxNumLineSearchIterations =
            lp.getInteger(LearnerParameter.MAX_NUMBER_LINE_SEARCH_ITERATIONS, LearnerParameter.DEFAULT_INT);

        //string
        WeightInit m_weightInit =
            WeightInit.valueOf(lp.getString(LearnerParameter.GLOBAL_WEIGHT_INIT, LearnerParameter.DEFAULT_WEIGHT_INIT));
        DL4JDistribution m_distribution = DL4JDistribution
            .valueOf(lp.getString(LearnerParameter.DISTRIBUTION, LearnerParameter.DEFAULT_DISTRIBUTION));
        GradientNormalization m_gradientNormalization = DL4JGradientNormalization
            .fromToString(
                lp.getString(LearnerParameter.GRADIENT_NORMALIZATION, LearnerParameter.DEFAULT_GRADIENT_NORMALIZATION))
            .getDL4JValue();
        Updater m_updater = Updater.valueOf(lp.getString(LearnerParameter.UPDATER, LearnerParameter.DEFAULT_UPDATER));
        Map<Integer, Double> m_momentumAfter = ParameterUtils
            .convertStringToMap(lp.getString(LearnerParameter.MOMENTUM_AFTER, LearnerParameter.DEFAULT_MAP));
        OptimizationAlgorithm m_optimization = DL4JOptimizationAlgorithm
            .fromToString(lp.getString(LearnerParameter.OPTIMIZATION_ALGORITHM, LearnerParameter.DEFAULT_OPTIMIZATION))
            .getDL4JValue();

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
        //ConfigurationUtils.setupLayers(layersCopy, m_nIn);
        //substituted with 'setInputType(InputType.feedForward(m_nIn)'

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

        //infer correct number of inputs and outputs for each layer by using the number of inputs of the first layer
        //and the number of outputs for the following ones
        listBuilder.setInputType(InputType.feedForward(m_nIn));

        return listBuilder;
    }

    /**
     * Iterates over specified list of layers and overwrites the BiasLearningRate parameter with the specified value.
     *
     * @param layers
     * @param newBiasLearningRate
     */
    private void overwriteBiasLearningRate(final List<Layer> layers, final double newBiasLearningRate) {
        for (final Layer l : layers) {
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
        for (final Layer l : layers) {
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
        for (final Layer l : layers) {
            l.setWeightInit(newWeightInit);
        }
    }
}
