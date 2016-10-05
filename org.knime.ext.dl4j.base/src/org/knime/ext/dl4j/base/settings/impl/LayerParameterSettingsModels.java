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
@Deprecated
public class LayerParameterSettingsModels implements IParameterSettingsModels<LayerParameter> {

    static final int DEFAULT_INT = 1;

    static final Double DEFAULT_DOUBLE = 0.0;

    private SettingsModelIntegerBounded m_numberOfOutputs;

    private SettingsModelIntegerBounded m_rbmIterations;

    private SettingsModelIntegerBounded m_lrnK;

    private SettingsModelIntegerBounded m_lrnN;

    private SettingsModelString m_kernelSize;

    private SettingsModelString m_stride;

    private SettingsModelString m_activation;

    private SettingsModelString m_weightInit;

    private SettingsModelString m_lossFunction;

    private SettingsModelString m_hiddenUnit;

    private SettingsModelString m_visibleUnit;

    private SettingsModelString m_poolingType;

    private SettingsModelDoubleBounded m_dropOut;

    private SettingsModelDoubleBounded m_learningRate;

    private SettingsModelDoubleBounded m_lrnAlpha;

    private SettingsModelDoubleBounded m_lrnBeta;

    private SettingsModelDoubleBounded m_corruption_level;

    private final List<SettingsModel> m_allInitializedSettings = new ArrayList<>();

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

    @Override
    public void setParameter(final LayerParameter enumerate) throws IllegalArgumentException {
        switch (enumerate) {
            case ACTIVATION:
                m_activation = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_activation)) {
                    m_allInitializedSettings.add(m_activation);
                }
                break;
            case DROP_OUT:
                m_dropOut = (SettingsModelDoubleBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_dropOut)) {
                    m_allInitializedSettings.add(m_dropOut);
                }
                break;
            case HIDDEN_UNIT:
                m_hiddenUnit = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_hiddenUnit)) {
                    m_allInitializedSettings.add(m_hiddenUnit);
                }
                break;
            case KERNEL_SIZE:
                m_kernelSize = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_kernelSize)) {
                    m_allInitializedSettings.add(m_kernelSize);
                }
                break;
            case LOSS_FUNCTION:
                m_lossFunction = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_lossFunction)) {
                    m_allInitializedSettings.add(m_lossFunction);
                }
                break;
            case NUMBER_OF_OUTPUTS:
                m_numberOfOutputs = (SettingsModelIntegerBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_numberOfOutputs)) {
                    m_allInitializedSettings.add(m_numberOfOutputs);
                }
                break;
            case POOLING_TYPE:
                m_poolingType = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_poolingType)) {
                    m_allInitializedSettings.add(m_poolingType);
                }
                break;
            case RBM_ITERATIONS:
                m_rbmIterations = (SettingsModelIntegerBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_rbmIterations)) {
                    m_allInitializedSettings.add(m_rbmIterations);
                }
                break;
            case STRIDE:
                m_stride = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_stride)) {
                    m_allInitializedSettings.add(m_stride);
                }
                break;
            case VISIBLE_UNIT:
                m_visibleUnit = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_visibleUnit)) {
                    m_allInitializedSettings.add(m_visibleUnit);
                }
                break;
            case WEIGHT_INIT:
                m_weightInit = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_weightInit)) {
                    m_allInitializedSettings.add(m_weightInit);
                }
                break;
            case LEARNING_RATE:
                m_learningRate = (SettingsModelDoubleBounded)createParameter(enumerate);
                addToSet(m_learningRate);
                break;
            case LRN_ALPHA:
                m_lrnAlpha = (SettingsModelDoubleBounded)createParameter(enumerate);
                addToSet(m_lrnAlpha);
                break;
            case LRN_BETA:
                m_lrnBeta = (SettingsModelDoubleBounded)createParameter(enumerate);
                addToSet(m_lrnBeta);
                break;
            case LRN_K:
                m_lrnK = (SettingsModelIntegerBounded)createParameter(enumerate);
                addToSet(m_lrnK);
                break;
            case LRN_N:
                m_lrnN = (SettingsModelIntegerBounded)createParameter(enumerate);
                addToSet(m_lrnN);
                break;
            case CORRUPTION_LEVEL:
                m_corruption_level = (SettingsModelDoubleBounded)createParameter(enumerate);
                addToSet(m_corruption_level);
                break;
            default:
                throw new IllegalArgumentException("No case defined for Layer Parameter: " + enumerate);
        }
    }

    private void addToSet(final SettingsModel model) {
        if (!m_allInitializedSettings.contains(model)) {
            m_allInitializedSettings.add(model);
        }
    }

    public SettingsModelDoubleBounded getCorruptionLevel() {
        return m_corruption_level;
    }

    public SettingsModelDoubleBounded getLearningRate() {
        return m_learningRate;
    }

    public SettingsModelIntegerBounded getLrnK() {
        return m_lrnK;
    }

    public SettingsModelIntegerBounded getLrnN() {
        return m_lrnN;
    }

    public SettingsModelIntegerBounded getNumberOfOutputs() {
        return m_numberOfOutputs;
    }

    public SettingsModelIntegerBounded getRbmIterations() {
        return m_rbmIterations;
    }

    public SettingsModelString getKernelSize() {
        return m_kernelSize;
    }

    public SettingsModelString getStride() {
        return m_stride;
    }

    public SettingsModelString getActivation() {
        return m_activation;
    }

    public SettingsModelString getWeightInit() {
        return m_weightInit;
    }

    public SettingsModelString getLossFunction() {
        return m_lossFunction;
    }

    public SettingsModelString getHiddenUnit() {
        return m_hiddenUnit;
    }

    public SettingsModelString getVisibleUnit() {
        return m_visibleUnit;
    }

    public SettingsModelString getPoolingType() {
        return m_poolingType;
    }

    public SettingsModelDoubleBounded getDropOut() {
        return m_dropOut;
    }

    public SettingsModelDoubleBounded getLrnAlpha() {
        return m_lrnAlpha;
    }

    public SettingsModelDoubleBounded getLrnBeta() {
        return m_lrnBeta;
    }

    @Override
    public List<SettingsModel> getAllInitializedSettings() {
        return m_allInitializedSettings;
    }
}
