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
package org.knime.ext.dl4j.base.util;

import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;
import org.knime.core.data.convert.java.DataCellToJavaConverter;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.knime.core.data.convert.java.DataCellToJavaConverterRegistry;
import org.knime.ext.dl4j.base.exception.DataCellConversionException;

/**
 * Utility class for type conversion using the converter extension point.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class ConverterUtils {

    private ConverterUtils() {
        // Utility class
    }

    /**
     * Converts the specified {@link DataCell} to the specified class. For conversion the KNIME converter framework is
     * used and in this case the converter created by the converter factory returned by
     * <code>getPreferredConverterFactory</code> using the specified class.
     *
     * @param cellToConvert the cell which should be converted
     * @param classOfResultType the class of the type which should be converted to
     * @return the result of the converter
     * @throws DataCellConversionException if no converter is available for the specified cell and class, if there are
     *             errors during conversion
     */
    public static <T> T convertDataCellToJava(final DataCell cellToConvert, final Class<T> classOfResultType)
        throws DataCellConversionException {

        checkMissing(cellToConvert);

        final Optional<DataCellToJavaConverterFactory<DataValue, T>> converterFactory = DataCellToJavaConverterRegistry
            .getInstance().getPreferredConverterFactory(cellToConvert.getType(), classOfResultType);

        if (!converterFactory.isPresent()) {
            throw new DataCellConversionException(
                "No converter for DataCell of type: " + cellToConvert.getType().getName() + " available.");
        }
        final DataCellToJavaConverter<DataValue, T> converter = converterFactory.get().create();
        try {
            return converter.convert(cellToConvert);
        } catch (final Exception e) {
            throw new DataCellConversionException(
                "Conversion of DataCell of type: " + cellToConvert.getType().getName()
                    + " failed. Converter implementation " + "may contain errors. Error message: " + e.getMessage());
        }
    }

    /**
     * Checks if the specified cell is missing. If so an Exception is thrown.
     *
     * @param cell the cell to check
     * @throws DataCellConversionException if the cell is missing
     */
    public static void checkMissing(final DataCell cell) throws DataCellConversionException {
        if(cell.isMissing()) {
            throw new DataCellConversionException("Input table must not contain Missing Values.");
        }
    }
}
