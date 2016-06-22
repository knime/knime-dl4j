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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deeplearning4j.nn.conf.layers.FeedForwardLayer;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.LocalResponseNormalization;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.nodes.layer.DNNType;

/**
 * Utility class to validate deep neural network
 * configurations and {@link DLModelPortObjectSpec}.
 * 
 * @author David Kolb, KNIME.com GmbH
 */
public class ConfigurationUtils {

	private ConfigurationUtils(){
		// Utility class
	}
	
	/**
	 * Checks if the last layer of the network is of specified {@link DNNLayerType}.
	 * 
	 * @param layers {@link DNNLayerType}s contained in the network
	 * @param expectedType the {@link DNNLayerType} the last layer should be
	 * @throws InvalidSettingsException if last layer is not of expected type
	 */
	public static void checkLastLayer(final List<DNNLayerType> layers, DNNLayerType expectedType) 
			throws InvalidSettingsException{
		if(layers.size() < 1){
			return;
		}
		DNNLayerType lastLayer = layers.get(layers.size()-1);
		if(!lastLayer.equals(expectedType)){
			throw new InvalidSettingsException("Type of last layer in newtwork: "
					+ lastLayer.toString() + ". Needs to be: " + expectedType);	
		}
	}
	
	/**
	 * Validates the given specification. Checks if the given {@link DNNType}s
	 * are compatible with the spec. Checks if the layer types contained in
	 * the spec are compatible with each other. Checks if the number of outputs 
	 * of each layer is the same as the number of inputs of the next layer. If no
	 * exception is thrown the specification seems to be valid, however there
	 * may be warning messages.
	 * 
	 * @param spec the spec which we want to validate
	 * @param types the {@link DNNType} of the current layer
	 * @return warning messages giving possible problems of spec, returns empty
	 * list if no problems were discovered
	 */
	public static List<String> validateSpec(final DLModelPortObjectSpec spec,
			final List<DNNType> types) throws InvalidSettingsException {						
		List<String> warnings = new ArrayList<>();
		
		warnings.addAll(validateType(spec, types));
		warnings.addAll(validateOutputLayerPosition(spec));
		
		return warnings;
	}
	
	/**
	 * Checks if the specified column selection is present in the specified {@link DataTableSpec}
	 * and if the selection is empty.
	 * 
	 * @param tableSpec the spec of the table
	 * @param columnSelection the selected column/s
	 * @throws InvalidSettingsException if no columns are selected
	 * 									if selected columns are not available in the table
	 */
	public static void validateColumnSelection(DataTableSpec tableSpec, SettingsModelFilterString columnSelection)
			throws InvalidSettingsException{
		List<String> selectedColumns = columnSelection.getIncludeList();
		for(String columnName : selectedColumns){
			validateColumnSelection(tableSpec, columnName);
		}
	}
	
	/**
	 * Checks if the specified column selection is present in the specified {@link DataTableSpec}
	 * and if the selection is empty.
	 * 
	 * @param tableSpec the spec of the table
	 * @param columnSelection the selected column
	 * @throws InvalidSettingsException if no columns are selected
	 * 									if selected columns are not available in the table
	 */
	public static void validateColumnSelection(DataTableSpec tableSpec, SettingsModelString columnSelection)
			throws InvalidSettingsException{		
		String selectedColumn = columnSelection.getStringValue();	
		validateColumnSelection(tableSpec, selectedColumn);	
	}
	
	/**
	 * Checks if the specified column selection is present in the specified {@link DataTableSpec}
	 * and if the selection is empty.
	 * 
	 * @param tableSpec the spec of the table
	 * @param columnSelection the selected column
	 * @throws InvalidSettingsException if no columns are selected
	 * 									if selected columns are not available in the table
	 */
	public static void validateColumnSelection(DataTableSpec tableSpec, String columnSelection)
			throws InvalidSettingsException{	
		if(columnSelection.isEmpty()){
			throw new InvalidSettingsException("No input columns selected");
		}
		if(!tableSpec.containsName(columnSelection)){
			throw new InvalidSettingsException("Input column not available in table");
		} 				
	}
	
	/**
	 * Checks if the specified column selection is present in the specified {@link DataTableSpec}
	 * and if the selection is empty.
	 * 
	 * @param tableSpec the spec of the table
	 * @param columnSelection the selected column/s
	 * @throws InvalidSettingsException if no columns are selected
	 * 									if selected columns are not available in the table
	 */
	public static void validateColumnSelection(DataTableSpec tableSpec, String[] columnSelection)
			throws InvalidSettingsException{	
		for(String columnName : columnSelection){
			validateColumnSelection(tableSpec, columnName);				
		}
	}
	
	/**
	 * Checks if there are more than one {@link DNNLayerType.OUTPUT_LAYER}s in 
	 * the net and if the output layer{@link DNNLayerType.OUTPUT_LAYER} is the 
	 * last layer in the net.
	 * 
	 * @param spec
	 * @return list of warnings
	 */
	private static List<String> validateOutputLayerPosition(final DLModelPortObjectSpec spec){
		List<String> warnings = new ArrayList<>();
		
		List<DNNLayerType> layerTypes = spec.getLayerTypes();
		int numberOfOutputLayers = 0;
		int indexOfLastOutputLayer = 0;
		for(int i = 0 ; i < layerTypes.size() ; i++){
			if(layerTypes.get(i).equals(DNNLayerType.OUTPUT_LAYER) || layerTypes.get(i).equals(DNNLayerType.RNN_OUTPUT_LAYER)){
				numberOfOutputLayers++;
				indexOfLastOutputLayer = i+1;
			}
		}
		if(numberOfOutputLayers > 1){
			warnings.add("There are more than one Output Layers in the network. "
					+ "This may be a problem. NUMBER OF OUTPUT LAYERS: " +
					numberOfOutputLayers);
		}
		if(indexOfLastOutputLayer != layerTypes.size() && numberOfOutputLayers != 0){
			warnings.add("The Output Layer is not the last layer in the network. This may "
					+ "be a problem. POSITION OF LAST OUTPUT LAYER: " + indexOfLastOutputLayer
					+ ", NUMBER OF LAYERS IN NETWORK: " + layerTypes.size());
		}
		
		return warnings;
	}
	

	/**
	 * Checks if the {@link DNNType}s contained in the spec are compatible with the 
	 * {@link DNNType}s of this node.
	 * 
	 * @param spec
	 * @param types of this node
	 * @return list of warnings
	 */
	private static List<String> validateType (final DLModelPortObjectSpec spec, 
			final List<DNNType> types){
		List<String> warnings = new ArrayList<>();
		List<DNNType> intersectOfTypes = new ArrayList<>(spec.getNeuralNetworkTypes());
		
		//calc intersection between types in spec and types of this node
		intersectOfTypes.retainAll(types);
		if(intersectOfTypes.isEmpty() && !spec.getNeuralNetworkTypes().contains(DNNType.EMPTY)){
			warnings.add(typesToString(types) + " may be incompatible with the "
    				+ "current network architecture. "
    				+ "this architecture: " + typesToString(types)
    				+ " current architecture: " + typesToString(spec.getNeuralNetworkTypes()));
		}
		return warnings;
	}	
	
	/**
	 * Converts list of enums to single string where every string representation
	 * of the enum is separated by a "OR" in the returned string. 
	 * 
	 * @param types
	 * @return
	 */
	private static <E extends Enum<E>> String typesToString(List<E> types){
		String typesToString = "";
		for(int i = 0 ; i < types.size() ; i++){
			typesToString += types.get(i).toString();
			if(i+1 != types.size()){
				typesToString += " or ";
			}
		}		
		return typesToString;
	}
	
	public static List<Pair<String,String>> createNameTypeListOfSelectedCols(List<String> featureColumns, DataTableSpec tableSpec) 
			throws InvalidSettingsException{
		List<Pair<String,String>> inputs = new ArrayList<>();
		for(String colName : featureColumns){
			DataColumnSpec colSpec = tableSpec.getColumnSpec(colName);
			String type = colSpec.getType().getName();			
			inputs.add(new Pair<String, String>(colName,type));		
		}
		return inputs;
    }
	
	public static boolean containsImg(DataTableSpec spec){		
    	Iterator<DataColumnSpec> colSpecs = spec.iterator();
    	while(colSpecs.hasNext()){
    		DataColumnSpec colSpec = colSpecs.next();   		
    		if(colSpec.getType().getName().contains("Image")){
        		return true;
        	} 
    	}
    	return false;
	}
	
	/**
	 * Sets up the input/output numbers of the specified list of layers. The number of 
	 * inputs of the first layer will be set to the specified value. Usually this value
	 * can be calculated from the data. The numbers for possible more layers will adjusted
	 * such that the number of outputs from one layer matches the number of inputs from the next.
	 * It is expected that the number of outputs is correctly set for each layer so the number 
	 * of inputs can be inferred.<br><br>
	 * 
	 * SubsamplingLayer and LocalResponseNormalization will be left untouched as they are no 
	 * FeedForwardLayer (strange layer hierarchy from DL4J, e.g. recurrent layers extend 
	 * {@link FeedForwardLayer}. May cause problems with next DL4J versions).
	 * 
	 * @param layers the list of layers to set up
	 * @param numberOfInputs the number of inputs for the first layer
	 */
	public static void setupLayers(final List<Layer> layers, final int numberOfInputs){
		FeedForwardLayer ffl = null;		
		if(layers.isEmpty()) return;
		//need to cast to FeedForwardLayer to access set/get methods of input/output numbers
		else ffl = (FeedForwardLayer)layers.get(0);
		//set the number of inputs to the inferred number of inputs from the data for the 
		//first layer
		ffl.setNIn(numberOfInputs);
		//get user specified number of outputs from first layer
		int previousOutNum = ffl.getNOut();		
		//start from second layer
		for(int i = 1; i < layers.size(); i++){
			//SubsamplingLayer and LocalResponseNormalization are not FeedForwardLayer so skip
			if(layers.get(i) instanceof SubsamplingLayer || layers.get(i) instanceof LocalResponseNormalization){
				continue;
			}
			ffl = (FeedForwardLayer)layers.get(i);
			//set number of inputs to number of outputs of previous layer
			ffl.setNIn(previousOutNum);
			//save number of outputs of current layer
			previousOutNum = ffl.getNOut();
		}
	}
}
