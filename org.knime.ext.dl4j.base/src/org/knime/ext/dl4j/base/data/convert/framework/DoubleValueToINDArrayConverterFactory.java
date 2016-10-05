/*
 * ------------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 */
package org.knime.ext.dl4j.base.data.convert.framework;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.convert.java.DataCellToJavaConverter;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Implementation of {@link DataCellToJavaConverterFactory} creating a converter from DoubleValue to INDArray.
 *
 * @author David Kolb, KNIME.com GmbH
 * @deprecated use DoubleValueToDoubleArrayConverterFactory instead
 */
@Deprecated
public class DoubleValueToINDArrayConverterFactory implements DataCellToJavaConverterFactory<DoubleValue, INDArray> {

    /**
     * Create a new converter from {@link DoubleValue} to {@link INDArray}. The convert method will return an INDArray
     * containing {@link DoubleValue#getDoubleValue()}.
     */
    @Override
    public DataCellToJavaConverter<DoubleValue, INDArray> create() {
        return (v) -> Nd4j.create(new double[]{v.getDoubleValue()});
    }

    @Override
    public Class<DoubleValue> getSourceType() {
        return DoubleValue.class;
    }

    @Override
    public Class<INDArray> getDestinationType() {
        return INDArray.class;
    }

    @Override
    public String getIdentifier() {
        return getClass().getName() + "(" + DoubleValue.class.getSimpleName() + "," + INDArray.class.toString() + ","
            + "" + ")";
    }
}
