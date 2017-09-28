/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
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
package org.knime.ext.dl4j.base.nodes.predict.feedforward.layer;

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
import org.knime.core.data.vector.doublevector.DenseDoubleVectorCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.data.iter.ClassificationBufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.nodes.predict.AbstractDLPredictorNodeModel;
import org.knime.ext.dl4j.base.settings.enumerate.PredictorPrameter;
import org.knime.ext.dl4j.base.settings.impl.PredictorParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;
import org.knime.ext.dl4j.base.util.NDArrayUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * Layer predictor for feedforward networks of Deeplearning4J integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class FeedforwardLayerPredictorNodeModel extends AbstractDLPredictorNodeModel {

    /* SettingsModels */
    private PredictorParameterSettingsModels2 m_predictorParameter;

    private DataTableSpec m_outputSpec;

    /**
     * Constructor for the node model.
     */
    protected FeedforwardLayerPredictorNodeModel() {
        super(new PortType[]{DLModelPortObject.TYPE, BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    @Override
    protected PortObject[] executeDL4JMemorySafe(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
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
            final int layerToActivate = getLayerNumFromDialogSelection();
            final INDArray activation = activate(mln, layerToActivate, next.getFeatureMatrix());

            final DenseDoubleVectorCell outputVector = NDArrayUtils.toDoubleVector(activation);
            cells.add(outputVector);

            container.addRowToTable(new DefaultRow(row.getKey(), cells));
            i++;
        }

        tableIter.close();
        container.close();

        return new PortObject[]{container.getTable()};
    }

    /**
     * Determines the index of the layer from the String selected in the dialog. Is expected to be in the format
     * 'layerNum':'layerName'
     *
     * @return the index of the layer selected in the dialog
     */
    private int getLayerNumFromDialogSelection() {
        String layerSelection = m_predictorParameter.getString(PredictorPrameter.LAYER_SELECTION);
        return Integer.parseInt(layerSelection.split(":")[0]);
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        super.configure(inSpecs);
        DataTableSpec tableSpec = (DataTableSpec)inSpecs[1];

        String layerToActivate = m_predictorParameter.getString(PredictorPrameter.LAYER_SELECTION);
        if (layerToActivate.isEmpty()) {
            throw new InvalidSettingsException("Plese select layer to activate!");
        }

        tableSpec = TableUtils.appendColumnSpec(tableSpec, "Activations Layer " + layerToActivate,
            DataType.getType(DenseDoubleVectorCell.class, DoubleCell.TYPE));

        m_outputSpec = tableSpec;
        return new DataTableSpec[]{m_outputSpec};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_predictorParameter = new PredictorParameterSettingsModels2();
        m_predictorParameter.setParameter(PredictorPrameter.LAYER_SELECTION);

        final List<SettingsModel> settings = new ArrayList<>();
        settings.addAll(m_predictorParameter.getAllInitializedSettings());

        return settings;
    }
}
