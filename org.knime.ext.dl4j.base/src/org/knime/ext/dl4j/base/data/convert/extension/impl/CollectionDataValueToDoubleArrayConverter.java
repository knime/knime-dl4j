/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   16.11.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.data.convert.extension.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.ext.dl4j.base.data.convert.extension.BaseDL4JConverter;
import org.knime.ext.dl4j.base.data.convert.extension.DL4JConverter;
import org.knime.ext.dl4j.base.data.convert.extension.DL4JConverterRegistry;
import org.knime.ext.dl4j.base.exception.UnsupportedDataTypeException;

/**
 * DL4JConverter that converts a CollectionDataValue to double[]. The element type of the collection needs to be
 * convertible by the DL4JConverter extension point as well. For conversion every element of the collection will be
 * converted to double[]. Then the resulting list of double[] arrays will be flattened.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class CollectionDataValueToDoubleArrayConverter extends BaseDL4JConverter<CollectionDataValue, double[]> {

    /**
     * Constructor for class CollectionDataValueToDoubleArrayConverter.
     */
    public CollectionDataValueToDoubleArrayConverter() {
        super(CollectionDataValue.class, double[].class, BaseDL4JConverter.DEFAULT_PRIORITY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] convert(final CollectionDataValue source) throws Exception {
        DataType collectionElementType = source.getElementType();

        Optional<DL4JConverter<DataValue, double[]>> elementConverterOptional =
            DL4JConverterRegistry.getInstance().getConverter(collectionElementType, double[].class);
        // check if element type can be converted to double[]
        // the element converter
        DL4JConverter<DataValue, double[]> elementConverter = elementConverterOptional
            .orElseThrow(() -> new UnsupportedDataTypeException("No converter for DataCell of type "
                + collectionElementType.getName() + " contained in collection of type "
                + source.getClass().getSimpleName() + " to class " + double[].class.getSimpleName() + " available."));

        // convert every element of the collection
        List<double[]> convertedElements = new ArrayList<>();
        for (DataCell cell : source) {
            convertedElements.add(elementConverter.convert(cell));
        }

        // flatten the collection
        return convertedElements.stream().flatMapToDouble(a -> Arrays.stream(a)).toArray();
    }
}
