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

    public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<DLModelPortObjectSpec> {
    }

    private static final String CFGKEY_NEURALNETTYPES = "NeuralNetType";

    private static final String CFGKEY_LAYERTYPES = "LayerTypeList";

    private static final String CFGKEY_ISTRAINED = "IsTrained";

    private static final String CFGKEY_COLUMNTYPES = "FeatureColumsTypes";

    private static final String CFGKEY_COLUMNNAMES = "FeatureColumsNames";

    private static final String CFGKEY_LABELS = "Labels";

    private List<DNNType> m_netTypes;

    private List<DNNLayerType> m_layerTypes;

    private boolean m_isTrained;

    private List<Pair<String, String>> m_featureColumns;

    private List<String> m_labels;

    /**
     * Empty no-arg constructor as needed by {@link AbstractSimplePortObjectSpec}.
     */
    public DLModelPortObjectSpec() {

    }

    /**
     * Constructor for class DLModelPortObjectSpec.
     *
     * @param types the list of possible network types for this deep learning model
     * @param layerTypeList the list of layer types contained in this deep learning model
     * @param featureColumns the list columns used for training
     * @param labels the list of possible labels
     * @param isTrained whether the model is trained or not
     */
    public DLModelPortObjectSpec(final List<DNNType> types, final List<DNNLayerType> layerTypeList,
        final List<Pair<String, String>> featureColumns, final List<String> labels, final boolean isTrained) {
        this.m_netTypes = types;
        this.m_layerTypes = layerTypeList;
        this.m_isTrained = isTrained;
        this.m_featureColumns = featureColumns;
        this.m_labels = labels;

    }

    /**
     * Constructor for class DLModelPortObjectSpec. Equal to calling
     * <code>DLModelPortObjectSpec(types, layerTypeList, new ArrayList<>(), new ArrayList<>(), isTrained);</code>
     *
     * @param types the list of possible network types for this deep learning model
     * @param layerTypeList the list of layer types contained in this deep learning model
     * @param isTrained whether the model is trained or not
     */
    public DLModelPortObjectSpec(final List<DNNType> types, final List<DNNLayerType> layerTypeList,
        final boolean isTrained) {
        this(types, layerTypeList, new ArrayList<>(), new ArrayList<>(), isTrained);
    }

    @Override
    protected void save(final ModelContentWO model) {
        model.addStringArray(CFGKEY_NEURALNETTYPES, EnumUtils.getStringListFromEnumCollection(m_netTypes));
        model.addStringArray(CFGKEY_LAYERTYPES, EnumUtils.getStringListFromEnumCollection(m_layerTypes));

        model.addBoolean(CFGKEY_ISTRAINED, m_isTrained);

        model.addStringArray(CFGKEY_COLUMNTYPES, DLModelPortObjectUtils.getSeconds(m_featureColumns, String.class));
        model.addStringArray(CFGKEY_COLUMNNAMES, DLModelPortObjectUtils.getFirsts(m_featureColumns, String.class));

        model.addStringArray(CFGKEY_LABELS, m_labels.toArray(new String[m_labels.size()]));

    }

    @Override
    protected void load(final ModelContentRO model) throws InvalidSettingsException {
        //load labels
        final List<String> labels = new ArrayList<>();
        for (final String label : model.getStringArray(CFGKEY_LABELS)) {
            labels.add(label);
        }
        m_labels = labels;

        //load learned columns types and names
        final String[] columnNames = model.getStringArray(CFGKEY_COLUMNNAMES);
        final String[] columnTypes = model.getStringArray(CFGKEY_COLUMNTYPES);
        final List<Pair<String, String>> namesAndTypes = new ArrayList<>();
        for (int i = 0; i < columnNames.length; i++) {
            namesAndTypes.add(new Pair<String, String>(columnNames[i], columnTypes[i]));
        }
        m_featureColumns = namesAndTypes;

        //load network types
        final List<DNNType> dnnTypeList = new ArrayList<>();
        for (final String dnnTypeAsString : model.getStringArray(CFGKEY_NEURALNETTYPES)) {
            dnnTypeList.add(DNNType.valueOf(dnnTypeAsString));
        }
        m_netTypes = dnnTypeList;

        //load layer types
        final List<DNNLayerType> layerTypeList = new ArrayList<>();
        for (final String layerTypeAsString : model.getStringArray(CFGKEY_LAYERTYPES)) {
            layerTypeList.add(DNNLayerType.valueOf(layerTypeAsString));
        }
        m_layerTypes = layerTypeList;

        //load isTrained flag
        m_isTrained = model.getBoolean(CFGKEY_ISTRAINED);
    }

    public List<DNNType> getNeuralNetworkTypes() {
        return m_netTypes;
    }

    public List<DNNLayerType> getLayerTypes() {
        return m_layerTypes;
    }

    public boolean isTrained() {
        return m_isTrained;
    }

    public List<Pair<String, String>> getLearnedColumns() {
        return m_featureColumns;
    }

    public List<String> getLabels() {
        return m_labels;
    }
}
