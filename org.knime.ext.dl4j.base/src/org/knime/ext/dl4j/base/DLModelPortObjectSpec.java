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
package org.knime.ext.dl4j.base;


import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.nodes.layer.DNNType;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;
import org.knime.ext.dl4j.base.util.EnumUtils;

/**
 * Port Object Spec for Deep Learning Models.
 * 
 * @author David Kolb, KNIME.com GmbH
 */
public class DLModelPortObjectSpec extends AbstractSimplePortObjectSpec {
	
	public static final class Serializer extends
    	AbstractSimplePortObjectSpecSerializer<DLModelPortObjectSpec> {
	}
	
	private static final String CFGKEY_NEURALNETTYPES =
            "NeuralNetType";
	private static final String CFGKEY_LAYERTYPES =
            "LayerTypeList";
	private static final String CFGKEY_NUMBERINS =
            "NumberOfIns";
	private static final String CFGKEY_NUMBEROUTS =
            "NumberOfOuts";
	private static final String CFGKEY_ISTRAINED =
            "IsTrained";
	private static final String CFGKEY_COLUMNTYPES =
            "FeatureColumsTypes";
	private static final String CFGKEY_COLUMNNAMES =
            "FeatureColumsNames";
	private static final String CFGKEY_LABELS =
            "Labels";
	
	
	private List<DNNType> m_netTypes;
	private List<DNNLayerType> m_layerTypes;
	private List<Pair<Integer, Integer>> m_insOuts;
	private boolean m_isTrained;
	
	private List<Pair<String, String>> m_featureColumns;
	private List<String> m_labels;
	
	/**
     * Empty no-arg constructor as needed by {@link AbstractSimplePortObjectSpec}
     */
	public DLModelPortObjectSpec(){
		
	}
	
	public DLModelPortObjectSpec(final List<DNNType> type, final List<DNNLayerType> layerTypeList, 
			final List<Pair<Integer, Integer>> insOuts, final List<Pair<String, String>> featureColumns, 
			final List<String> labels, final boolean isTrained){
		this.m_netTypes = type;
		this.m_layerTypes = layerTypeList;
		this.m_insOuts = insOuts;
		this.m_isTrained = isTrained;
		this.m_featureColumns = featureColumns;
		this.m_labels = labels;
		
	}
	
	public DLModelPortObjectSpec(final List<DNNType> type, final List<DNNLayerType> layerTypeList, 
			final List<Pair<Integer, Integer>> insOuts, final boolean isTrained){
		this.m_netTypes = type;
		this.m_layerTypes = layerTypeList;
		this.m_insOuts = insOuts;
		this.m_isTrained = isTrained;
		m_featureColumns = new ArrayList<>();
		m_labels = new ArrayList<>();
	}
	
	@Override
	protected void save(final ModelContentWO model) {
		model.addStringArray(CFGKEY_NEURALNETTYPES,
				EnumUtils.getStringListFromEnumCollection(m_netTypes));
		model.addStringArray(CFGKEY_LAYERTYPES, 
				EnumUtils.getStringListFromEnumCollection(m_layerTypes));

		model.addIntArray(CFGKEY_NUMBERINS, getFirstsOfInsOuts());
		model.addIntArray(CFGKEY_NUMBEROUTS, getSecondsOfInsOuts());
		
		model.addBoolean(CFGKEY_ISTRAINED, m_isTrained);
		
		
		model.addStringArray(CFGKEY_COLUMNTYPES,
				DLModelPortObjectUtils.getSeconds(m_featureColumns, String.class));
		model.addStringArray(CFGKEY_COLUMNNAMES,
				DLModelPortObjectUtils.getFirsts(m_featureColumns, String.class));
		
		model.addStringArray(CFGKEY_LABELS,
				m_labels.toArray(new String[m_labels.size()]));
		
	}

	@Override
	protected void load(final ModelContentRO model) throws InvalidSettingsException {
		//load labels
		List<String> labels = new ArrayList<>();
		for(String label : model.getStringArray(CFGKEY_LABELS)){
			labels.add(label);			
		}
		m_labels = labels;
		
		//load learned columns types and names
		String[] columnNames = model.getStringArray(CFGKEY_COLUMNNAMES);
		String[] columnTypes = model.getStringArray(CFGKEY_COLUMNTYPES);
		List<Pair<String, String>> namesAndTypes = new ArrayList<>();
		for(int i = 0; i < columnNames.length; i++){
			namesAndTypes.add(new Pair<String, String>(columnNames[i], columnTypes[i]));
		}
		m_featureColumns = namesAndTypes;
		
		//load network types	
		List<DNNType> dnnTypeList = new ArrayList<>();
		for(String dnnTypeAsString : model.getStringArray(CFGKEY_NEURALNETTYPES)){
			dnnTypeList.add(DNNType.valueOf(dnnTypeAsString));			
		}
		m_netTypes = dnnTypeList;							
		
		//load layer types
		List<DNNLayerType> layerTypeList = new ArrayList<>();
		for(String layerTypeAsString : model.getStringArray(CFGKEY_LAYERTYPES)){
			layerTypeList.add(DNNLayerType.valueOf(layerTypeAsString));			
		}
		m_layerTypes = layerTypeList;
		
		//load insOuts
		int[] lefts = model.getIntArray(CFGKEY_NUMBERINS);
		int[] rights = model.getIntArray(CFGKEY_NUMBEROUTS);
		
		List<Pair<Integer, Integer>> pairs = new ArrayList<>();
		for(int i = 0 ; i < lefts.length ; i ++){
			if(lefts[i] == -1 && rights[i] == -1){
				pairs.add(null);
			} else {
				pairs.add(new Pair<Integer, Integer>(lefts[i], rights[i]));
			}			
		}
		m_insOuts = pairs;
		
		//load isTrained flag
		m_isTrained = model.getBoolean(CFGKEY_ISTRAINED);
	}
	
	public List<DNNType> getNeuralNetworkTypes(){
		return m_netTypes;
	}

	public List<DNNLayerType> getLayerTypes() {
		return m_layerTypes;
	}
	
	public List<Pair<Integer,Integer>> getInsOuts(){
		return m_insOuts;
	}
	
	public boolean isTrained(){
		return m_isTrained;
	}
	
	public List<Pair<String, String>> getLearnedColumns() {
		return m_featureColumns;
	}
	
	public List<String> getLabels(){
		return m_labels;
	}
	
	/**
	 * Helper method to convert left side of 
	 * pair array to single array. If a pair should be null
	 * the value -1 is added instead to mark the null value
	 * for load method.
	 * 
	 * @return left side of pair as int array
	 */
	private int[] getFirstsOfInsOuts(){
		int[] firsts = new int[m_insOuts.size()];		
		for(int i = 0 ; i < m_insOuts.size() ; i++){
			if(m_insOuts.get(i) != null){
				firsts[i] = m_insOuts.get(i).getFirst();
			} else {
				firsts[i] = -1;
			}
		}		
		return firsts;
	}
	
	/**
	 * Helper method to convert right side of 
	 * pair array to single array. If a pair should be null
	 * the value -1 is added instead to mark the null value
	 * for load method.
	 * 
	 * @return right side of pair as int array
	 */
	private int[] getSecondsOfInsOuts(){
		int[] seconds = new int[m_insOuts.size()];		
		for(int i = 0 ; i < m_insOuts.size() ; i++){
			if(m_insOuts.get(i) != null){
				seconds[i] = m_insOuts.get(i).getSecond();
			} else {
				seconds[i] = -1;
			}
		}		
		return seconds;
	}
}
