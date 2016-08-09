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

import org.knime.core.node.BufferedDataTable;
import org.knime.ext.dl4j.base.data.convert.row.LabelTargetDataRowToDataSetConverter;

/**
 * Implementation of {@link AbstractBufferedDataTableDataSetIterator} for classification. Expects a table containing
 * feature columns, which will be flattened, and one label column, which will be converted to one-hot.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class ClassificationBufferedDataTableDataSetIterator extends AbstractBufferedDataTableDataSetIterator {

    /**
     *
     */
    private static final long serialVersionUID = 5874206797134932076L;

    private final List<String> m_distinctLabels;

    /**
     * Constructor for class ClassificationBufferedDataTableDataSetIterator specifying the table to iterate, the index
     * of the label column in the table, the batch size, the list of distinct labels for one-hot vector conversion, and
     * if in train mode.
     *
     * @param table the table to iterate
     * @param labelColumnIndex the index of the label column in the label
     * @param batchSize the batch size
     * @param distinctLabels the list of distinct labels
     * @param isTrain if in train mode or not
     * @throws Exception
     */
    public ClassificationBufferedDataTableDataSetIterator(final BufferedDataTable table, final int labelColumnIndex,
        final int batchSize, final List<String> distinctLabels, final boolean isTrain) throws Exception {

        super(table, batchSize, new LabelTargetDataRowToDataSetConverter(table.iterator().next(), distinctLabels,
            labelColumnIndex, isTrain));
        m_distinctLabels = distinctLabels;
    }

    /**
     * Convenience constructor for class ClassificationBufferedDataTableDataSetIterator specifying the table to iterate
     * and the batch size, hence test mode is used.
     *
     * @param table
     * @param batchSize
     * @throws Exception
     */
    public ClassificationBufferedDataTableDataSetIterator(final BufferedDataTable table, final int batchSize)
        throws Exception {
        super(table, batchSize, new LabelTargetDataRowToDataSetConverter(table.iterator().next()));
        m_distinctLabels = new ArrayList<String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getLabels() {
        return m_distinctLabels;
    }

}
