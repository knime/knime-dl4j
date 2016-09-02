/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   30.08.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.settings.enumerate.dl4j;

import org.deeplearning4j.nn.weights.WeightInit;

public enum DL4JWeightInit {
        DISTRIBUTION(WeightInit.DISTRIBUTION), ZERO(WeightInit.ZERO), XAVIER(WeightInit.XAVIER), RELU(WeightInit.RELU);

    private WeightInit m_DL4JValue;

    /**
     *
     */
    private DL4JWeightInit(final WeightInit weightInit) {
        m_DL4JValue = weightInit;
    }

    /**
     * Converts string representation of this enum back to this enum.
     *
     * @param toString the value from toString of this enum
     * @return this enum corresponding to toString
     */
    public static DL4JWeightInit fromToString(final String toString) {
        for (final DL4JWeightInit e : DL4JWeightInit.values()) {
            if (e.toString().equals(toString)) {
                return e;
            }
        }
        //TODO add backward compatibility for nodes which saved the original weight init value
        return null;
    }

    /**
     * Get the in dl4j usable {@link WeightInit} corresponding to this enum.
     *
     * @return dl4j usable {@link WeightInit}
     */
    public WeightInit getDL4JValue() {
        return m_DL4JValue;
    }

    @Override
    public String toString() {
        switch (this) {
            case DISTRIBUTION:
                return "Distribution";
            case RELU:
                return "ReLU";
            case XAVIER:
                return "XAVIER";
            case ZERO:
                return "Zeroes";
            default:
                return super.toString();
        }
    }
}
