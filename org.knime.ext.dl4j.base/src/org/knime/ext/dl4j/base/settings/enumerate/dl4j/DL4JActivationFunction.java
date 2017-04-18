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
package org.knime.ext.dl4j.base.settings.enumerate.dl4j;

import org.knime.core.node.NodeLogger;
import org.nd4j.linalg.activations.Activation;

/**
 * Wrapper for {@link Activation} for better String representation of values.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public enum DL4JActivationFunction {
        /** Cubic */
        cube(Activation.CUBE),
        /** Exponential Linear Unit */
        elu(Activation.ELU),
        /** Rectified Linear Unit */
        relu(Activation.RELU),
        /** Randomized Leaky Rectified Linear Unit */
        rrelu(Activation.RRELU),
        /** Hyperbolic Tangent */
        tanh(Activation.TANH),
        /** Hard Sigmoid */
        hardsigmoid(Activation.HARDSIGMOID),
        /** Hard TanH */
        hardtanh(Activation.HARDTANH),
        /** Identity */
        identity(Activation.IDENTITY),
        /** Rational TanH */
        rationaltanh(Activation.RATIONALTANH),
        /** Sigmoid */
        sigmoid(Activation.SIGMOID),
        /** Softmax */
        softmax(Activation.SOFTMAX),
        /** Leaky Rectified Linear Unit */
        leakyrelu(Activation.LEAKYRELU),
        /** Softsign */
        softsign(Activation.SOFTSIGN),
        /** Softplus */
        softplus(Activation.SOFTPLUS),;

    /** the corresponding dl4j value of this enum. */
    private Activation m_DL4JValue;

    private static final NodeLogger logger = NodeLogger.getLogger(DL4JActivationFunction.class);

    private DL4JActivationFunction(final Activation activation) {
        m_DL4JValue = activation;
    }

    /**
     * Converts string representation of this enum back to this enum.
     *
     * @param toString the value from toString of this enum
     * @return this enum corresponding to toString
     */
    public static DL4JActivationFunction fromToString(final String toString) {
        for (final DL4JActivationFunction e : DL4JActivationFunction.values()) {
            if (e.toString().equals(toString)) {
                return e;
            }
        }
        //default fallback
        logger.warn("No activation function for parameter value: " + toString
            + " could be found. The default '" + DL4JActivationFunction.relu + "' will be used.");
        return DL4JActivationFunction.relu;
    }

    /**
     * Get the in dl4j usable {@link Activation} corresponding to this enum.
     *
     * @return dl4j usable activation function string
     */
    public Activation getDL4JValue() {
        return m_DL4JValue;
    }

    @Override
    public String toString() {
        switch (this) {
            case hardtanh:
                return "HardTanH";
            case leakyrelu:
                return "LeakyReLU";
            case relu:
                return "ReLU";
            case sigmoid:
                return "Sigmoid";
            case softmax:
                return "Softmax";
            case softplus:
                return "SoftPlus";
            case softsign:
                return "Softsign";
            case tanh:
                return "TanH";
            case identity:
                return "Identity";
            case cube:
                return "Cubic";
            case elu:
                return "ELU";
            case hardsigmoid:
                return "HardSigmoid";
            case rationaltanh:
                return "RationalTanH";
            case rrelu:
                return "RandomizedLeakyReLU";
            default:
                return super.toString();
        }
    }
}
