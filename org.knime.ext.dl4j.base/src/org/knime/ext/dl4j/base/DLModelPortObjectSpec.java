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

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.util.Pair;
import org.knime.ext.dl4j.base.DLModelPortObject.ModelType;
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

    /**
     * Serializer for class DLModelPortObjectSpec.
     *
     * @author David Kolb, KNIME.com GmbH
     */
    public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<DLModelPortObjectSpec> {
    }

    private static final String CFGKEY_NEURALNETTYPES = "NeuralNetType";

    private static final String CFGKEY_LAYERTYPES = "LayerTypeList";

    private static final String CFGKEY_ISTRAINED = "IsTrained";

    private static final String CFGKEY_FEATURECOLUMNTYPES = "FeatureColumsTypes";

    private static final String CFGKEY_TARGETCOLUMNNAMES = "TargetColumNames";

    private static final String CFGKEY_FEATURECOLUMNNAMES = "FeatureColumsNames";

    private static final String CFGKEY_LEARNERTYPE = "LearnerType";

    private static final String CFGKEY_LABELS = "Labels";

    private static final String CFGKEY_MODELTYPE = "ModelType";

    private List<DNNType> m_netTypes;

    private List<DNNLayerType> m_layerTypes;

    private List<String> m_targetColumnNames;

    private boolean m_isTrained;

    private List<Pair<String, String>> m_featureColumns;

    private List<String> m_labels;

    private String m_learnerType;

    private String m_modelType;

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
     * @param featureColumns the list of columns used for training
     * @param labels the list of possible labels
     * @param targetColumnNames the list of target column names
     * @param learnerType the type of the used learner specifying the use case
     * @param isTrained whether the model is trained or not
     * @param modelType the type of the model contained in the corresponding port object
     */
    public DLModelPortObjectSpec(final List<DNNType> types, final List<DNNLayerType> layerTypeList,
        final List<Pair<String, String>> featureColumns, final List<String> labels,
        final List<String> targetColumnNames, final String learnerType, final boolean isTrained,
        final ModelType modelType) {
        m_netTypes = types;
        m_layerTypes = layerTypeList;
        m_isTrained = isTrained;
        m_featureColumns = featureColumns;
        m_labels = labels;
        m_targetColumnNames = targetColumnNames;
        m_learnerType = learnerType;

        if (modelType != null) {
            m_modelType = modelType.name();
        } else {
            m_modelType = "";
        }
    }

    /**
     * Constructor for class DLModelPortObjectSpec. Equal to calling
     * <code>DLModelPortObjectSpec(types, layerTypeList, featureColumns, labels, new ArrayList<>(), "", isTrained);</code>
     *
     * @param types the list of possible network types for this deep learning model
     * @param layerTypeList the list of layer types contained in this deep learning model
     * @param featureColumns the list of columns used for training
     * @param labels the list of possible labels
     * @param isTrained whether the model is trained or not
     */
    public DLModelPortObjectSpec(final List<DNNType> types, final List<DNNLayerType> layerTypeList,
        final List<Pair<String, String>> featureColumns, final List<String> labels, final boolean isTrained) {
        this(types, layerTypeList, featureColumns, labels, new ArrayList<>(), "", isTrained, null);
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

        model.addStringArray(CFGKEY_FEATURECOLUMNTYPES,
            DLModelPortObjectUtils.getSeconds(m_featureColumns, String.class));
        model.addStringArray(CFGKEY_FEATURECOLUMNNAMES,
            DLModelPortObjectUtils.getFirsts(m_featureColumns, String.class));

        model.addStringArray(CFGKEY_LABELS, m_labels.toArray(new String[m_labels.size()]));

        model.addStringArray(CFGKEY_TARGETCOLUMNNAMES,
            m_targetColumnNames.toArray(new String[m_targetColumnNames.size()]));

        model.addString(CFGKEY_LEARNERTYPE, m_learnerType);

        model.addString(CFGKEY_MODELTYPE, m_modelType);
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
        final String[] columnNames = model.getStringArray(CFGKEY_FEATURECOLUMNNAMES);
        final String[] columnTypes = model.getStringArray(CFGKEY_FEATURECOLUMNTYPES);
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

        final List<String> targetColumnNames = new ArrayList<>();
        for (final String colName : model.getStringArray(CFGKEY_TARGETCOLUMNNAMES, new String[]{})) {
            targetColumnNames.add(colName);
        }
        m_targetColumnNames = targetColumnNames;

        m_learnerType = model.getString(CFGKEY_LEARNERTYPE, "");
        m_modelType = model.getString(CFGKEY_MODELTYPE, "");
    }

    /**
     * @return list of names of target columns
     */
    public List<String> getTargetColumnNames() {
        return m_targetColumnNames;
    }

    /**
     * @return list of possible {@link DNNType}s of the model contained in the corresponding port object.
     */
    public List<DNNType> getNeuralNetworkTypes() {
        return m_netTypes;
    }

    /**
     * @return list of {@link DNNLayerType}s contained in the {@link MultiLayerNetwork} if model type is MLN.
     */
    public List<DNNLayerType> getLayerTypes() {
        return m_layerTypes;
    }

    /**
     * @return flag indicating if the model is trained or not
     */
    public boolean isTrained() {
        return m_isTrained;
    }

    /**
     * @return list of pairs of columns the model was trained on. First String corresponds to column name, second to
     *         column type.
     */
    public List<Pair<String, String>> getLearnedColumns() {
        return m_featureColumns;
    }

    /**
     * @return list of labels used for training of the model was trained supervised.
     */
    public List<String> getLabels() {
        return m_labels;
    }

    /**
     * @return the type of learner used to train this model. This specifies the use case. For possible values see static
     *         access identifier member in learner model implementation.
     */
    public String getLearnerType() {
        return m_learnerType;
    }

    /**
     * @return the type of model contained in the corresponding port object. See {@link ModelType}.
     */
    public ModelType getModelType() {
        if (!m_modelType.isEmpty()) {
            return ModelType.valueOf(m_modelType);
        }
        return null;
    }
}
