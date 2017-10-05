/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.com; Email: contact@knime.com
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.ext.dl4j.base.exception.DataCellConversionException;
import org.knime.ext.dl4j.base.exception.UnsupportedDataTypeException;

/**
 * Utility class containing helper methods for {@link BufferedDataTable} related types.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class TableUtils {

    private TableUtils() {
        // Utility class
    }

    /**
     * Converts a {@link DataRow} to a {@link List} of {@link DataCell}s.
     *
     * @param row the row to convert
     * @return the list of cells
     */
    public static List<DataCell> toListOfCells(final DataRow row) {
        final List<DataCell> cells = new ArrayList<>();
        for (int i = 0; i < row.getNumCells(); i++) {
            cells.add(row.getCell(i));
        }
        return cells;
    }

    /**
     * Converts a {@link DataTableSpec} to a {@link List} of {@link DataColumnSpec}s.
     *
     * @param spec the spec to convert
     * @return the list of column spec
     */
    public static List<DataColumnSpec> toListOfColumnSpec(final DataTableSpec spec) {
        final List<DataColumnSpec> columnSpecs = new ArrayList<>();
        for (int i = 0; i < spec.getNumColumns(); i++) {
            columnSpecs.add(spec.getColumnSpec(i));
        }
        return columnSpecs;
    }

    /**
     * Appends a {@link DataColumnSpec} of the specified {@link DataType} to the given {@link DataTableSpec}.
     *
     * @param tableSpec the spec, which should be extended
     * @param columnName the name of the appended column
     * @param dataType the type of the column to append
     * @return new {@link DataTableSpec} containing one more column spec
     */
    public static DataTableSpec appendColumnSpec(final DataTableSpec tableSpec, final String columnName,
        final DataType dataType) {
        final List<DataColumnSpec> columnSpecs = toListOfColumnSpec(tableSpec);
        final DataColumnSpecCreator listColSpecCreator = new DataColumnSpecCreator(columnName, dataType);
        columnSpecs.add(listColSpecCreator.createSpec());
        return new DataTableSpec(columnSpecs.toArray(new DataColumnSpec[columnSpecs.size()]));
    }

    /**
     * Calculate the resulting feature vector length if the specified {@link DataRow} is converted into vector format
     * for deep learning. The cells with the specified indices will be skipped (normally a label or target columns
     * contained in the input table).
     *
     * @param cells the {@link DataRow} which is expected for conversion
     * @param indices List containing indices of columns which should be skipped
     * @return the calculated feature vector length
     * @throws DataCellConversionException if the row contains a type which is not yet supported for conversion
     */
    public static int calculateFeatureVectorLengthExcludingIndices(final DataRow cells, final List<Integer> indices)
        throws DataCellConversionException {
        int recordLength = 0;

        int i = 0;
        for (final DataCell cell : cells) {
            if (indices.contains(i)) {
                i++;
                continue;
            }
            recordLength += calculateFeatureVectorLength(cell);
            i++;
        }

        return recordLength;
    }

    /**
     * Calculate the resulting feature vector length if the specified {@link DataCell} is converted into vector format
     * for deep learning.
     *
     * @param cell the {@link DataCell} which is expected for conversion
     * @return the calculated feature vector length
     * @throws DataCellConversionException if the cell is not supported for conversion
     */
    public static int calculateFeatureVectorLength(final DataCell cell) throws DataCellConversionException {
        int recordLength = 0;
        return recordLength += ConverterUtils.convertDataCellToJava(cell, double[].class).length;
    }

    /**
     * Calculate the resulting feature vector length if the cells of the specified {@link DataRow} is converted into
     * vector format for deep learning.
     *
     * @param row the {@link DataRow} which is expected for conversion
     * @return the calculated feature vector length
     * @throws DataCellConversionException if the row contains a type which is not yet supported for conversion
     */
    public static int calculateFeatureVectorLength(final DataRow row) throws DataCellConversionException {
        int recordLength = 0;
        for (DataCell cell : row) {
            recordLength += calculateFeatureVectorLength(cell);
        }
        return recordLength;
    }

    /**
     * Calculate the resulting feature vector length if the specified {@link DataRow} is converted into vector format
     * for deep learning. Only the cells with the specified indices will be used.
     *
     * @param cells the {@link DataRow} which is expected for conversion
     * @param indices List containing indices of columns which should be used
     * @return the calculated feature vector length
     * @throws DataCellConversionException if the row contains a type which is not yet supported for conversion
     */
    public static int calculateFeatureVectorLengthOnlyIndices(final DataRow cells, final List<Integer> indices)
        throws DataCellConversionException {
        int recordLength = 0;

        int i = 0;
        for (final DataCell cell : cells) {
            if (!indices.contains(i)) {
                i++;
                continue;
            }
            recordLength += calculateFeatureVectorLength(cell);
            i++;
        }

        return recordLength;
    }

    /**
     * Determine the maximum length of the collections contained in a collection type column, meaning searching for the
     * row with the longest collection and returning its size.
     *
     * @param table the table containing the collection type column
     * @param sequenceColumnIndex the index of the collection type column in the table
     * @return the maximum length of all collections
     * @throws UnsupportedDataTypeException if column in table with specified index is no collection type
     */
    public static int calculateMaximumSequenceLength(final BufferedDataTable table, final int sequenceColumnIndex)
        throws UnsupportedDataTypeException {
        final CloseableRowIterator tableIter = table.iterator();
        int maxLength = 0;
        while (tableIter.hasNext()) {
            final DataCell cell = tableIter.next().getCell(sequenceColumnIndex);
            if (!cell.getType().isCollectionType()) {
                throw new UnsupportedDataTypeException(
                    "Expected type of sequence column to be collection but was: " + cell.getType().getName());
            }
            final CollectionDataValue sequenceCell = (CollectionDataValue)cell;
            if (sequenceCell.size() > maxLength) {
                maxLength = sequenceCell.size();
            }
        }
        return maxLength;
    }

    /**
     * Determine all possible values of Strings contained in a collection type column.
     *
     * @param table the table containing the collection type column containing Strings
     * @param labelColumnIndex the index of the collection type column containing strings in the table
     * @return {@link List} containing all possible String values
     * @throws UnsupportedDataTypeException if column in table with specified index is no collection type or if the
     *             element type of the collection type column is not String
     */
    public static List<String> determineDistinctLabelsInCollectionColumn(final BufferedDataTable table,
        final int labelColumnIndex) throws UnsupportedDataTypeException {
        final Set<String> labels = new HashSet<>();
        final CloseableRowIterator iter = table.iterator();
        while (iter.hasNext()) {
            final DataCell cell = iter.next().getCell(labelColumnIndex);
            if (!cell.getType().isCollectionType()) {
                throw new UnsupportedDataTypeException(
                    "Expected type of label column to be collection but was: " + cell.getType().getName());
            } else if (!((CollectionDataValue)cell).getElementType().isCompatible(StringValue.class)) {
                throw new UnsupportedDataTypeException("Expected element type of label column to be String but was: "
                    + ((CollectionDataValue)cell).getElementType().getName());
            }
            final CollectionDataValue collectionCell = (CollectionDataValue)cell;
            final Iterator<DataCell> collectionCellIter = collectionCell.iterator();
            while (collectionCellIter.hasNext()) {
                final StringCell labelCell = (StringCell)collectionCellIter.next();
                labels.add(labelCell.getStringValue());
            }
        }
        return new ArrayList<String>(labels);
    }

    /**
     * Creates a list of column indices from a list of column names and corresponding {@link DataTableSpec}.
     *
     * @param columnNames the list of column names
     * @param spec the spec corresponding to the column names
     * @return a list of the indices of the specified columns in the table
     */
    public static List<Integer> indicesFromColumnNames(final List<String> columnNames, final DataTableSpec spec) {
        List<Integer> indices = new ArrayList<Integer>();
        for (String columnName : columnNames) {
            indices.add(spec.findColumnIndex(columnName));
        }
        return indices;
    }

    /**
     * Checks if the table has rows. If not throws a RuntimeException.
     *
     * @param table the table to check
     */
    public static void checkForEmptyTable(final BufferedDataTable table) {
        if (table.size() == 0) {
            throw new IllegalStateException("Input table must not be empty!");
        }
    }

    /**
     * Checks if the specified row has missing cells at the specified indices.
     *
     * @param row the row to check
     * @param indices the indices to check
     * @return true if all cells at the specified indices are not missing, false if otherwise
     */
    public static boolean hasMissing(final DataRow row, final int... indices) {
        for (int i : indices) {
            if (row.getCell(i).isMissing()) {
                return true;
            }
        }
        return false;
    }
}
