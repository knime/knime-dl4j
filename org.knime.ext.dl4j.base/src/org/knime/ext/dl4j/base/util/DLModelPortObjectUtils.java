/*
 * ------------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 */
package org.knime.ext.dl4j.base.util;

import java.io.DataOutputStream;
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
import org.knime.core.node.NodeLogger;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.nodes.layer.DNNType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Utility class for {@link DLModelPortObject} and {@link DLModelPortObjectSpec} Serialisation. Also contains utility
 * methods for members of port and spec class.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class DLModelPortObjectUtils {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DLModelPortObjectUtils.class);

    private DLModelPortObjectUtils() {
        // Utility class
    }

    /**
     * Writes the specified {@link DLModelPortObject} and the {@link DLModelPortObjectSpec} contained within to the
     * specified {@link ZipOutputStream}.
     *
     * @param portObject the port object to write
     * @param writePortObject whether to write the port object
     * @param writeSpec whether to write the spec
     * @param outStream the stream to write to
     * @throws IOException
     */
    public static void saveModelToZip(final DLModelPortObject portObject, final boolean writePortObject,
        final boolean writeSpec, final ZipOutputStream outStream) throws IOException {
        final DLModelPortObjectSpec spec = (DLModelPortObjectSpec)portObject.getSpec();

        if (outStream == null) {
            throw new IOException("OutputStream is null");
        }
        if (writePortObject && !writeSpec) {
            savePortObjectOnly(portObject, outStream);
        }
        if (!writePortObject && writeSpec) {
            saveSpecOnly(spec, outStream);
        }
        if (writePortObject && writeSpec) {
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
    public static DLModelPortObjectSpec loadSpecFromZip(final ZipInputStream inStream) throws IOException {
        final List<DNNLayerType> layerTypes = new ArrayList<>();
        final List<DNNType> networkTypes = new ArrayList<>();
        boolean isTrained = false;
        final List<Pair<String, String>> learnedColumnTypes = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        final List<String> targetColumnNames = new ArrayList<String>();
        String learnerType = "";

        ZipEntry entry;

        while ((entry = inStream.getNextEntry()) != null) {
            if (entry.getName().matches("isTrained")) { //read flag
                final Integer read = inStream.read();
                if (read == 1) {
                    isTrained = true;
                } else {
                    isTrained = false;
                }
            } else if (entry.getName().matches("layer_type[0123456789]+")) { //read layer type
                final String read = readStringFromZipStream(inStream);
                layerTypes.add(DNNLayerType.valueOf(read));

            } else if (entry.getName().matches("dnn_type[0123456789]+")) { //read dnn type
                final String read = readStringFromZipStream(inStream);
                networkTypes.add(DNNType.valueOf(read));
            } else if (entry.getName().matches("input_column[0123456789]+")) { //read input type
                final String read = readStringFromZipStream(inStream);
                final String columnName = read.split(",")[0];
                final String columnType = (read.split(",")[1]);

                learnedColumnTypes.add(new Pair<String, String>(columnName, columnType));

            } else if (entry.getName().matches("label[0123456789]+")) { //read label
                final String read = readStringFromZipStream(inStream);
                labels.add(read);
            } else if (entry.getName().matches("target_column[0123456789]+")) { //read target column name
                final String read = readStringFromZipStream(inStream);
                targetColumnNames.add(read);
            } else if (entry.getName().matches("learner_type")) { //read learner type
                final String read = readStringFromZipStream(inStream);
                learnerType = read;
            } else {
                // ignore unrecognised ZipEntries
                LOGGER.debug("Skipping unrecognised ZipEntry: " + entry.getName());
            }
        }
        return new DLModelPortObjectSpec(networkTypes, layerTypes, learnedColumnTypes, labels, targetColumnNames,
            learnerType, isTrained);
    }

    /**
     * Loads a {@link DLModelPortObject} from the specified {@link ZipInputStream}.
     *
     * @param inStream inStream the stream to load from
     * @return the loaded {@link DLModelPortObject}
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static DLModelPortObject loadPortFromZip(final ZipInputStream inStream) throws IOException {
        final List<Layer> layers = new ArrayList<>();
        INDArray mln_params = null;
        MultiLayerConfiguration mln_config = null;
        org.deeplearning4j.nn.api.Updater updater = null;

        ZipEntry entry;

        while ((entry = inStream.getNextEntry()) != null) {
            if (entry.getName().matches("layer[0123456789]+")) { //read layers
                final String read = readStringFromZipStream(inStream);
                layers.add(NeuralNetConfiguration.fromJson(read).getLayer());
            } else if (entry.getName().matches("mln_config")) { //read MultilayerNetwork
                final String read = readStringFromZipStream(inStream);
                mln_config = MultiLayerConfiguration.fromJson(read.toString());
            } else if (entry.getName().matches("mln_params")) { //read params
                try {
                    mln_params = Nd4j.read(inStream);
                } catch (Exception e) {
                    throw new IOException("Could not load network parameters. Please re-execute the Node.", e);
                }
            } else if (entry.getName().matches("mln_updater")) { //read updater
                // stream must not be closed, even if an exception is thrown, because the wrapped stream must stay open
                final ObjectInputStream ois = new ObjectInputStream(inStream);
                try {
                    updater = (org.deeplearning4j.nn.api.Updater)ois.readObject();
                } catch (final ClassNotFoundException e) {
                    throw new IOException("Problem with updater loading: " + e.getMessage(), e);
                }
            }
        }

        MultiLayerNetwork mln;

        if (mln_config != null) {
            mln = new MultiLayerNetwork(mln_config);
            mln.init();
            if (updater != null) {
                mln.setUpdater(updater);
            }

            if (mln_params != null) {
                mln.setParams(mln_params);
            }
        } else {
            mln = null;
        }

        return new DLModelPortObject(layers, mln, null);
    }

    private static void savePortObjectOnly(final DLModelPortObject portObject, final ZipOutputStream out)
        throws IOException {
        final List<Layer> layers = portObject.getLayers();
        final MultiLayerNetwork mln = portObject.getMultilayerLayerNetwork();

        writeLayers(layers, out);
        writeMultiLayerNetwork(mln, out);
    }

    private static void saveSpecOnly(final DLModelPortObjectSpec spec, final ZipOutputStream out) throws IOException {
        final List<DNNLayerType> layerTypes = spec.getLayerTypes();
        final List<DNNType> networkTypes = spec.getNeuralNetworkTypes();
        final boolean isTrained = spec.isTrained();
        final List<Pair<String, String>> learnedColumnTypes = spec.getLearnedColumns();
        final List<String> labels = spec.getLabels();
        final List<String> targetColumnNames = spec.getTargetColumnNames();
        final String learnerType = spec.getLearnerType();

        writeLayerTypes(layerTypes, out);
        writeDNNTypes(networkTypes, out);
        writeIsTrained(isTrained, out);
        writeLearnedColumns(learnedColumnTypes, out);
        writeLabels(labels, out);
        writeTargetColumnNames(targetColumnNames, out);
        writeLearnerType(learnerType, out);
    }

    private static void savePortObjectAndSpec(final DLModelPortObject portObject, final DLModelPortObjectSpec spec,
        final ZipOutputStream out) throws IOException {
        savePortObjectOnly(portObject, out);
        saveSpecOnly(spec, out);
    }

    private static void writeLayers(final List<Layer> layers, final ZipOutputStream out) throws IOException {
        final List<String> allLayersAsJson = convertLayersToJSONs(layers);
        ZipEntry entry;

        for (int i = 0; i < allLayersAsJson.size(); i++) {
            entry = new ZipEntry("layer" + i);
            out.putNextEntry(entry);
            out.write(allLayersAsJson.get(i).getBytes(Charset.forName("UTF-8")));
        }
    }

    private static void writeIsTrained(final boolean isTrained, final ZipOutputStream out) throws IOException {
        ZipEntry entry;
        entry = new ZipEntry("isTrained");
        out.putNextEntry(entry);
        if (isTrained) {
            out.write(Integer.valueOf(1));
        } else {
            out.write(Integer.valueOf(0));
        }
    }

    private static void writeMultiLayerNetwork(final MultiLayerNetwork mln, final ZipOutputStream out)
        throws IOException {
        ZipEntry entry;
        if (mln != null) {
            //write MultilayerNetwork, consists of configuration and network parameters
            entry = new ZipEntry("mln_config");
            out.putNextEntry(entry);
            out.write(mln.getLayerWiseConfigurations().toJson().getBytes(Charset.forName("UTF-8")));

            try {
                //params() throws exception if not yet set
                final INDArray params = mln.params();
                entry = new ZipEntry("mln_params");
                out.putNextEntry(entry);
                //use this write call, Nd4j.write(OutputStream,INDArray) is outdated
                Nd4j.write(params, new DataOutputStream(out));
            } catch (final IOException e) {
                throw e;
            } catch (final Exception e) {
                //net does not contain params so we just write nothing
                LOGGER.debug("Cought Exception writing multi layer network parameter.", e);
            }

            //write updater
            try {
                //if no backprop is done getUpdater() will throw an exception
                if (mln.getUpdater() != null) {
                    entry = new ZipEntry("mln_updater");
                    out.putNextEntry(entry);
                    final ObjectOutputStream oos = new ObjectOutputStream(out);
                    oos.writeObject(mln.getUpdater());
                }
            } catch (final IOException e) {
                throw e;
            } catch (final Exception e) {
                //net does not contain updater because no backprop was done
                LOGGER.debug("Cought Exception writing multi layer network updater.", e);
            }
        }
    }

    private static void writeLayerTypes(final List<DNNLayerType> layerTypes, final ZipOutputStream out)
        throws IOException {
        final String[] layerTypesString = EnumUtils.getStringListFromEnumCollection(layerTypes);
        ZipEntry entry;

        for (int i = 0; i < layerTypesString.length; i++) {
            entry = new ZipEntry("layer_type" + i);
            out.putNextEntry(entry);
            out.write(layerTypesString[i].getBytes(Charset.forName("UTF-8")));
        }

    }

    private static void writeDNNTypes(final List<DNNType> dnnTypes, final ZipOutputStream out) throws IOException {
        final String[] layerTypesStrings = EnumUtils.getStringListFromEnumCollection(dnnTypes);
        ZipEntry entry;

        for (int i = 0; i < layerTypesStrings.length; i++) {
            entry = new ZipEntry("dnn_type" + i);
            out.putNextEntry(entry);
            out.write(layerTypesStrings[i].getBytes(Charset.forName("UTF-8")));
        }
    }

    private static void writeLearnedColumns(final List<Pair<String, String>> learnedColumnTypes,
        final ZipOutputStream out) throws IOException {
        final String[] inputTypesStrings = getSeconds(learnedColumnTypes, String.class);
        final String[] columnNames = getFirsts(learnedColumnTypes, String.class);
        ZipEntry entry;

        for (int i = 0; i < inputTypesStrings.length; i++) {
            entry = new ZipEntry("input_column" + i);
            out.putNextEntry(entry);
            final String stringToWrite = columnNames[i] + "," + inputTypesStrings[i];
            out.write(stringToWrite.getBytes(Charset.forName("UTF-8")));
        }
    }

    private static void writeTargetColumnNames(final List<String> targetColumnNames, final ZipOutputStream out)
        throws IOException {
        ZipEntry entry;
        int i = 0;
        for (String name : targetColumnNames) {
            entry = new ZipEntry("target_column" + i);
            out.putNextEntry(entry);
            out.write(name.getBytes(Charset.forName("UTF-8")));
        }
    }

    private static void writeLearnerType(final String learnerType, final ZipOutputStream out) throws IOException {
        ZipEntry entry = new ZipEntry("learner_type");
        out.putNextEntry(entry);
        out.write(learnerType.getBytes(Charset.forName("UTF-8")));
    }

    private static void writeLabels(final List<String> labels, final ZipOutputStream out) throws IOException {
        ZipEntry entry;

        int i = 0;
        for (final String label : labels) {
            entry = new ZipEntry("label" + i);
            out.putNextEntry(entry);
            out.write(label.getBytes(Charset.forName("UTF-8")));
            i++;
        }
    }

    /**
     * Converts layers to list of Strings in json format.
     *
     * @param layers the list of layers to convert
     * @return the list of converted layers to json
     */
    public static List<String> convertLayersToJSONs(final List<Layer> layers) {
        final List<String> allLayersAsJson = new ArrayList<>();
        final NeuralNetConfiguration config = new NeuralNetConfiguration();
        for (final Layer layer : layers) {
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
    private static String readStringFromZipStream(final ZipInputStream in) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final byte[] byteBuffer = new byte[1024];
        int currentRead = 0;

        while ((currentRead = in.read(byteBuffer, 0, 1024)) >= 0) {
            stringBuilder.append(new String(byteBuffer, 0, currentRead));
        }

        return stringBuilder.toString();
    }

    /**
     * Returns an array of all first elements of the specified list of pairs. Returned array will be of specified class.
     * The class is needed in order to determine the class of the array to return when there are no pairs in the list so
     * we can't get the class during runtime.
     *
     * @param pairs the pairs to get first elements from
     * @param c the class of first elements
     * @return array of all first elements of pairs contained in the list
     */
    @SuppressWarnings("unchecked")
    public static <E, V> E[] getFirsts(final List<Pair<E, V>> pairs, final Class<E> c) {
        return pairs.stream().map(f -> f.getFirst()).collect(Collectors.toList())
            .toArray((E[])Array.newInstance(c, pairs.size()));
    }

    /**
     * Returns an array of all second elements of the specified list of pairs. Returned array will be of specified
     * class. The class is needed in order to determine the class of the array to return when there are no pairs in the
     * list so we can't get the class during runtime.
     *
     * @param pairs the pairs to get second elements from
     * @param c the class of second elements
     * @return array of all second elements of pairs contained in the list
     */
    @SuppressWarnings("unchecked")
    public static <E, V> V[] getSeconds(final List<Pair<E, V>> pairs, final Class<V> c) {
        return pairs.stream().map(f -> f.getSecond()).collect(Collectors.toList())
            .toArray((V[])Array.newInstance(c, pairs.size()));
    }

    /**
     * Returns a cloned list of the specified list of {@link Layer}s.
     *
     * @param layers the list of layers to clone
     * @return clone of specified list of layers
     */
    public static List<Layer> cloneLayers(final List<Layer> layers) {
        final List<Layer> layersClone = new ArrayList<>();
        for (final Layer l : layers) {
            layersClone.add(l.clone());
        }
        return layersClone;
    }
}
