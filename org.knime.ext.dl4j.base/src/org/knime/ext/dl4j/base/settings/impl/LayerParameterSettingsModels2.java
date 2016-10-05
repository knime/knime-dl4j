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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.IParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.enumerate.LayerParameter;

/**
 * Implementation of {@link IParameterSettingsModels} to store and create {@link SettingsModel}s for
 * {@link LayerParameter}s.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class LayerParameterSettingsModels2 extends AbstractMapSetParameterSettingsModels<LayerParameter> {

    static final int DEFAULT_INT = 1;

    static final Double DEFAULT_DOUBLE = 0.0;

    @Override
    public SettingsModel createParameter(final LayerParameter enumerate) throws IllegalArgumentException {
        switch (enumerate) {
            //Integer parameters
            case NUMBER_OF_OUTPUTS:
                return new SettingsModelIntegerBounded("number_of_outputs", DEFAULT_INT, 1, Integer.MAX_VALUE);
            case RBM_ITERATIONS:
                return new SettingsModelIntegerBounded("rbm_iterations", DEFAULT_INT, 1, Integer.MAX_VALUE);
            case LRN_K:
                return new SettingsModelIntegerBounded("lrn_k", LayerParameter.DEFAULT_LRN_K, 0, Integer.MAX_VALUE);
            case LRN_N:
                return new SettingsModelIntegerBounded("lrn_n", LayerParameter.DEFAULT_LRN_N, 0, Integer.MAX_VALUE);

            //multi Integer parameter
            case KERNEL_SIZE:
                return new SettingsModelString("kernel_size", LayerParameter.DEFAULT_MULTIINT);
            case STRIDE:
                return new SettingsModelString("stride", LayerParameter.DEFAULT_MULTIINT);

            //String parameters
            case ACTIVATION:
                return new SettingsModelString("activation", LayerParameter.DEFAULT_ACTIVATION);
            case WEIGHT_INIT:
                return new SettingsModelString("weight_init", LayerParameter.DEFAULT_WEIGHT_INIT);
            case LOSS_FUNCTION:
                return new SettingsModelString("loss_function", LayerParameter.DEFAULT_LOSS);
            case HIDDEN_UNIT:
                return new SettingsModelString("hidden_unit", LayerParameter.DEFAULT_TRANSFORMATION);
            case VISIBLE_UNIT:
                return new SettingsModelString("visible_unit", LayerParameter.DEFAULT_TRANSFORMATION);
            case POOLING_TYPE:
                return new SettingsModelString("pooling_type", LayerParameter.DEFAULT_POOLING);

            //Double parameters
            case DROP_OUT:
                return new SettingsModelDoubleBounded("drop_out", DEFAULT_DOUBLE, 0, 1);
            case LEARNING_RATE:
                return new SettingsModelDoubleBounded("learning_rate", LayerParameter.DEFAULT_LAYER_LEARNING_RATE, 0,
                    Double.MAX_VALUE);
            case LRN_ALPHA:
                return new SettingsModelDoubleBounded("lrn_alpha", LayerParameter.DEFAULT_LRN_ALPHA, 0,
                    Double.MAX_VALUE);
            case LRN_BETA:
                return new SettingsModelDoubleBounded("lrn_beta", LayerParameter.DEFAULT_LRN_BETA, 0, Double.MAX_VALUE);
            case CORRUPTION_LEVEL:
                return new SettingsModelDoubleBounded("corruption_level", DEFAULT_DOUBLE, 0, 1);
            default:
                throw new IllegalArgumentException("No case defined for Layer Parameter: " + enumerate);
        }
    }
}
