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

/**
 * Activation function types supported by DL4J. Enumeration wrapper as there is no enum in DL4J.
 */
public enum DL4JActivationFunction {
        relu("relu"), tanh("tanh"), sigmoid("sigmoid"), softmax("softmax"), hardtanh("hardtanh"),
        leakyrelu("leakyrelu"), maxout("maxout"), softsign("softsign"), softplus("softplus"), identity("identity");

    /** the corresponding dl4j value of this enum. */
    private String m_DL4JValue;

    private DL4JActivationFunction(final String activation) {
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
        return null;
    }

    /**
     * Get the in dl4j usable activation function string corresponding to this enum.
     *
     * @return dl4j usable activation function string
     */
    public String getDL4JValue() {
        return m_DL4JValue;
    }

    @Override
    public String toString() {
        switch (this) {
            case hardtanh:
                return "HardTanH";
            case leakyrelu:
                return "LeakyReLU";
            case maxout:
                return "MaxOut";
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
            default:
                return super.toString();
        }
    }
}
