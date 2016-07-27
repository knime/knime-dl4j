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
package org.knime.ext.dl4j.base.settings.impl;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.IParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;

/**
 * Implementation of {@link IParameterSettingsModels} to store and create {@link SettingsModel}s for
 * {@link LearnerParameter}s.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class LearnerParameterSettingsModels implements IParameterSettingsModels<LearnerParameter> {

    private SettingsModelIntegerBounded m_seed;

    private SettingsModelIntegerBounded m_trainingIterations;

    private SettingsModelString m_optimizationAlgorithm;

    private SettingsModelString m_gradientNormalization;

    private SettingsModelString m_updater;

    private SettingsModelString m_momentumAfter;

    private SettingsModelString m_trainingsMode;

    private SettingsModelString m_globalWeightInit;

    private SettingsModelDoubleBounded m_globalDropOut;

    private SettingsModelDoubleBounded m_globalLearningRate;

    private SettingsModelDoubleBounded m_L1;

    private SettingsModelDoubleBounded m_L2;

    private SettingsModelDoubleBounded m_gradientNormalizationThreshold;

    private SettingsModelDoubleBounded m_momentum;

    private SettingsModelBoolean m_useSeed;

    private SettingsModelBoolean m_useRegularization;

    private SettingsModelBoolean m_useGradientNormalization;

    private SettingsModelBoolean m_usePretrain;

    private SettingsModelBoolean m_useBackprop;

    private SettingsModelBoolean m_useFinetune;

    private SettingsModelBoolean m_useMomentum;

    private SettingsModelBoolean m_useDropConnect;

    private SettingsModelBoolean m_usePretrainedUpdater;

    private SettingsModelBoolean m_useGlobalDropOut;

    private SettingsModelBoolean m_useGlobalWeightInit;

    private SettingsModelBoolean m_useGlobalLearningRate;

    private final List<SettingsModel> m_allInitializedSettings = new ArrayList<>();

    @Override
    public SettingsModel createParameter(final LearnerParameter enumerate) throws IllegalArgumentException {
        switch (enumerate) {
            //Integer parameters
            case SEED:
                return new SettingsModelIntegerBounded("seed", LearnerParameter.DEFAULT_INT, Integer.MIN_VALUE,
                    Integer.MAX_VALUE);
            case TRAINING_ITERATIONS:
                return new SettingsModelIntegerBounded("training_iterations", LearnerParameter.DEFAULT_INT, 1,
                    Integer.MAX_VALUE);

            //String parameters
            case OPTIMIZATION_ALGORITHM:
                return new SettingsModelString("optimization_algorithm", LearnerParameter.DEFAULT_OPTIMIZATION);
            case GRADIENT_NORMALIZATION:
                return new SettingsModelString("gradient_normalization", LearnerParameter.DEFAULT_GRADIENTNORM);
            case UPDATER:
                return new SettingsModelString("updater", LearnerParameter.DEFAULT_UPDATER);
            case MOMENTUM_AFTER:
                return new SettingsModelString("momentum_after", LearnerParameter.DEFAULT_MAP);
            case TRAINING_MODE:
                return new SettingsModelString("trainings_mode", LearnerParameter.DEFAULT_TRAININGS_MODE);
            case GLOBAL_WEIGHT_INIT:
                return new SettingsModelString("global_weight_init", LearnerParameter.DEFAULT_WEIGHT_INIT);

            //Double parameters
            case GLOBAL_DROP_OUT:
                return new SettingsModelDoubleBounded("global_drop_out", LearnerParameter.DEFAULT_DOUBLE, 0, 1);
            case GLOBAL_LEARNING_RATE:
                return new SettingsModelDoubleBounded("global_learning_rate", LearnerParameter.DEFAULT_LEARNING_RATE, 0,
                    Double.MAX_VALUE);
            case L1:
                return new SettingsModelDoubleBounded("l1", LearnerParameter.DEFAULT_DOUBLE, 0, Double.MAX_VALUE);
            case L2:
                return new SettingsModelDoubleBounded("l2", LearnerParameter.DEFAULT_DOUBLE, 0, Double.MAX_VALUE);
            case GRADIENT_NORMALIZATION_THRESHOLD:
                return new SettingsModelDoubleBounded("gradient_normalization_threshold",
                    LearnerParameter.DEFAULT_DOUBLE, 0, Double.MAX_VALUE);
            case MOMENTUM:
                return new SettingsModelDoubleBounded("momentum", LearnerParameter.DEFAULT_DOUBLE, 0, Double.MAX_VALUE);

            //boolean parameters
            case USE_SEED:
                return new SettingsModelBoolean("use_seed", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_REGULARIZATION:
                return new SettingsModelBoolean("use_regularization", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_GRADIENT_NORMALIZATION:
                return new SettingsModelBoolean("use_gradient_normalization", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_FINETUNE:
                return new SettingsModelBoolean("use_finetune", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_PRETRAIN:
                return new SettingsModelBoolean("use_pretrain", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_BACKPROP:
                return new SettingsModelBoolean("use_backprop", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_MOMENTUM:
                return new SettingsModelBoolean("use_momentum", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_DROP_CONNECT:
                return new SettingsModelBoolean("use_drop_connect", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_PRETRAINED_UPDATER:
                return new SettingsModelBoolean("use_pretrained_updater", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_GLOBAL_DROP_OUT:
                return new SettingsModelBoolean("use_global_drop_out", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_GLOBAL_WEIGHT_INIT:
                return new SettingsModelBoolean("use_global_weight_init", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_GLOBAL_LEARNING_RATE:
                return new SettingsModelBoolean("use_global_learning_rate", LearnerParameter.DEFAULT_BOOLEAN);
            default:
                throw new IllegalArgumentException("No case defined for Learner Parameter: " + enumerate);
        }
    }

    @Override
    public void setParameter(final LearnerParameter enumerate) throws IllegalArgumentException {
        switch (enumerate) {
            case GLOBAL_DROP_OUT:
                m_globalDropOut = (SettingsModelDoubleBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_globalDropOut)) {
                    m_allInitializedSettings.add(m_globalDropOut);
                }
                break;
            case GRADIENT_NORMALIZATION:
                m_gradientNormalization = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_gradientNormalization)) {
                    m_allInitializedSettings.add(m_gradientNormalization);
                }
                break;
            case GRADIENT_NORMALIZATION_THRESHOLD:
                m_gradientNormalizationThreshold = (SettingsModelDoubleBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_gradientNormalizationThreshold)) {
                    m_allInitializedSettings.add(m_gradientNormalizationThreshold);
                }
                break;
            case L1:
                m_L1 = (SettingsModelDoubleBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_L1)) {
                    m_allInitializedSettings.add(m_L1);
                }
                break;
            case L2:
                m_L2 = (SettingsModelDoubleBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_L2)) {
                    m_allInitializedSettings.add(m_L2);
                }
                break;
            case GLOBAL_LEARNING_RATE:
                m_globalLearningRate = (SettingsModelDoubleBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_globalLearningRate)) {
                    m_allInitializedSettings.add(m_globalLearningRate);
                }
                break;
            case MOMENTUM:
                m_momentum = (SettingsModelDoubleBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_momentum)) {
                    m_allInitializedSettings.add(m_momentum);
                }
                break;
            case MOMENTUM_AFTER:
                m_momentumAfter = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_momentumAfter)) {
                    m_allInitializedSettings.add(m_momentumAfter);
                }
                break;
            case OPTIMIZATION_ALGORITHM:
                m_optimizationAlgorithm = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_optimizationAlgorithm)) {
                    m_allInitializedSettings.add(m_optimizationAlgorithm);
                }
                break;
            case SEED:
                m_seed = (SettingsModelIntegerBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_seed)) {
                    m_allInitializedSettings.add(m_seed);
                }
                break;
            case TRAINING_ITERATIONS:
                m_trainingIterations = (SettingsModelIntegerBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_trainingIterations)) {
                    m_allInitializedSettings.add(m_trainingIterations);
                }
                break;
            case UPDATER:
                m_updater = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_updater)) {
                    m_allInitializedSettings.add(m_updater);
                }
                break;
            case USE_BACKPROP:
                m_useBackprop = (SettingsModelBoolean)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_useBackprop)) {
                    m_allInitializedSettings.add(m_useBackprop);
                }
                break;
            case USE_DROP_CONNECT:
                m_useDropConnect = (SettingsModelBoolean)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_useDropConnect)) {
                    m_allInitializedSettings.add(m_useDropConnect);
                }
                break;
            case USE_GRADIENT_NORMALIZATION:
                m_useGradientNormalization = (SettingsModelBoolean)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_useGradientNormalization)) {
                    m_allInitializedSettings.add(m_useGradientNormalization);
                }
                break;
            case USE_MOMENTUM:
                m_useMomentum = (SettingsModelBoolean)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_useMomentum)) {
                    m_allInitializedSettings.add(m_useMomentum);
                }
                break;
            case USE_FINETUNE:
                m_useFinetune = (SettingsModelBoolean)createParameter(enumerate);
                addToSet(m_useFinetune);
                break;
            case USE_PRETRAIN:
                m_usePretrain = (SettingsModelBoolean)createParameter(enumerate);
                addToSet(m_usePretrain);
                break;
            case USE_REGULARIZATION:
                m_useRegularization = (SettingsModelBoolean)createParameter(enumerate);
                addToSet(m_useRegularization);
                break;
            case USE_SEED:
                m_useSeed = (SettingsModelBoolean)createParameter(enumerate);
                addToSet(m_useSeed);
                break;
            case USE_PRETRAINED_UPDATER:
                m_usePretrainedUpdater = (SettingsModelBoolean)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_usePretrainedUpdater)) {
                    m_allInitializedSettings.add(m_usePretrainedUpdater);
                }
                break;
            case TRAINING_MODE:
                m_trainingsMode = (SettingsModelString)createParameter(enumerate);
                addToSet(m_trainingsMode);
                break;
            case GLOBAL_WEIGHT_INIT:
                m_globalWeightInit = (SettingsModelString)createParameter(enumerate);
                addToSet(m_globalWeightInit);
                break;
            case USE_GLOBAL_DROP_OUT:
                m_useGlobalDropOut = (SettingsModelBoolean)createParameter(enumerate);
                addToSet(m_useGlobalDropOut);
                break;
            case USE_GLOBAL_WEIGHT_INIT:
                m_useGlobalWeightInit = (SettingsModelBoolean)createParameter(enumerate);
                addToSet(m_useGlobalWeightInit);
                break;
            case USE_GLOBAL_LEARNING_RATE:
                m_useGlobalLearningRate = (SettingsModelBoolean)createParameter(enumerate);
                addToSet(m_useGlobalLearningRate);
                break;
            default:
                throw new IllegalArgumentException("No case defined for Learner Parameter: " + enumerate);
        }
    }

    private void addToSet(final SettingsModel model) {
        if (!m_allInitializedSettings.contains(model)) {
            m_allInitializedSettings.add(model);
        }
    }

    public SettingsModelBoolean getUseGlobalLearningRate() {
        return m_useGlobalLearningRate;
    }

    public SettingsModelString getTrainingsMode() {
        return m_trainingsMode;
    }

    public SettingsModelBoolean getUsePretrainedUpdater() {
        return m_usePretrainedUpdater;
    }

    public SettingsModelIntegerBounded getSeed() {
        return m_seed;
    }

    public SettingsModelIntegerBounded getTrainingIterations() {
        return m_trainingIterations;
    }

    public SettingsModelString getOptimizationAlgorithm() {
        return m_optimizationAlgorithm;
    }

    public SettingsModelString getGradientNormalization() {
        return m_gradientNormalization;
    }

    public SettingsModelString getUpdater() {
        return m_updater;
    }

    public SettingsModelString getMomentumAfter() {
        return m_momentumAfter;
    }

    public SettingsModelDoubleBounded getGlobalDropOut() {
        return m_globalDropOut;
    }

    public SettingsModelDoubleBounded getGlobalLearningRate() {
        return m_globalLearningRate;
    }

    public SettingsModelDoubleBounded getL1() {
        return m_L1;
    }

    public SettingsModelDoubleBounded getL2() {
        return m_L2;
    }

    public SettingsModelDoubleBounded getGradientNormalizationThreshold() {
        return m_gradientNormalizationThreshold;
    }

    public SettingsModelDoubleBounded getMomentum() {
        return m_momentum;
    }

    public SettingsModelBoolean getUseSeed() {
        return m_useSeed;
    }

    public SettingsModelBoolean getUseRegularization() {
        return m_useRegularization;
    }

    public SettingsModelBoolean getUseGradientNormalization() {
        return m_useGradientNormalization;
    }

    public SettingsModelBoolean getUsePretrain() {
        return m_usePretrain;
    }

    public SettingsModelBoolean getUseBackprop() {
        return m_useBackprop;
    }

    public SettingsModelBoolean getUseFinetune() {
        return m_useFinetune;
    }

    public SettingsModelBoolean getUseMomentum() {
        return m_useMomentum;
    }

    public SettingsModelBoolean getUseDropConnect() {
        return m_useDropConnect;
    }

    public SettingsModelString getGobalWeightInit() {
        return m_globalWeightInit;
    }

    public SettingsModelBoolean getUseGlobalDropOut() {
        return m_useGlobalDropOut;
    }

    public SettingsModelBoolean getUseGlobalWeightInit() {
        return m_useGlobalWeightInit;
    }

    @Override
    public List<SettingsModel> getAllInitializedSettings() {
        return m_allInitializedSettings;
    }

}
