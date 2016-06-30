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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.nodes.layer.DNNType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Utility class for {@link DLModelPortObject} and {@link DLModelPortObjectSpec}
 * Serialisation. Also contains utility methods for members of port and spec class. 
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class DLModelPortObjectUtils {
	
	private DLModelPortObjectUtils() {
		// Utility class
	}
	
	/**
	 * Writes the specified {@link DLModelPortObject} and the {@link DLModelPortObjectSpec} contained 
	 * within to the specified {@link ZipOutputStream}.
	 * 
	 * @param portObject the port object to write
	 * @param writePortObject whether to write the port object
	 * @param writeSpec whether to write the spec
	 * @param outStream the stream to write to
	 * @throws IOException
	 */
	public static void saveModelToZip(DLModelPortObject portObject, boolean writePortObject, boolean writeSpec, ZipOutputStream outStream)
			throws IOException{
		DLModelPortObjectSpec spec = (DLModelPortObjectSpec)portObject.getSpec();
		
		if(outStream == null){
			throw new IOException("OutputStream is null");
		}
		if(writePortObject && !writeSpec){
			savePortObjectOnly(portObject, outStream);
		}
		if(!writePortObject && writeSpec){
			saveSpecOnly(spec, outStream);
		}
		if(writePortObject && writeSpec){
			savePortObjectAndSpec(portObject, spec, outStream);
		}
	}
	
	/**
	 * Loads a {@link DLModelPortObjectSpec} from the specified {@link ZipInputStream}.
	 * 
	 * @param inStream the stream to load from
	 * @return the loaded {@link DLModelPortObjectSpec}
	 * @throws IOException
	 */
	public static DLModelPortObjectSpec loadSpecFromZip(ZipInputStream inStream) throws IOException{						
		List<DNNLayerType> layerTypes = new ArrayList<>();
		List<DNNType> networkTypes = new ArrayList<>();
		boolean isTrained = false;
		List<Pair<String,String>> learnedColumnTypes = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		
		ZipEntry entry;
		
		 while ((entry = inStream.getNextEntry())!= null) {
			 	if(entry.getName().matches("isTrained")){//read flag
		    		Integer read = inStream.read();
		    		if( read == 1 ){
		    			isTrained = true;
		    		} else {
		    			isTrained = false;
		    		}		    				    	
		    	} else if (entry.getName().matches("layer_type[0123456789]+")){//read layer type
		    		String read = readStringFromZipStream(inStream);
		    		layerTypes.add(DNNLayerType.valueOf(read));
		    		
		    	} else if (entry.getName().matches("dnn_type[0123456789]+")){//read dnn type
		    		String read = readStringFromZipStream(inStream);
		    		networkTypes.add(DNNType.valueOf(read));		    	
		    	} else if (entry.getName().matches("input_column[0123456789]+")){//read input type
		    		String read = readStringFromZipStream(inStream);
		    		String columnName = read.split(",")[0];
		    		String columnType = (read.split(",")[1]);
		    		
		    		learnedColumnTypes.add(new Pair<String,String>(columnName,columnType));
		    	
		    	} else if (entry.getName().matches("label[0123456789]+")){//read label
		    		String read = readStringFromZipStream(inStream);
		    		labels.add(read);
		    		  		
		    	} else {
		    		// ignore unrecognised ZipEntries
		    	}
		 }
		 
		 DLModelPortObjectSpec spec = new DLModelPortObjectSpec(networkTypes, layerTypes, 
				 learnedColumnTypes, labels, isTrained);
		 return spec;	
	}
	
	/**
	 * Loads a {@link DLModelPortObject} from the specified {@link ZipInputStream}.
	 * 
	 * @param inStream inStream the stream to load from
	 * @return the loaded {@link DLModelPortObject}
	 * @throws IOException
	 */
	public static DLModelPortObject loadPortFromZip(ZipInputStream inStream) throws IOException{
		List<Layer> layers = new ArrayList<>();
		INDArray mln_params = null;
		MultiLayerConfiguration mln_config = null;
		org.deeplearning4j.nn.api.Updater updater = null;
		
	    ZipEntry entry;

	    while ((entry = inStream.getNextEntry())!= null) {
	    	if(entry.getName().matches("layer[0123456789]+")){//read layers
	    		String read = readStringFromZipStream(inStream);
	    		layers.add(NeuralNetConfiguration.fromJson(read).getLayer());	 	    
	    	} else if (entry.getName().matches("mln_config")){//read MultilayerNetwork
	    		String read = readStringFromZipStream(inStream);
	    		mln_config = MultiLayerConfiguration.fromJson(read.toString());		    		
	    	} else if (entry.getName().matches("mln_params")){
	    		mln_params = Nd4j.read(inStream);
	    	} else if (entry.getName().matches("mln_updater")){    		
	    		ObjectInputStream ois = new ObjectInputStream(inStream);
	    		try {
					updater = (org.deeplearning4j.nn.api.Updater)ois.readObject();
				} catch (ClassNotFoundException e) {
					throw new IOException("Problem with updater loading: " + e.getMessage());					
				}	    	
	    	}
	    }
	    
	    MultiLayerNetwork mln;
	    
	    if(mln_config != null){
	    	mln = new MultiLayerNetwork(mln_config);
	    	mln.init();
	    	if(updater != null){
	    		mln.setUpdater(updater);
	    	}
	    	
	    	if (mln_params != null){
		    	mln.setParams(mln_params);
		    } 	
	    } else {
	    	mln = null;
	    }

	    DLModelPortObject newPortObject = new DLModelPortObject(layers, mln, null);
	    
	    return newPortObject;
	}
	
	private static void savePortObjectOnly(DLModelPortObject portObject, ZipOutputStream out)
			throws IOException{
		List<Layer> layers = portObject.getLayers();
		MultiLayerNetwork mln = portObject.getMultilayerLayerNetwork();
	
		writeLayers(layers, out);
		writeMultiLayerNetwork(mln, out);		
	}
	
	private static void saveSpecOnly(DLModelPortObjectSpec spec, ZipOutputStream out)
			throws IOException{
		List<DNNLayerType> layerTypes = spec.getLayerTypes();
		List<DNNType> networkTypes = spec.getNeuralNetworkTypes();
		boolean isTrained = spec.isTrained();
		List<Pair<String,String>> learnedColumnTypes = spec.getLearnedColumns();
		List<String> labels = spec.getLabels();
		
		writeLayerTypes(layerTypes, out);
		writeDNNTypes(networkTypes, out);
		writeIsTrained(isTrained, out);
		writeLearnedColumns(learnedColumnTypes, out);
		writeLabels(labels, out);
	}
	
	private static void savePortObjectAndSpec(DLModelPortObject portObject, DLModelPortObjectSpec spec, ZipOutputStream out)
			throws IOException{
		savePortObjectOnly(portObject, out);	
		saveSpecOnly(spec,out);	
	}
	
	
	private static void writeLayers(List<Layer> layers, ZipOutputStream out) throws IOException{
		List<String> allLayersAsJson = convertLayersToJSONs(layers);
		ZipEntry entry;
		
		for(int i = 0 ; i < allLayersAsJson.size() ; i++){
			entry = new ZipEntry("layer" + i);
			out.putNextEntry(entry);
			out.write(allLayersAsJson.get(i).getBytes(Charset.forName("UTF-8")));
		}
	}
	
	private static void writeIsTrained(boolean isTrained, ZipOutputStream out) throws IOException{
		ZipEntry entry;
		entry = new ZipEntry("isTrained");
		out.putNextEntry(entry);
		if(isTrained){
			out.write(new Integer(1));
		} else {
			out.write(new Integer(0));
		}
	}
	
	private static void writeMultiLayerNetwork(MultiLayerNetwork mln, ZipOutputStream out) throws IOException{
		ZipEntry entry;
		if(mln != null){
			//write MultilayerNetwork, consists of configuration and network parameters
			entry = new ZipEntry("mln_config");
			out.putNextEntry(entry);
			out.write(mln.getLayerWiseConfigurations().toJson()
					.getBytes(Charset.forName("UTF-8")));
									
			try {
				//params() throws exception if not yet set
				INDArray params = mln.params();
				entry = new ZipEntry("mln_params");
				out.putNextEntry(entry);
				Nd4j.write(out, params);
			} catch (Exception e) {
				//net does not contain params so we just write nothing
			}
			
			//write updater
			try {
				//if no backprop is done getUpdater() will throw an exception
				if(mln.getUpdater() != null){
					entry = new ZipEntry("mln_updater");
					out.putNextEntry(entry);
					ObjectOutputStream oos = new ObjectOutputStream(out);				
					oos.writeObject(mln.getUpdater());
				}
			} catch (Exception e) {
				//net does not contain updater because no backprop was done
			}
		}
	}
	
	private static void writeLayerTypes(List<DNNLayerType> layerTypes, ZipOutputStream out) throws IOException{
		String[] layerTypesString = EnumUtils.getStringListFromEnumCollection(layerTypes);
		ZipEntry entry;
		
		for(int i = 0 ; i < layerTypesString.length ; i++){
			entry = new ZipEntry("layer_type" + i);
			out.putNextEntry(entry);
			out.write(layerTypesString[i].getBytes(Charset.forName("UTF-8")));
		}
		
	}
	
	private static void writeDNNTypes(List<DNNType> dnnTypes, ZipOutputStream out) throws IOException{
		String[] layerTypesStrings = EnumUtils.getStringListFromEnumCollection(dnnTypes);
		ZipEntry entry;
		
		for(int i = 0 ; i < layerTypesStrings.length ; i++){
			entry = new ZipEntry("dnn_type" + i);
			out.putNextEntry(entry);
			out.write(layerTypesStrings[i].getBytes(Charset.forName("UTF-8")));
		}
	}
	
	private static void writeLearnedColumns(List<Pair<String,String>> learnedColumnTypes, ZipOutputStream out) throws IOException{
		String[] inputTypesStrings = getSeconds(learnedColumnTypes, String.class);
		String[] columnNames = getFirsts(learnedColumnTypes, String.class);
		ZipEntry entry;
		
		for(int i = 0 ; i < inputTypesStrings.length ; i++){
			entry = new ZipEntry("input_column" + i);
			out.putNextEntry(entry);
			String stringToWrite = columnNames[i] + "," + inputTypesStrings[i];
			out.write(stringToWrite.getBytes(Charset.forName("UTF-8")));
		}
	}
	
	private static void writeLabels(List<String> labels, ZipOutputStream out) throws IOException{
		ZipEntry entry;
		
		int i = 0;
		for(String label : labels){
			entry = new ZipEntry("label" + i);
			out.putNextEntry(entry);
			out.write(label.getBytes(Charset.forName("UTF-8")));
			i++;
		}
	}
	
	/**
	 * Converts layers to list of Strings in json format.
	 * 
	 * @return list of Strings in json format of converted layers
	 */
	public static List<String> convertLayersToJSONs(List<Layer> layers){
		List<String> allLayersAsJson = new ArrayList<>();		
		NeuralNetConfiguration config = new NeuralNetConfiguration();
		for(Layer layer : layers){
			config.setLayer(layer);
			allLayersAsJson.add(config.toJson());
		}
		return allLayersAsJson;
	}
	
	/**
	 * Read a String from {@link ZipInputStream} until the stream is finished.
	 * 
	 * @param in the stream to read from
	 * @return the read String
	 * @throws IOException
	 */
	private static String readStringFromZipStream(ZipInputStream in) throws IOException{
		StringBuilder stringBuilder = new StringBuilder();
	    byte[] byteBuffer = new byte[1024];
	    int currentRead = 0;
	    
	    while ((currentRead = in.read(byteBuffer, 0, 1024)) >= 0) {
            stringBuilder.append(new String(byteBuffer, 0, currentRead));
	    }
	    
	    return stringBuilder.toString();
	}
	
	/**
	 * Returns an array of all first elements of the specified list of
	 * pairs. Returned array will be of specified class. The class is needed
	 * in order to determine the class of the array to return when there are 
	 * no pairs in the list so we can't get the class during runtime.
	 * 
	 * @param pairs the pairs to get first elements from
	 * @param c the class of first elements
	 * @return array of all first elements of pairs contained in the list
	 */
	@SuppressWarnings("unchecked")
	public static <E,V> E[] getFirsts(List<Pair<E,V>> pairs, Class<E> c){				
		return (E[])pairs.stream().map(f -> f.getFirst()).collect(Collectors.toList())
				.toArray((E[])Array.newInstance(c, pairs.size()));
	}
	
	/**
	 * Returns an array of all second elements of the specified list of
	 * pairs. Returned array will be of specified class. The class is needed
	 * in order to determine the class of the array to return when there are 
	 * no pairs in the list so we can't get the class during runtime.
	 * 
	 * @param pairs the pairs to get second elements from
	 * @param c the class of second elements
	 * @return array of all second elements of pairs contained in the list
	 */
	@SuppressWarnings("unchecked")
	public static <E,V> V[] getSeconds(List<Pair<E,V>> pairs, Class<V> c){		
		return (V[])pairs.stream().map(f -> f.getSecond()).collect(Collectors.toList())
				.toArray((V[])Array.newInstance(c, pairs.size()));
	}
}
