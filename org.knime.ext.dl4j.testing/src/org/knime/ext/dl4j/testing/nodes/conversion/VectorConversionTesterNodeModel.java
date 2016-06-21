package org.knime.ext.dl4j.testing.nodes.conversion;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.data.iter.BufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

/**
 * This is the model implementation of VectorConversionTester.
 * 
 *
 * @author KNIME
 */
public class VectorConversionTesterNodeModel extends AbstractDLNodeModel {

	// the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(VectorConversionTesterNodeModel.class);
	
	private SettingsModelString m_mnistColumn;
	private SettingsModelBoolean m_expectBinary;
	
	/**
     * Constructor for the node model.
     */
    protected VectorConversionTesterNodeModel() {   
    	super(new PortType[] {BufferedDataTable.TYPE}, new PortType[] {}); 
    }
	
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
    	
    	BufferedDataTable table = inData[0];
    	DataTableSpec spec = table.getDataTableSpec();
    	
    	String mnistLabelColumnName = m_mnistColumn.getStringValue();
    	if(mnistLabelColumnName.isEmpty()){
    		throw new InvalidSettingsException("Need to specify MNIST label column");
    	}
    	
    	boolean expectBinary= m_expectBinary.getBooleanValue();
    	long tableSize = table.size();
    	
    	List<String> labels = ConverterUtils.ListOfNominalValuesToListOfStrings(spec.getColumnSpec(mnistLabelColumnName)
    			.getDomain().getValues());
    	
    	MnistDataSetIterator mnistIter = new MnistDataSetIterator(1, (int)tableSize, expectBinary, true, false, 0);
    	BufferedDataTableDataSetIterator tableIter = new BufferedDataTableDataSetIterator(table, mnistLabelColumnName, 1, labels, true);		
    
    	DataSet mnistNext = null;
    	DataSet tableNext = null;
    	String mnistLabel = "";
    	String tableLabel = "";
    	
    	for(int i = 0; i < tableSize; i++){
    		mnistNext = mnistIter.next();
    		tableNext = tableIter.next();
    		if(!mnistNext.getFeatureMatrix().equals(tableNext.getFeatureMatrix())){
        		throw new Exception("Feature Matrix of row: " + i + " not equal with refernce");
        	}
    		
    		mnistLabel = createMnistLabelFromINDArray(mnistNext.getLabels());
    		tableLabel = softmaxActivationToLabel(labels, tableNext.getLabels());
    		
    		if(!mnistLabel.equals(tableLabel)){
    			throw new Exception("Label of row: " + i + " not equal with refernce");
    		}
    		
    	}   	
    	return new BufferedDataTable[]{};
    }
    
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
    	if(m_mnistColumn.getStringValue().isEmpty()){
    		logger.warn("No MNIST column selected");
    	}
    	return new DataTableSpec[]{};
    }   
    
	@Override
	protected List<SettingsModel> initSettingsModels() {
		List<SettingsModel> settings = new ArrayList<>();
		m_mnistColumn = createMnistColumnSelectionModel();
		m_expectBinary = createExpectBinaryImagesModel();
		settings.add(m_mnistColumn);
		settings.add(m_expectBinary);
		return settings;
	}
	
	public static SettingsModelString createMnistColumnSelectionModel(){
		return new SettingsModelString("mnist_column", "");
	}
	
	public static SettingsModelBoolean createExpectBinaryImagesModel(){
		return new SettingsModelBoolean("expect_binary", false);
	}
    
	private String softmaxActivationToLabel(List<String> labels, INDArray softmaxActivation) throws Exception{
		if(labels.size() != softmaxActivation.length()){
			throw new Exception("The number of labels: " + labels.size() + "does not match the length of the softmaxActivation "
					+ "vector: " + softmaxActivation.length());
		}
		List<Double> classProbabilities = new ArrayList<>();
		for(int i = 0; i < softmaxActivation.length(); i++){
			classProbabilities.add(softmaxActivation.getDouble(i));
		}
		double max = softmaxActivation.max(1).getDouble(0);
		int indexOfMax = classProbabilities.indexOf(max);

		return labels.get(indexOfMax);
	}
	
	private String createMnistLabelFromINDArray(INDArray labelVector){
		float label = 0;
		for(int i = 0; i < labelVector.length(); i++){
			if(labelVector.getFloat(i) == 1.0){
				label = i;
				break;
			}
		}
		return  (int)label + "";
	}
   
}

