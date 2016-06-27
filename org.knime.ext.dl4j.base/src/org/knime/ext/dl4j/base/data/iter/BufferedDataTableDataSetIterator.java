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
package org.knime.ext.dl4j.base.data.iter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.knime.core.data.convert.java.DataCellToJavaConverterRegistry;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.knime.ext.dl4j.base.util.NDArrayUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.FeatureUtil;

/**
 * Implementation of {@link DataSetIterator}, which iterates a {@link BufferedDataTable}
 * and converts the specified number of rows to {@link DataSet} on the fly when calling 
 * next(). DataSets are used as input for DL4J network training. Conversion is done using 
 * the DataCellTojavaConverter extension point, hence, all {@link DataType}s which can be
 * converted to {@link INDArray} are supported.
 * 
 * @author David Kolb, KNIME.com GmbH
 */
public class BufferedDataTableDataSetIterator implements DataSetIterator{

	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(BufferedDataTableDataSetIterator.class);
	
	private static final long serialVersionUID = 1L;
	
	private final BufferedDataTable m_table;
	private final int m_labelColumnIndex;
	private final int m_batchSize;
	private final List<String> m_distinctLabels;
	
	private int m_recordLength;
	private CloseableRowIterator m_tableIterator;	
	private int m_cursor;
	
	private final boolean m_isTrain;
	
	/**
	 * Constructor for class BufferedDataTableDataSetIterator. 
	 * 
	 * @param table the table to iterate
	 * @param labelColumnName name of possible label column
	 * @param batchSize the number of rows to return for next next() call, hence the number
	 * 					of examples contained in the returned DataSet of next()
	 * @param distinctLabels list of all distinct labels
	 * @param isTrain flag if test or train mode, meaning whether to expect labels or not
	 * @throws Exception
	 */
	public BufferedDataTableDataSetIterator(final BufferedDataTable table, final String labelColumnName, 
			final int batchSize, final List<String> distinctLabels, boolean isTrain) throws Exception {
		m_table = table;
		m_batchSize = batchSize;
		
		if(distinctLabels != null){
			m_distinctLabels = distinctLabels;
		} else {
			m_distinctLabels = new ArrayList<String>();
		}
		
		m_isTrain = isTrain;
		if(isTrain){			
			m_labelColumnIndex = table.getSpec().findColumnIndex(labelColumnName);
		} else {
			m_labelColumnIndex = -1;
		}
		
		m_tableIterator = table.iterator();
		m_recordLength = TableUtils.calculateFeatureVectorLength(m_tableIterator.next(), m_labelColumnIndex);
		reset();
	}
	
	/**
	 * Constructor for class BufferedDataTableDataSetIterator if no labels are available, hence, test mode is used.
	 * 
	 * @param table the table to iterate
	 * @param batchSize the number of rows to return for next next() call, hence the number
	 * 					of examples contained in the returned DataSet of next()
	 * @throws Exception
	 */
	public BufferedDataTableDataSetIterator(final BufferedDataTable table, final int batchSize) throws Exception {
		this(table, null, batchSize, null, false);
	}
	
	@Override
	public boolean hasNext() {		
		return m_tableIterator.hasNext();
	}

	/**
	 * Returns a DataSet containing two INDArrays. One array for the features and one for the labels. Both arrays
	 * have batchSize number of rows. In the feature array each row contains the corresponding row of the table 
	 * converted to INDArray. Conversion method is specified by {@link DataCellToJavaConverterFactory}s which
	 * convert to INDArray registered in converter extension point. In the label array each row contains the label
	 * for each feature row converted to one-hot representation. If test mode this array will only contain zeroes.
	 */
	@Override
	public DataSet next() {
		//if table.size % batch != 0
		long numberOfRows = Math.min(m_batchSize, m_table.size() - m_cursor);
		INDArray dataMatrix = Nd4j.create((int)numberOfRows, inputColumns());
		INDArray labelsMatrix = null;
		if(m_isTrain){
			labelsMatrix = Nd4j.create((int)numberOfRows, m_distinctLabels.size());
		} else {
			labelsMatrix = Nd4j.create((int)numberOfRows, 1);
		}
		//loop over rows of current batch
		for (int k = 0; k < numberOfRows; k++) {
            DataRow row = m_tableIterator.next();     
            //list of arrays which will be concatenated into one row
            List<INDArray> dataRow = new ArrayList<>();
            //loop over cells of current row
            for(int i = 0; i < row.getNumCells(); i++){          	
            	try {
            		DataCell cell = row.getCell(i);            	
            		//if label convert to one hot vector
            		if(i == m_labelColumnIndex && m_isTrain){   
            			//first convert nominal value to string
            			Optional<DataCellToJavaConverterFactory<DataCell, String>> factory =
            					DataCellToJavaConverterRegistry.getInstance().getConverterFactory(cell.getType(), String.class); 
            			String label = ConverterUtils.convertWithFactory(factory, cell);
            			INDArray labelOutcomeVector = FeatureUtil.toOutcomeVector(m_distinctLabels.indexOf(label), m_distinctLabels.size());
            			labelsMatrix.putRow(k, labelOutcomeVector);
            			//if collection convert every entry using existing converters
            		} else if (cell.getType().isCollectionType()){
            			Optional<DataCellToJavaConverterFactory<DataCell, INDArray[]>> factory =
            					DataCellToJavaConverterRegistry.getInstance().getConverterFactory(cell.getType(), INDArray[].class);             		
            			INDArray[] convertedCollction = ConverterUtils.convertWithFactory(factory, cell);
            			dataRow.addAll(Arrays.asList(convertedCollction));
            			//else convert directly
            		} else {    
            			Optional<DataCellToJavaConverterFactory<DataCell, INDArray>> factory =
            					DataCellToJavaConverterRegistry.getInstance().getConverterFactory(cell.getType(), INDArray.class);       
            			dataRow.add(ConverterUtils.convertWithFactory(factory, cell));            		          	
            		}					
				} catch (Exception e) {
					logger.coding("Problem with input conversion",e);
				}            	           	
            }
            INDArray linearConcat = NDArrayUtils.linearConcat(dataRow);
            if(linearConcat.length() != inputColumns()){
            	logger.error("Length of current input in row: " + row.getKey() + " does not match expected length. Possible images or collections "
            			+ "may not be of same size.");
            }
            dataMatrix.putRow(k, linearConcat);
            m_cursor++;
		}		
		
		return new DataSet(dataMatrix, labelsMatrix);
	}

	@Override
	public DataSet next(int num) {
		throw new UnsupportedOperationException("next(num) not supported");
	}

	@Override
	public int totalExamples() {
		return (int)m_table.size();
	}

	/**
	 * The number of features of the input data.
	 */
	@Override
	public int inputColumns() {
		return m_recordLength;
	}

	@Override
	public int totalOutcomes() {
		return m_distinctLabels.size();
	}

	@Override
	public void reset() {
		m_cursor = 0;
		m_tableIterator.close();	
		m_tableIterator = m_table.iterator();	
	}

	@Override
	public int batch() {
		return m_batchSize;
	}

	@Override
	public int cursor() {
		return m_cursor;
	}

	@Override
	public int numExamples() {
		return (int)m_table.size();
	}

	@Override
	public void setPreProcessor(DataSetPreProcessor preProcessor) {
		throw new UnsupportedOperationException("setPreProcessor is not supported");		
	}

	@Override
	public List<String> getLabels() {
		return m_distinctLabels;
	}
}
