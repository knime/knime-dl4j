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
package org.knime.ext.dl4j.base.nodes.predict.feedforward.classification;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.data.iter.ClassificationBufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.nodes.learn.feedforward.classification.FeedforwardClassificationLearnerNodeModel;
import org.knime.ext.dl4j.base.nodes.predict.AbstractDLPredictorNodeModel;
import org.knime.ext.dl4j.base.settings.enumerate.PredictorPrameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JActivationFunction;
import org.knime.ext.dl4j.base.settings.impl.PredictorParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;
import org.knime.ext.dl4j.base.util.NDArrayUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * Classification predictor for feedforward networks of Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class FeedforwardClassificationPredictorNodeModel extends AbstractDLPredictorNodeModel {

    /* SettingsModels */
    private PredictorParameterSettingsModels2 m_predictorParameter;

    private DataTableSpec m_outputSpec;

    /**
     * Constructor for the node model.
     */
    protected FeedforwardClassificationPredictorNodeModel() {
        super(new PortType[]{DLModelPortObject.TYPE, BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final DLModelPortObject model = (DLModelPortObject)inObjects[0];
        final DLModelPortObjectSpec modelSpec = (DLModelPortObjectSpec)model.getSpec();
        final BufferedDataTable table = (BufferedDataTable)inObjects[1];
        final DataTableSpec tableSpec = table.getDataTableSpec();

        //select feature columns from table used for prediction
        final String[] predictCols = DLModelPortObjectUtils.getFirsts(modelSpec.getLearnedColumns(), String.class);
        ColumnRearranger crr = new ColumnRearranger(tableSpec);
        crr.keepOnly(predictCols);
        final BufferedDataTable filteredTable = exec.createColumnRearrangeTable(table, crr, exec);

        try {
            TableUtils.checkForEmptyTable(filteredTable);
        } catch (IllegalStateException e) {
            return createEmptyTable(exec, m_outputSpec);
        }

        //create iterator and prediction
        final DataSetIterator input = new ClassificationBufferedDataTableDataSetIterator(filteredTable, 1);
        final MultiLayerNetwork mln = model.getMultilayerLayerNetwork();

        if (!isOutActivation(model.getLayers(), DL4JActivationFunction.softmax)) {
            throw new InvalidSettingsException("The activation of the output layer is not softmax!");
        }

        final List<String> labels = modelSpec.getLabels();
        boolean appendProbability = m_predictorParameter.getBoolean(PredictorPrameter.APPEND_PROBABILITY);

        //write output to table
        final BufferedDataContainer container = exec.createDataContainer(m_outputSpec);
        final CloseableRowIterator tableIter = table.iterator();

        int i = 0;
        while (tableIter.hasNext()) {
            exec.setProgress((double)(i + 1) / (double)(table.size()));
            exec.checkCanceled();

            final DataRow row = tableIter.next();
            final List<DataCell> cells = TableUtils.toListOfCells(row);

            final DataSet next = input.next();
            final INDArray prediction = predict(mln, next.getFeatureMatrix());

            //add probability values
            if (appendProbability) {
                for (int j = 0; j < prediction.length(); j++) {
                    cells.add(new DoubleCell(prediction.getDouble(j)));
                }
            }

            //append prediction
            final String winningLabel = NDArrayUtils.softmaxActivationToLabel(labels, prediction);
            cells.add(new StringCell(winningLabel));

            container.addRowToTable(new DefaultRow(row.getKey(), cells));
            i++;
        }

        tableIter.close();
        container.close();

        return new PortObject[]{container.getTable()};
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        super.configure(inSpecs);
        DLModelPortObjectSpec modelSpec = (DLModelPortObjectSpec)inSpecs[0];
        DataTableSpec tableSpec = (DataTableSpec)inSpecs[1];

        if (!modelSpec.getLearnerType().equals(FeedforwardClassificationLearnerNodeModel.LEARNER_TYPE)) {
            throw new InvalidSettingsException("Classification predictor is not compatible with '"
                + modelSpec.getLearnerType() + "' model! Classification model expected.");
        }
        if (!containsLabels()) {
            throw new InvalidSettingsException("Model does not contain labels for prediction!");
        }
        if (!containsTargetNames()) {
            throw new InvalidSettingsException("Model does not contain targets for prediction!");
        }
        if (modelSpec.getTargetColumnNames().size() > 1) {
            throw new InvalidSettingsException(
                "Model contains more than one target column. Only single target expected for classification prediction!");
        }

        boolean appendProbability = m_predictorParameter.getBoolean(PredictorPrameter.APPEND_PROBABILITY);
        if (appendProbability) {
            tableSpec = appendProbabilityColumnSpecs(modelSpec, tableSpec);
        }

        String targetColumnName = modelSpec.getTargetColumnNames().get(0);
        String predictionColumnName = "Prediction (" + targetColumnName + ")";
        boolean changePredictionColumnName =
            m_predictorParameter.getBoolean(PredictorPrameter.CHANGE_PREDICTION_COLUMN_NAME);
        if (changePredictionColumnName) {
            predictionColumnName = m_predictorParameter.getString(PredictorPrameter.NEW_PREDICTION_COLUMN_NAME);
        }
        tableSpec = TableUtils.appendColumnSpec(tableSpec, predictionColumnName, DataType.getType(StringCell.class));

        //show current name in dialog
        SettingsModelString columnNameSettings =
            (SettingsModelString)m_predictorParameter.getParameter(PredictorPrameter.NEW_PREDICTION_COLUMN_NAME);
        columnNameSettings.setStringValue(predictionColumnName);

        m_outputSpec = tableSpec;
        return new DataTableSpec[]{m_outputSpec};
    }

    /**
     * Appends column specs of the probability columns to the specified tableSpecs. The number of column specs to append
     * is determined by the number of unique labels.
     *
     * @param modelSpec the spec of the dl model containing labels and one target column name
     * @param tableSpec the initial table spec
     * @return spec with appended probability column specs
     */
    private DataTableSpec appendProbabilityColumnSpecs(final DLModelPortObjectSpec modelSpec,
        final DataTableSpec tableSpec) {
        DataTableSpec specWithProbCols = tableSpec;
        String probabilityColumnSuffix = m_predictorParameter.getString(PredictorPrameter.PROBABILITY_COLUMN_SUFFIX);
        //we expect only one target column, this was (should be) checked in configure
        String targetColumnName = modelSpec.getTargetColumnNames().get(0);
        List<String> labels = modelSpec.getLabels();

        for (String label : labels) {
            String probColName = "P (" + targetColumnName + "=" + label + ")" + probabilityColumnSuffix;
            specWithProbCols =
                TableUtils.appendColumnSpec(specWithProbCols, probColName, DataType.getType(DoubleCell.class));
        }
        return specWithProbCols;
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_predictorParameter = new PredictorParameterSettingsModels2();
        m_predictorParameter.setParameter(PredictorPrameter.CHANGE_PREDICTION_COLUMN_NAME);
        m_predictorParameter.setParameter(PredictorPrameter.NEW_PREDICTION_COLUMN_NAME);
        m_predictorParameter.setParameter(PredictorPrameter.APPEND_PROBABILITY);
        m_predictorParameter.setParameter(PredictorPrameter.PROBABILITY_COLUMN_SUFFIX);

        //set default name in dialog
        SettingsModelString columnNameSettings =
            (SettingsModelString)m_predictorParameter.getParameter(PredictorPrameter.NEW_PREDICTION_COLUMN_NAME);
        columnNameSettings.setStringValue("Prediction ()");

        final List<SettingsModel> settings = new ArrayList<>();
        settings.addAll(m_predictorParameter.getAllInitializedSettings());

        return settings;
    }
}
