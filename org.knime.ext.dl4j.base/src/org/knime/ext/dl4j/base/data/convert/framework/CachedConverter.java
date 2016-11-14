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
 *   02.08.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.data.convert.framework;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;
import org.knime.core.data.convert.java.DataCellToJavaConverter;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.knime.core.data.convert.java.DataCellToJavaConverterRegistry;
import org.knime.ext.dl4j.base.exception.DataCellConversionException;
import org.knime.ext.dl4j.base.exception.UnsupportedDataTypeException;
import org.knime.ext.dl4j.base.util.ConverterUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Class to convert {@link DataCell}s to Java using converter framework. Converter factories are cached and reused to
 * create converters if possible.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class CachedConverter {

    @SuppressWarnings("rawtypes")
    private final Cache<String, DataCellToJavaConverterFactory> m_converterFactoryCache;

    /**
     * Constructor for class {@link CachedConverter}. Creates a new cache to save converter factories.
     */
    public CachedConverter() {
        m_converterFactoryCache = CacheBuilder.newBuilder().maximumSize(10).build();
    }

    /**
     * Converts the specified {@link DataCell} to an object of the specified class using the converter framework. For
     * every unseen combination of cell type and class a new converter factory is created and added to the cache. If the
     * converter factory already exists in cache it is just retrieved and used to create a new converter for the cell.
     *
     * @param cellToConvert the cell which should be converted
     * @param classOfResultType the class to convert to
     * @return the converted cell
     * @throws ExecutionException if an exception is thrown while loading the converter factory from cache
     * @throws DataCellConversionException if an exception is thrown during cell conversion
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> T convertDataCellToJava(final DataCell cellToConvert, final Class<T> classOfResultType)
        throws ExecutionException, DataCellConversionException {

        ConverterUtils.checkMissing(cellToConvert);

        String factoryIdent = cellToConvert.getClass().toString() + "," + classOfResultType.toString();

        DataCellToJavaConverterFactory converterFactory =
            m_converterFactoryCache.get(factoryIdent, new Callable<DataCellToJavaConverterFactory>() {
                /**
                 * If the converter to retrieve was not yet accessed from the converter registry we try to access it.
                 */
                @Override
                public DataCellToJavaConverterFactory call() throws UnsupportedDataTypeException {
                    Optional<DataCellToJavaConverterFactory<? extends DataValue, T>> fac = DataCellToJavaConverterRegistry
                        .getInstance().getConverterFactories(cellToConvert.getType(), classOfResultType).stream().findFirst();
                    if (!fac.isPresent()) {
                        throw new UnsupportedDataTypeException(
                            "No converter for DataCell of type: " + cellToConvert.getType().getName() + " to Class: "
                                + classOfResultType.getSimpleName() + " available.");
                    }
                    return fac.get();
                }
            });
        final DataCellToJavaConverter<DataValue, T> converter = converterFactory.create();
        try {
            return converter.convert(cellToConvert);
        } catch (final Exception e) {
            throw new DataCellConversionException("Conversion of DataCell of type: " + cellToConvert.getType().getName()
                + " failed. Error message: " + e.getMessage(), e);
        }
    }

}
