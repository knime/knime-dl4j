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
package org.knime.ext.dl4j.base.settings.enumerate;

/**
 * Parameters for training of deep networks.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public enum LearnerParameter {
        /** Whether a seed or not. */
    USE_SEED, /** The seed used for reproducibility between runs. */
    SEED, /** The number of training iterations. */
    TRAINING_ITERATIONS, /** The type of optimization algorithm to use. */
    OPTIMIZATION_ALGORITHM, /** The global learning rate to use. */
    GLOBAL_LEARNING_RATE, /** Whether to use global learning or not. */
    USE_GLOBAL_LEARNING_RATE, /** Whether to use regularization or not. */
    USE_REGULARIZATION, /** The l1 regularization coefficient. */
    L1, /** The l2 regularization coefficient. */
    L2, /** Whether to use gradient normalization or not. */
    USE_GRADIENT_NORMALIZATION, /** The gradient normalization strategy. */
    GRADIENT_NORMALIZATION, /**
                             * Threshold for gradient normalization, only used for GradientNormalization.ClipL2PerLayer,
                             * GradientNormalization.ClipL2PerParamType, and
                             * GradientNormalization.ClipElementWiseAbsoluteValue. Not used otherwise. L2 threshold for
                             * first two types of clipping, or absolute value threshold for last type of clipping.
                             */
    GRADIENT_NORMALIZATION_THRESHOLD, /**
                                       * Whether to do pretraining or not, only applies for RBMs and states whether to
                                       * run contrastive divergence or not.
                                       */
    USE_PRETRAIN, /** Whether to do finetuning or not. */
    USE_FINETUNE, /** Whether to do backpropagation or not. */
    USE_BACKPROP, /** The kind of updater to use. */
    UPDATER, /** Whether a momentum or not. */
    USE_MOMENTUM, /** The momentum rate. */
    MOMENTUM, /** The momentum rate to use for every iteration. */
    MOMENTUM_AFTER, /** Whether to use drop connect or not. */
    USE_DROP_CONNECT, /** The drop out rate. */
    GLOBAL_DROP_OUT, /** Whether to overwrite the drop out rate of each layer. */
    USE_GLOBAL_DROP_OUT, /** Whether to use the updater of a previously trained net or create a new one. */
    USE_PRETRAINED_UPDATER, /** The kind of training to do. e.g. supervised, unsupervised. */
    TRAINING_MODE, /** The global weight initialization strategy. */
    GLOBAL_WEIGHT_INIT, /** Whether to overwrite weight initialization strategy of each layer. */
    USE_GLOBAL_WEIGHT_INIT;

    //default values for learner parameters
    public static final int DEFAULT_INT = 1;

    public static final Double DEFAULT_DOUBLE = 0.0;

    public static final Double DEFAULT_LEARNING_RATE = 0.1;

    public static final String DEFAULT_OPTIMIZATION = "Stochastic Gradient Descent";

    public static final String DEFAULT_GRADIENTNORM = "Clip Element Wise Absolute Value";

    public static final String DEFAULT_UPDATER = "NESTEROVS";

    public static final String DEFAULT_MAP = "";

    public static final String DEFAULT_TRAININGS_MODE = "SUPERVISED";

    public static final String DEFAULT_WEIGHT_INIT = "XAVIER";

    public static final String DEFAULT_IN_OUT_OPTIONS = "NOT_OPTIONAL";

    public static final boolean DEFAULT_BOOLEAN = false;
}
