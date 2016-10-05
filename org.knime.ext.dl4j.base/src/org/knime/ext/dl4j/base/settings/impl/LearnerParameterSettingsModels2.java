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

import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
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
public class LearnerParameterSettingsModels2 extends AbstractMapSetParameterSettingsModels<LearnerParameter> {

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
            case MAX_NUMBER_LINE_SEARCH_ITERATIONS:
                return new SettingsModelIntegerBounded("max_number_line_search_iterations",
                    LearnerParameter.DEFAULT_MAX_NUMBER_LINE_SEARCH_ITERATIONS, 1, Integer.MAX_VALUE);
            case DISTRIBUTION_BINOMIAL_TRAILS:
                return new SettingsModelIntegerBounded("distribution_binomial_trails", LearnerParameter.DEFAULT_INT, 1,
                    Integer.MAX_VALUE);

            //String parameters
            case OPTIMIZATION_ALGORITHM:
                return new SettingsModelString("optimization_algorithm", LearnerParameter.DEFAULT_OPTIMIZATION);
            case GRADIENT_NORMALIZATION:
                return new SettingsModelString("gradient_normalization",
                    LearnerParameter.DEFAULT_GRADIENT_NORMALIZATION);
            case UPDATER:
                return new SettingsModelString("updater", LearnerParameter.DEFAULT_UPDATER);
            case MOMENTUM_AFTER:
                return new SettingsModelString("momentum_after", LearnerParameter.DEFAULT_MAP);
            case TRAINING_MODE:
                return new SettingsModelString("trainings_mode", LearnerParameter.DEFAULT_TRAININGS_MODE);
            case GLOBAL_WEIGHT_INIT:
                return new SettingsModelString("global_weight_init", LearnerParameter.DEFAULT_WEIGHT_INIT);
            case LEARNING_RATE_AFTER:
                return new SettingsModelString("learning_rate_after", LearnerParameter.DEFAULT_MAP);
            case DISTRIBUTION:
                return new SettingsModelString("distribution", LearnerParameter.DEFAULT_DISTRIBUTION);
            case LR_POLICY:
                return new SettingsModelString("lr_policy", LearnerParameter.DEFAULT_LEARNING_RATE_POLICY);

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
                    LearnerParameter.DEFAULT_GRADIENT_NORMALIZATION_THRESHOLD, 0, Double.MAX_VALUE);
            case MOMENTUM:
                return new SettingsModelDoubleBounded("momentum", LearnerParameter.DEFAULT_MOMENTUM, 0,
                    Double.MAX_VALUE);
            case ADADELTA_RHO:
                return new SettingsModelDoubleBounded("adadelta_rho", LearnerParameter.DEFAULT_ADADELTA_RHO, 0,
                    Double.MAX_VALUE);
            case ADAM_MEAN_DECAY:
                return new SettingsModelDoubleBounded("adam_mean_decay", LearnerParameter.DEFAULT_ADAM_MEAN_DECAY, 0,
                    Double.MAX_VALUE);
            case ADAM_VAR_DECAY:
                return new SettingsModelDoubleBounded("adam_var_decay", LearnerParameter.DEFAULT_ADAM_VAR_DECAY, 0,
                    Double.MAX_VALUE);
            case RMS_DECAY:
                return new SettingsModelDoubleBounded("rms_decay", LearnerParameter.DEFAULT_RMS_DECAY, 0,
                    Double.MAX_VALUE);
            case BIAS_INIT:
                return new SettingsModelDouble("bias_init", LearnerParameter.DEFAULT_DOUBLE);
            case BIAS_LEARNING_RATE:
                return new SettingsModelDoubleBounded("bias_learning_rate", LearnerParameter.DEFAULT_LEARNING_RATE, 0,
                    Double.MAX_VALUE);
            case DISTRIBUTION_BINOMIAL_PROBABILITY:
                return new SettingsModelDoubleBounded("distribution_binomial_probability",
                    LearnerParameter.DEFAULT_DOUBLE, 0, 1);
            case DISTRIBUTION_LOWER_BOUND:
                return new SettingsModelDouble("distribution_lower_bound", LearnerParameter.DEFAULT_DOUBLE);
            case DISTRIBUTION_MEAN:
                return new SettingsModelDouble("distribution_mean", LearnerParameter.DEFAULT_DOUBLE);
            case DISTRIBUTION_STD:
                return new SettingsModelDoubleBounded("distribution_std", LearnerParameter.DEFAULT_DOUBLE, 0,
                    Double.MAX_VALUE);
            case DISTRIBUTION_UPPER_BOUND:
                return new SettingsModelDouble("distribution_upper_bound", LearnerParameter.DEFAULT_DOUBLE);
            case LR_POLICY_DECAY_RATE:
                return new SettingsModelDoubleBounded("lr_policy_decay_rate", LearnerParameter.DEFAULT_DOUBLE, 0,
                    Double.MAX_VALUE);
            case LR_POLICY_POWER:
                return new SettingsModelDoubleBounded("lr_policy_power", LearnerParameter.DEFAULT_DOUBLE, 0,
                    Double.MAX_VALUE);
            case LR_POLICY_SCORE_DECAY:
                return new SettingsModelDoubleBounded("lr_policy_score_decay", LearnerParameter.DEFAULT_DOUBLE, 0, 1);
            case LR_POLICY_STEPS:
                return new SettingsModelDoubleBounded("lr_policy_steps", 1, 1, Double.MAX_VALUE);

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
            case USE_UPDATER:
                return new SettingsModelBoolean("use_updater", LearnerParameter.DEFAULT_USE_UPDATER);
            case USE_ADVANCED_LEARNING_RATE:
                return new SettingsModelBoolean("use_advanced_learning_rate", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_BIAS_INIT:
                return new SettingsModelBoolean("use_bias_init", LearnerParameter.DEFAULT_BOOLEAN);
            case USE_BIAS_LEARNING_RATE:
                return new SettingsModelBoolean("use_bias_learning_rate", LearnerParameter.DEFAULT_BOOLEAN);
            default:
                throw new IllegalArgumentException("No case defined for Learner Parameter: " + enumerate);
        }
    }

}
