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
 * Parameters for layers contained in deep networks.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public enum LayerParameter {
        /** The number of output neurons. */
    NUMBER_OF_OUTPUTS, /** The number of contrastive divergence iterations. */
    RBM_ITERATIONS, /** The activation function. */
    ACTIVATION, /** The weight initialization strategy. */
    WEIGHT_INIT, /** The loss function type. */
    LOSS_FUNCTION, /** The hidden unit type. */
    HIDDEN_UNIT, /** The visible unit type. */
    VISIBLE_UNIT, /** The drop out rate. */
    DROP_OUT, /** The type of pooling for subsampling layer. */
    POOLING_TYPE, /** The size of kernel. */
    KERNEL_SIZE, /** The stride of kernel. */
    STRIDE, /** Whether to use specified input output numbers. */
    LEARNING_RATE, /** A hyper parameter for local response normalization. */
    LRN_ALPHA, /** A hyper parameter for local response normalization. */
    LRN_BETA, /** A hyper parameter for local response normalization. */
    LRN_K, /** A hyper parameter for local response normalization. */
    LRN_N, /** The amount of 'noise' or 'corruption' to use. */
    CORRUPTION_LEVEL;

    //default values for learner parameters
    public static final Double DEFAULT_LRN_ALPHA = 0.0001;

    public static final Double DEFAULT_LRN_BETA = 0.75;

    public static final int DEFAULT_LRN_K = 2;

    public static final int DEFAULT_LRN_N = 5;

    public static final String DEFAULT_ACTIVATION = "ReLU";

    public static final String DEFAULT_WEIGHT_INIT = "XAVIER";

    public static final String DEFAULT_LOSS = "Mean Squared Error";

    public static final String DEFAULT_TRANSFORMATION = "BINARY";

    public static final String DEFAULT_POOLING = "MAX";

    public static final String DEFAULT_MULTIINT = "2,2";
}
