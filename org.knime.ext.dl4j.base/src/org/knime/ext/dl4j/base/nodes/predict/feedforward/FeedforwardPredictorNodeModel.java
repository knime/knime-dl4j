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
package org.knime.ext.dl4j.base.nodes.predict.feedforward;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.base.data.filter.column.FilterColumnTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.data.iter.BufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.nodes.predict.AbstractDLPredictorNodeModel;
import org.knime.ext.dl4j.base.settings.enumerate.PredictorPrameter;
import org.knime.ext.dl4j.base.settings.impl.PredictorParameterSettingsModels;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;
import org.knime.ext.dl4j.base.util.NDArrayUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Predictor for feedforward networks of Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class FeedforwardPredictorNodeModel extends AbstractDLPredictorNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(FeedforwardPredictorNodeModel.class);


    /* SettingsModels */
    private PredictorParameterSettingsModels m_predictorParameter;

    private DataTableSpec m_outputSpec;

    /**
     * Constructor for the node model.
     */
    protected FeedforwardPredictorNodeModel() {
        super(new PortType[] { DLModelPortObject.TYPE , BufferedDataTable.TYPE }, new PortType[] {
            BufferedDataTable.TYPE });
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final DLModelPortObject port = (DLModelPortObject)inObjects[0];
        final DLModelPortObjectSpec portSpec = (DLModelPortObjectSpec)port.getSpec();
        final BufferedDataTable table = (BufferedDataTable)inObjects[1];

        //select feature columns from table used for prediction
        final String[] predictCols = DLModelPortObjectUtils.getFirsts(portSpec.getLearnedColumns(), String.class);
        final BufferedDataTable filteredTable = exec.createBufferedDataTable(new FilterColumnTable(table, predictCols), exec);

        //create iterator and prediction
        final BufferedDataTableDataSetIterator input = new BufferedDataTableDataSetIterator(filteredTable, 1);
        final MultiLayerNetwork mln = port.getMultilayerLayerNetwork();

        //set flag if last layer activation is softmax
        final boolean outputActivationIsSoftmax = isOutActivationSoftmax(port.getLayers());

        final boolean appendPrediction = m_predictorParameter.getAppendPrediction().getBooleanValue();
        final boolean appendScore = m_predictorParameter.getAppendScore().getBooleanValue();
        final List<String> labels = portSpec.getLabels();

        //write output to table
        final BufferedDataContainer container = exec.createDataContainer(m_outputSpec);
        final CloseableRowIterator tabelIter = table.iterator();

        int i = 0;
        while(tabelIter.hasNext()){
            exec.setProgress((double)(i+1)/(double)(table.size()));
            exec.checkCanceled();

            final DataRow row = tabelIter.next();
            final List<DataCell> cells = TableUtils.toListOfCells(row);

            final DataSet next = input.next();
            final INDArray prediction = predict(mln, next.getFeatureMatrix());

            final ListCell outputVector = CollectionCellFactory.createListCell(NDArrayUtils.toListOfDoubleCells(prediction));
            cells.add(outputVector);
            if(appendScore){
                final double score = mln.score(new DataSet(next.getFeatureMatrix(),prediction), false);
                cells.add(new DoubleCell(score));
            }
            if(appendPrediction && outputActivationIsSoftmax && containsLabels()){
                final String winningLabel = NDArrayUtils.softmaxActivationToLabel(labels, prediction);
                cells.add(new StringCell(winningLabel));
            } else if (appendPrediction && containsLabels()){
                cells.add(new MissingCell("Output Layer activation is not softmax"));
            } else if (appendPrediction && !containsLabels()){
                cells.add(new MissingCell("Model contains no labels"));
            }

            container.addRowToTable(new DefaultRow(row.getKey(), cells));
            i++;
        }
        if(appendPrediction && !outputActivationIsSoftmax){
            logger.warn("Output Layer activation is not softmax. Label prediction column will be empty.");
        } else if (appendPrediction && outputActivationIsSoftmax && !containsLabels()){
            logger.warn("Model contains no labels. May be trained unsupervised. Label prediction column will be empty.");
        }

        container.close();
        final BufferedDataTable outputTable = container.getTable();

        return new PortObject[]{outputTable};
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        m_outputSpec = configure(inSpecs, logger)[0];
        m_outputSpec = TableUtils.appendColumnSpec(m_outputSpec, "output_activations",
            DataType.getType(ListCell.class, DoubleCell.TYPE));

        final boolean appendScore = m_predictorParameter.getAppendScore().getBooleanValue();
        if(appendScore){
            m_outputSpec = TableUtils.appendColumnSpec(m_outputSpec, "error", DataType.getType(DoubleCell.class));
        }
        final boolean appendPrediction = m_predictorParameter.getAppendPrediction().getBooleanValue();
        if(appendPrediction){
            m_outputSpec = TableUtils.appendColumnSpec(m_outputSpec, "prediction", DataType.getType(StringCell.class));
        }
        return new DataTableSpec[]{m_outputSpec};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_predictorParameter = new PredictorParameterSettingsModels();
        m_predictorParameter.setParameter(PredictorPrameter.APPEND_PREDICTION);
        m_predictorParameter.setParameter(PredictorPrameter.APPEND_SCORE);

        final List<SettingsModel> settings = new ArrayList<>();
        settings.addAll(m_predictorParameter.getAllInitializedSettings());

        return settings;
    }

    /**
     * Creates out for a input {@link INDArray}. The input array must contain each example to predict
     * in a row. Returns a {@link INDArray} with 'number of outputs' columns and 'number of examples'
     * rows, whereby the number of examples is the number of rows of the input array.
     *
     * @param mln the network to use for prediction
     * @param input the input used to create output
     * @param exec {@link ExecutionContext} for progress reporting
     * @return array containing the output of the network for each row of the input
     */
    private INDArray predict(final MultiLayerNetwork mln, final INDArray input){
        final INDArray output = Nd4j.create(input.rows(), getNumberOfOutputs(mln));
        for(int i = 0; i< input.rows(); i++){
            output.putRow(i, mln.output(input.getRow(i), false));
        }
        return output;
    }
}

