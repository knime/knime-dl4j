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
package org.knime.ext.dl4j.base.nodes.layer.convolutional.lrn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.conf.layers.LocalResponseNormalization;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.nodes.layer.AbstractDLLayerNodeModel;
import org.knime.ext.dl4j.base.nodes.layer.DNNLayerType;
import org.knime.ext.dl4j.base.nodes.layer.DNNType;
import org.knime.ext.dl4j.base.settings.enumerate.LayerParameter;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels2;

/**
 * Local Response Normalization layer for Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class LRNLayerNodeModel extends AbstractDLLayerNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(LRNLayerNodeModel.class);

    private static final List<DNNType> DNNTYPES = Arrays.asList(DNNType.DEEPCONVOLUTIONAL);

    private static final DNNLayerType DNNLAYERTYPE = DNNLayerType.LOCAL_RESPONSE_NORMALIZATION;

    /* SettingsModels */
    private LayerParameterSettingsModels2 m_dnnParameterSettings;

    /**
     * Constructor for the node model.
     */
    protected LRNLayerNodeModel() {
        super(new PortType[]{DLModelPortObject.TYPE}, new PortType[]{DLModelPortObject.TYPE});
    }

    @Override
    protected DLModelPortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        final DLModelPortObject portObject = (DLModelPortObject)inData[0];
        final List<Layer> newLayers = portObject.getLayers();

        //parameters
        final int k = m_dnnParameterSettings.getInteger(LayerParameter.LRN_K);
        final int n = m_dnnParameterSettings.getInteger(LayerParameter.LRN_N);
        final double alpha = m_dnnParameterSettings.getDouble(LayerParameter.LRN_ALPHA);
        final double beta = m_dnnParameterSettings.getDouble(LayerParameter.LRN_BETA);

        //build layer
        final Layer lrnLayer = new LocalResponseNormalization.Builder(k, n, alpha, beta).build();

        newLayers.add(lrnLayer);

        DLModelPortObject newPortObject;
        newPortObject = new DLModelPortObject(newLayers, portObject.getMultilayerLayerNetwork(), m_outputSpec);
        return new DLModelPortObject[]{newPortObject};
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return configure(inSpecs, DNNTYPES, DNNLAYERTYPE, logger);
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_dnnParameterSettings = new LayerParameterSettingsModels2();
        m_dnnParameterSettings.setParameter(LayerParameter.LRN_ALPHA);
        m_dnnParameterSettings.setParameter(LayerParameter.LRN_BETA);
        m_dnnParameterSettings.setParameter(LayerParameter.LRN_K);
        m_dnnParameterSettings.setParameter(LayerParameter.LRN_N);

        final List<SettingsModel> settings = new ArrayList<>();
        settings.addAll(m_dnnParameterSettings.getAllInitializedSettings());

        return settings;
    }

}
