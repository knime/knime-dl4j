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
package org.knime.ext.dl4j.base.nodes.predict;


import java.util.List;

import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;

/**
 * Abstract superclass for predictor node models of Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public abstract class AbstractDLPredictorNodeModel extends AbstractDLNodeModel {

	private boolean m_inputTableContainsImg;
	private boolean m_containsLabels;

	
	protected AbstractDLPredictorNodeModel(final PortType[] inPortTypes,
            final PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
    }	

	/**
	 * Make basic checks before a predictor can be executed. Check if the feature columns that were
	 * used for learning are present in the table and if they have the same type. Sets predictor flags.
	 * 
	 * @param inSpecs the specs of the model to use for prediction (index 0) and the specs of the table 
	 * 				  to get data for prediction (index 1)
	 * @param logger a logger to log errors
	 * @return
	 * @throws InvalidSettingsException
	 */
	protected DataTableSpec[] configure(PortObjectSpec[] inSpecs, final NodeLogger logger)
			throws InvalidSettingsException {
		DLModelPortObjectSpec modelSpec = (DLModelPortObjectSpec)inSpecs[0];
		DataTableSpec predictTableSpec = (DataTableSpec)inSpecs[1];

		if(!modelSpec.isTrained()){
			throw new InvalidSettingsException("Model not yet trained. Can't predict with untrained model.");
		}
		
		m_inputTableContainsImg = ConfigurationUtils.containsImg(predictTableSpec);
		
		if(modelSpec.getLabels() == null || modelSpec.getLabels().isEmpty()){
			m_containsLabels = false;
		} else {
			m_containsLabels = true;
		}
		
		checkInputTableForFeatureColumns(predictTableSpec, modelSpec.getLearnedColumns());		
		
		//check for spec sanity
    	logWarnings(logger, ConfigurationUtils.validateSpec(modelSpec, modelSpec.getNeuralNetworkTypes()));
    	
		return new DataTableSpec[]{predictTableSpec};
	}			
	
	protected boolean inputTableContainsImg() {
		return m_inputTableContainsImg;
	}
	
	protected boolean containsLabels(){
		return m_containsLabels;
	}
	
	/**
	 * Checks if the specified list of feature columns is contained in the specified spec.
	 * 
	 * @param spec the table spec to check for feature columns
	 * @param expectedCols the feature columns 
	 * @throws InvalidSettingsException if spec does not contain a column of the feature columns
	 * 									if it contains the columns but one of it is not of the same type
	 */
	private void checkInputTableForFeatureColumns(DataTableSpec spec, List<Pair<String,String>> expectedCols) 
			throws InvalidSettingsException {
		for(Pair<String,String> c : expectedCols){
			if(spec.containsName(c.getFirst())){
				DataColumnSpec colSpec = spec.getColumnSpec(c.getFirst());
				if(!colSpec.getType().getName().equals(c.getSecond())){
					throw new InvalidSettingsException("Table contains column: " + c.getFirst() + " but was "
							+ "not of expected type. Expected: " + c.getSecond() + " but was: "
							+ colSpec.getType());
				}
			} else {
				throw new InvalidSettingsException("Table does not contain expected input column: " + c.getFirst());
			}
		}
	}
	
	/**
	 * Checks if the last layer of the supplied list of layers has softmax activation function.
	 * 
	 * @param layers the list of layers to check
	 * @return true if activation of the last layer is softmax, false if not
	 */
	protected boolean isOutActivationSoftmax(List<Layer> layers){
    	Layer outputLayer = layers.get(layers.size()-1);
    	if(outputLayer.getActivationFunction().equals("softmax")){
    		return true;
    	}
    	return false;
	}
	
	/**
	 * Determines the number of outputs of a {@link MultiLayerNetwork}, hence the
	 * number of outputs of the last layer which must be a {@link OutputLayer}.
	 *  
	 * @param mln the network to use
	 * @return number of outputs of a network
	 */
	protected int getNumberOfOutputs(MultiLayerNetwork mln) {
		int numberOfLayers = mln.getLayerWiseConfigurations().getConfs().size();
		
		Layer l = mln.getLayerWiseConfigurations().getConf(numberOfLayers-1).getLayer();
		if(l instanceof OutputLayer){
			return ((OutputLayer)l).getNOut();
		} else {
			new InvalidSettingsException("Last layer is not a Output Layer");
		}
		return 0;
	}
}
