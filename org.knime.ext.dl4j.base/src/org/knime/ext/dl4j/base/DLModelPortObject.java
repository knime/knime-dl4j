/*******************************************************************************
 * Copyright by KNIME AG, Zurich, Switzerland
 * Website: http://www.knime.com; Email: contact@knime.com
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
 * KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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

import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.ext.dl4j.base.exception.DL4JOutOfMemoryException;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;

/**
 * PortObject for Deep Learning Models.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class DLModelPortObject extends AbstractPortObject {

    /**
     * Serializer for class DLModelPortObject.
     *
     * @author David Kolb, KNIME.com GmbH
     */
    public static final class Serializer extends AbstractPortObjectSerializer<DLModelPortObject> {
    }

    /**
     * Define port type of objects of this class when used as PortObjects.
     */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(DLModelPortObject.class);

    private static final String SUMMARY = "Deep Learning Model";

    private List<Layer> m_layers;

    private Model m_model;

    private DLModelPortObjectSpec m_spec;

    /** Framework constructor. */
    public DLModelPortObject() {
    }

    /**
     * Constructor for class DLModelPortObject.
     *
     * @param layers the list of layers contained in this model
     * @param model the {@link Model} which should be stored
     * @param spec the specs corresponding to this object
     */
    public DLModelPortObject(final List<Layer> layers, final Model model, final DLModelPortObjectSpec spec) {
        this.m_layers = layers;
        this.m_spec = spec;
        this.m_model = model;
    }

    @Override
    protected void save(final PortObjectZipOutputStream out, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

        DLModelPortObjectUtils.saveModelToZip(this, true, false, out);
    }

    @Override
    protected void load(final PortObjectZipInputStream in, final PortObjectSpec spec, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

        DLModelPortObject port = null;
        try {
            port = DLModelPortObjectUtils.loadPortFromZip(in);
        } catch (Error e) {
            DL4JOutOfMemoryException dl4jOoM = DL4JOutOfMemoryException.fromDL4JError(e);
            throw new IOException(dl4jOoM == null ? e : dl4jOoM);
        }

        this.m_spec = (DLModelPortObjectSpec)spec;
        this.m_layers = port.getLayers();
        this.m_model = port.getModel();
    }

    @Override
    public String getSummary() {
        return SUMMARY;
    }

    @Override
    public PortObjectSpec getSpec() {
        return m_spec;
    }

    /**
     * List of layers this model is composed of.
     *
     * @return list of layers
     */
    public List<Layer> getLayers() {
        return m_layers;
    }

    /**
     * The dl4j model. This could be either MultiLayerNetwork or ComputationGraph.
     *
     * @return the dl4j model
     */
    public Model getModel() {
        return m_model;
    }

    /**
     * Assumes that this model contains a {@link MultiLayerNetwork}. Otherwise this method will return null.
     *
     * @return the {@link MultiLayerNetwork} or null
     */
    public MultiLayerNetwork getMultilayerLayerNetwork() {
        if (m_model instanceof MultiLayerNetwork) {
            return (MultiLayerNetwork)m_model;
        }
        return null;
    }

    /**
     * Assumes that this model contains a {@link ComputationGraph}. Otherwise this method will return null.
     *
     * @return the {@link ComputationGraph} or null
     */
    public ComputationGraph getComputationGraph() {
        if (m_model instanceof ComputationGraph) {
            return (ComputationGraph)m_model;
        }
        return null;
    }

    @Override
    public JComponent[] getViews() {
        return new JComponent[]{};
    }

    /**
     * Currently supported implementations of {@link Model} by this port object.
     *
     * @author David Kolb, KNIME.com GmbH
     */
    public enum ModelType {
        /** MultiLayerNetwork */
        MLN,
        /** ComputationGraph */
        CG;
    }
}
