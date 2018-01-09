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
package org.knime.ext.dl4j.base.settings.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.convert.java.DataCellToJavaConverterRegistry;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.IParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Implementation of {@link IParameterSettingsModels} to store and create {@link SettingsModel}s for
 * {@link DataParameter}s.
 *
 * @author David Kolb, KNIME.com GmbH
 */
@Deprecated
public class DataParameterSettingsModels implements IParameterSettingsModels<DataParameter> {

    private Class<? extends DoubleValue>[] m_allowedTypes;

    private SettingsModelIntegerBounded m_batchSize;

    private SettingsModelIntegerBounded m_epochs;

    private SettingsModelString m_labelColumn;

    private SettingsModelColumnFilter2 m_featureColumnSelection2;

    private SettingsModelColumnFilter2 m_targetColumnSelection2;

    private SettingsModelFilterString m_featureColumnSelection;

    private SettingsModelFilterString m_targetColumnSelection;

    private SettingsModelString m_imageSize;

    private SettingsModelString m_documentColumn;

    private SettingsModelString m_sequenceColumn;

    private final List<SettingsModel> m_allInitializedSettings = new ArrayList<>();

    @Override
    public SettingsModel createParameter(final DataParameter enumerate) throws IllegalArgumentException {
        switch (enumerate) {
            case BATCH_SIZE:
                return new SettingsModelIntegerBounded("batch_size", DataParameter.DEFAULT_BATCH_SIZE, 1,
                    Integer.MAX_VALUE);
            case EPOCHS:
                return new SettingsModelIntegerBounded("epochs", DataParameter.DEFAULT_EPOCHS, 1, Integer.MAX_VALUE);
            case FEATURE_COLUMN_SELECTION:
                return new SettingsModelFilterString("column_selection");
            case LABEL_COLUMN:
                return new SettingsModelString("label_column", "");
            case IMAGE_SIZE:
                return new SettingsModelString("image_size", DataParameter.DEFAULT_IMAGE_SIZE);
            case DOCUMENT_COLUMN:
                return new SettingsModelString("document_column", "");
            case SEQUENCE_COLUMN:
                return new SettingsModelString("sequence_column", "");
            case TARGET_COLUMN_SELECTION:
                return new SettingsModelFilterString("target_column_selection");
            case FEATURE_COLUMN_SELECTION2:
                return new SettingsModelColumnFilter2("feature_column_selection2", getAllowedTypes());
            case TARGET_COLUMN_SELECTION2:
                return new SettingsModelColumnFilter2("target_column_selection2", getAllowedTypes());
            default:
                throw new IllegalArgumentException("No case defined for Data Parameter: " + enumerate);
        }
    }

    @Override
    public void setParameter(final DataParameter enumerate) throws IllegalArgumentException {
        switch (enumerate) {
            case BATCH_SIZE:
                m_batchSize = (SettingsModelIntegerBounded)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_batchSize)) {
                    m_allInitializedSettings.add(m_batchSize);
                }
                break;
            case EPOCHS:
                m_epochs = (SettingsModelIntegerBounded)createParameter(enumerate);
                addToSet(m_epochs);
                break;
            case FEATURE_COLUMN_SELECTION:
                m_featureColumnSelection = (SettingsModelFilterString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_featureColumnSelection)) {
                    m_allInitializedSettings.add(m_featureColumnSelection);
                }
                break;
            case LABEL_COLUMN:
                m_labelColumn = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_labelColumn)) {
                    m_allInitializedSettings.add(m_labelColumn);
                }
                break;
            case IMAGE_SIZE:
                m_imageSize = (SettingsModelString)createParameter(enumerate);
                if (!m_allInitializedSettings.contains(m_imageSize)) {
                    m_allInitializedSettings.add(m_imageSize);
                }
                break;
            case DOCUMENT_COLUMN:
                m_documentColumn = (SettingsModelString)createParameter(enumerate);
                addToSet(m_documentColumn);
                break;
            case SEQUENCE_COLUMN:
                m_sequenceColumn = (SettingsModelString)createParameter(enumerate);
                addToSet(m_sequenceColumn);
                break;
            case TARGET_COLUMN_SELECTION:
                m_targetColumnSelection = (SettingsModelFilterString)createParameter(enumerate);
                addToSet(m_targetColumnSelection);
                break;
            case FEATURE_COLUMN_SELECTION2:
                m_featureColumnSelection2 = (SettingsModelColumnFilter2)createParameter(enumerate);
                addToSet(m_featureColumnSelection2);
                break;
            case TARGET_COLUMN_SELECTION2:
                m_targetColumnSelection2 = (SettingsModelColumnFilter2)createParameter(enumerate);
                addToSet(m_targetColumnSelection2);
                break;
            default:
                throw new IllegalArgumentException("No case defined for Data Parameter: " + enumerate);
        }
    }

    public SettingsModelColumnFilter2 getTargetColumnSelection2() {
        return m_targetColumnSelection2;
    }

    public SettingsModelColumnFilter2 getFeatureColumnSelection2() {
        return m_featureColumnSelection2;
    }

    public SettingsModelFilterString getTargetColumnSelection() {
        return m_targetColumnSelection;
    }

    public SettingsModelIntegerBounded getBatchSize() {
        return m_batchSize;
    }

    public SettingsModelIntegerBounded getEpochs() {
        return m_epochs;
    }

    public SettingsModelString getLabelColumn() {
        return m_labelColumn;
    }

    public SettingsModelFilterString getFeatureColumnSelection() {
        return m_featureColumnSelection;
    }

    public SettingsModelString getImageSize() {
        return m_imageSize;
    }

    public SettingsModelString getDocumentColumn() {
        return m_documentColumn;
    }

    public SettingsModelString getSequenceColumn() {
        return m_sequenceColumn;
    }

    @Override
    public List<SettingsModel> getAllInitializedSettings() {
        return m_allInitializedSettings;
    }

    private void addToSet(final SettingsModel model) {
        if (!m_allInitializedSettings.contains(model)) {
            m_allInitializedSettings.add(model);
        }
    }

    /**
     * Retrieve all convertible classes from the converter registry.
     *
     * @return
     */
    private Class<? extends DoubleValue>[] getAllowedTypes() {
        if (m_allowedTypes == null) {
            //get all types from where we can convert to INDArray
            final Set<Class<?>> possibleTypes =
                DataCellToJavaConverterRegistry.getInstance().getFactoriesForDestinationType(INDArray.class)
                    // Get the destination type of factories which can handle mySourceType
                    .stream().map((factory) -> factory.getSourceType())
                    // Put all the destination types into a set
                    .collect(Collectors.toSet());
            //we can also convert collections
            possibleTypes.add(CollectionDataValue.class);
            m_allowedTypes = (Class<? extends DoubleValue>[])possibleTypes.toArray(new Class<?>[possibleTypes.size()]);
        }
        return m_allowedTypes;
    }
}
