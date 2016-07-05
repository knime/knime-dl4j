package org.knime.ext.dl4j.testing.nodes.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.convert.java.DataCellToJavaConverterFactory;
import org.knime.core.data.convert.java.DataCellToJavaConverterRegistry;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.vector.doublevector.DoubleVectorCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.data.iter.BufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;

/**
 * This is the model implementation of DL4JModelTester.
 * 
 *
 * @author KNIME
 */
public class TableToDL4JVectorNodeModel extends AbstractDLNodeModel {

    private SettingsModelString m_labelColumn;
    private DataTableSpec m_outputSpec;
    
	/**
     * Constructor for the node model.
     */
    protected TableToDL4JVectorNodeModel() {   
    	super(new PortType[] {BufferedDataTable.TYPE}, new PortType[] {
    			BufferedDataTable.TYPE});   	
    }
	
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
    	DataTableSpec tableSpec = inData[0].getDataTableSpec();
    	
    	DataSetIterator input = null;
    	if(m_labelColumn.getStringValue() != null && !m_labelColumn.getStringValue().isEmpty()){
    		String labelColumnName = m_labelColumn.getStringValue();
    		List<String> labels = new ArrayList<String>();
			for(DataCell cell: tableSpec.getColumnSpec(labelColumnName).getDomain().getValues()){
				Optional<DataCellToJavaConverterFactory<DataValue, String>> factory =
						DataCellToJavaConverterRegistry.getInstance().getPreferredConverterFactory(cell.getType(), String.class);
				labels.add(ConverterUtils.convertWithFactory(factory, cell));
			}
    		input = new BufferedDataTableDataSetIterator(inData[0], labelColumnName, 1, labels, true);
    	} else{
    		input = new BufferedDataTableDataSetIterator(inData[0], 1);
    	}
    	
    	BufferedDataContainer container = exec.createDataContainer(m_outputSpec);
    	int i = 0;
    	while(input.hasNext()){
    		DataSet n = input.next();
    		INDArray features = n.getFeatureMatrix();
    		INDArray one_hot = n.getLabels();
    		List<DataCell> cells = new ArrayList<DataCell>();
    		
    		cells.add(INDArrayToDoubleVector(features));
    		if(m_labelColumn.getStringValue() != null && !m_labelColumn.getStringValue().isEmpty()){
    			cells.add(INDArrayToDoubleVector(one_hot));
    		}
    		
    		container.addRowToTable(new DefaultRow(new RowKey("Row" + i), cells));
    		i++;
    	}
    	container.close();
    	
    	return new BufferedDataTable[]{container.getTable()};
    }
    
    public static SettingsModelString createLabelColumnSettings(){
    	return new SettingsModelString("label_column", "");
    }
    
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
    	DataTableSpec newSpec = new DataTableSpec();
    	newSpec = TableUtils.appendColumnSpec(newSpec, "dl4j_vector", DoubleVectorCellFactory.TYPE);
    	if(m_labelColumn.getStringValue() != null && !m_labelColumn.getStringValue().isEmpty()){
    		newSpec = TableUtils.appendColumnSpec(newSpec, "one_hot_label", DoubleVectorCellFactory.TYPE);
    	}
    	m_outputSpec = newSpec;
    	return new DataTableSpec[]{newSpec};
    }
   
	@Override
	protected List<SettingsModel> initSettingsModels() {
		m_labelColumn = createLabelColumnSettings();
		List<SettingsModel> settings = new ArrayList<SettingsModel>();
		settings.add(m_labelColumn);
		return settings;
	}
	
	private DataCell INDArrayToDoubleVector(INDArray arr){
		double[] content = new double[arr.length()];
		for(int i = 0; i < arr.length(); i++){
			content[i] = arr.getDouble(i);
		}
		return DoubleVectorCellFactory.createCell(content);
	}
}

