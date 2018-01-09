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
 *   02.08.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.data.convert.extension;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;
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
    private final Cache<String, DL4JConverter> m_converterCache;

    /**
     * Constructor for class {@link CachedConverter}. Creates a new cache to save {@link DL4JConverter}s.
     */
    public CachedConverter() {
        m_converterCache = CacheBuilder.newBuilder().maximumSize(10).build();
    }

    /**
     * Converts the specified {@link DataCell} to an object of the specified class using the DL4JConverter extension
     * point. For every unseen combination of cell type and class a new converter is created and added to the cache. If
     * the converter already exists in cache it is just retrieved and used to perform the conversion.
     *
     * @param cellToConvert the cell which should be converted
     * @param classOfResultType the class to convert to
     * @return the converted cell
     * @throws ExecutionException if an exception is thrown while loading the converter from cache
     * @throws DataCellConversionException if an exception is thrown during cell conversion
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> T convertDataCellToJava(final DataCell cellToConvert, final Class<T> classOfResultType)
        throws ExecutionException, DataCellConversionException {
        ConverterUtils.checkMissing(cellToConvert);

        String factoryIdent = cellToConvert.getClass().toString() + "," + classOfResultType.toString();

        DL4JConverter<DataValue, T> converter = m_converterCache.get(factoryIdent, new Callable<DL4JConverter>() {
            /**
             * If the converter to retrieve was not yet accessed from the converter registry we try to access it.
             */
            @Override
            public DL4JConverter<DataValue, T> call() throws UnsupportedDataTypeException {
                Optional<DL4JConverter<DataValue, T>> c =
                    DL4JConverterRegistry.getInstance().getConverter(cellToConvert.getType(), classOfResultType);
                return c.orElseThrow(() -> new UnsupportedDataTypeException(
                    "No converter for DataCell of type " + cellToConvert.getType().getName() + " to class "
                        + classOfResultType.getSimpleName() + " available."));
            }
        });
        try {
            return converter.convert(cellToConvert);
        } catch (final Exception e) {
            throw new DataCellConversionException("Conversion of DataCell of type " + cellToConvert.getType().getName()
                + " failed: " + e.getMessage(), e);
        }
    }

}
