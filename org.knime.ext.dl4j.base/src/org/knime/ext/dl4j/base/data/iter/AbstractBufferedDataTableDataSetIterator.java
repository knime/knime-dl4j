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
 *   14.07.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.data.iter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.ext.dl4j.base.data.convert.row.IDataRowToDataSetConverter;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * Abstract superclass for {@link DataSetIterator}s iterating a {@link BufferedDataTable}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public abstract class AbstractBufferedDataTableDataSetIterator implements DataSetIterator {

    /**
     *
     */
    private static final long serialVersionUID = -4611650142289529410L;

    private static final Logger LOGGER = Logger.getLogger(AbstractBufferedDataTableDataSetIterator.class);

    private final IDataRowToDataSetConverter m_rowConverter;

    private final BufferedDataTable m_table;

    private CloseableRowIterator m_tableIterator;

    private final int m_batchSize;

    private int m_cursor;

    private final int m_featureLength;

    private final int m_targetLength;

    /**
     * Super constructor for class AbstractBufferedDataTableDataSetIterator specifying the table to iterate and the
     * batch size to use.
     *
     * @param table the table to iterate
     * @param batchSize the number of examples contained in the {@link DataSet} returned by the next() method.
     * @param rowConverter
     */
    public AbstractBufferedDataTableDataSetIterator(final BufferedDataTable table, final int batchSize,
        final IDataRowToDataSetConverter rowConverter) {
        m_table = table;
        m_batchSize = batchSize;
        m_rowConverter = rowConverter;
        m_tableIterator = table.iterator();
        m_featureLength = rowConverter.featureLength();
        m_targetLength = rowConverter.targetLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return m_tableIterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSet next() {
        final int numberOfRows = (int)Math.min(m_batchSize, m_table.size() - m_cursor);
        final List<DataSet> rows = new ArrayList<DataSet>();
        //iterate and convert rows
        for (int r = 0; r < numberOfRows; r++) {
            final DataRow row = m_tableIterator.next();
            try {
                rows.add(m_rowConverter.convert(row));
            } catch (Exception e) {
                LOGGER.error("Error in row " + row.getKey() + " : " + e);
            }
            m_cursor++;
        }
        return DataSet.merge(rows);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSet next(final int num) {
        throw new UnsupportedOperationException("next(num) not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int totalExamples() {
        return (int)m_table.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inputColumns() {
        return m_featureLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int totalOutcomes() {
        return m_targetLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        m_cursor = 0;
        m_tableIterator.close();
        m_tableIterator = m_table.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int batch() {
        return m_batchSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int cursor() {
        return m_cursor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int numExamples() {
        return (int)m_table.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPreProcessor(final DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException("setPreProcessor is not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("setPreProcessor is not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean resetSupported() {
        return true;
    }
}
