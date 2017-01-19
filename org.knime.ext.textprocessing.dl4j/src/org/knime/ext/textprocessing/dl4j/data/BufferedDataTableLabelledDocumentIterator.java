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
package org.knime.ext.textprocessing.dl4j.data;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;
import org.knime.ext.dl4j.base.exception.DataCellConversionException;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.knime.ext.dl4j.base.util.TableUtils;

/**
 * {@link LabelAwareIterator} for a {@link BufferedDataTable}. Expects a column contained in the data table holding one
 * document and one label per row.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class BufferedDataTableLabelledDocumentIterator implements LabelAwareIterator {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(BufferedDataTableLabelledDocumentIterator.class);

    private final BufferedDataTable m_table;

    private CloseableRowIterator m_tableIterator;

    private CloseableRowIterator m_hasNextTableIterator;

    private final int m_documentColumnIndex;

    private final int m_labelColumnIndex;

    private final LabelsSource m_labelsSource;

    private final List<String> m_labels;

    private int m_currentRow = 0;

    private final boolean m_skipMissing;

    /**
     * Convenience constructor for class BufferedDataTableLabelledDocumentIterator. Equal to calling
     * <code>this(table, documentColumnName, labelColumnName, false)</code>
     *
     * @param table the table to iterate
     * @param documentColumnName the name of the document column
     * @param labelColumnName the name of the label column
     */
    public BufferedDataTableLabelledDocumentIterator(final BufferedDataTable table, final String documentColumnName,
        final String labelColumnName) {
        this(table, documentColumnName, labelColumnName, false);
    }

    /**
     * Constructor for class BufferedDataTableLabelledDocumentIterator.
     *
     * @param table the table to iterate
     * @param documentColumnName the name of the document column
     * @param labelColumnName the name of the label column
     * @param skipMissing whether rows containing missing cells should be skipped
     */
    public BufferedDataTableLabelledDocumentIterator(final BufferedDataTable table, final String documentColumnName,
        final String labelColumnName, final boolean skipMissing) {
        m_skipMissing = skipMissing;
        m_table = table;
        m_documentColumnIndex = table.getSpec().findColumnIndex(documentColumnName);
        m_labelColumnIndex = table.getSpec().findColumnIndex(labelColumnName);
        m_tableIterator = table.iterator();
        m_hasNextTableIterator = table.iterator();
        m_labels = new ArrayList<>();
        m_labelsSource = initLabelsSource();
        this.reset();
    }

    @Override
    public boolean hasNextDocument() {
        if (m_skipMissing) {
            return hasNextNonEmptyRow();
        } else {
            return m_tableIterator.hasNext();
        }

    }

    /**
     * Search for a next row that does not contain empty cells. This is needed if we skip rows containing missing cells
     * in the <code>nextDocument()</code> method.
     *
     * @return true if at least one of the following rows does not contain a missing cell, false otherwise or if the end
     *         of the table is reached
     */
    private boolean hasNextNonEmptyRow() {
        if (!m_hasNextTableIterator.hasNext()) {
            return false;
        } else {
            final DataRow row = m_hasNextTableIterator.next();
            if (containsMissing(row)) {
                return hasNextNonEmptyRow();
            } else {
                return true;
            }
        }
    }

    /**
     * Convenience helper because we always check for the same indices.
     *
     * @param row
     * @return
     */
    private boolean containsMissing(final DataRow row) {
        return TableUtils.hasMissing(row, new int[]{m_documentColumnIndex, m_labelColumnIndex});
    }

    /**
     * Returns the next {@link LabelledDocument} containing a document and a corresponding label from the
     * {@link BufferedDataTable}.
     *
     * @return the next labelled document
     */
    @Override
    public LabelledDocument nextDocument() {
        final DataRow row = m_tableIterator.next();
        final DataCell documentCell = row.getCell(m_documentColumnIndex);

        if (m_skipMissing && containsMissing(row)) {
            return nextDocument();
        }

        String documentContent = null;
        try {
            documentContent = ConverterUtils.convertDataCellToJava(documentCell, String.class);
        } catch (DataCellConversionException e) {
            throw new RuntimeException("Error in row " + row.getKey() + " : " + e.getMessage(), e);
        }

        String documentLabel = m_labels.get(m_currentRow);
        m_currentRow++;

        final LabelledDocument output = new LabelledDocument();
        output.setContent(documentContent);
        output.setLabel(documentLabel);

        return output;
    }

    @Override
    public void reset() {
        m_tableIterator.close();
        m_tableIterator = m_table.iterator();
        m_hasNextTableIterator.close();
        m_hasNextTableIterator = m_table.iterator();
        m_currentRow = 0;
    }

    @Override
    public LabelsSource getLabelsSource() {
        return m_labelsSource;
    }

    /**
     * Iterates over {@link BufferedDataTable} and collects all labels.
     *
     * @return {@link LabelsSource} containing the collected labels.
     */
    private LabelsSource initLabelsSource() {
        while (m_tableIterator.hasNext()) {
            final DataRow row = m_tableIterator.next();
            final DataCell labelCell = row.getCell(m_labelColumnIndex);

            if (m_skipMissing && labelCell.isMissing()) {
                continue;
            }

            try {
                m_labels.add(ConverterUtils.convertDataCellToJava(labelCell, String.class));
            } catch (DataCellConversionException e) {
                throw new RuntimeException("Error in row " + row.getKey() + " : " + e.getMessage(), e);
            }
        }
        return new LabelsSource(m_labels);
    }
}
