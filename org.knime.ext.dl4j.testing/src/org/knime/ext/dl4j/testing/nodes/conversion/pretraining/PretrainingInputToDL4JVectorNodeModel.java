package org.knime.ext.dl4j.testing.nodes.conversion.pretraining;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.vector.doublevector.DoubleVectorCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.data.iter.PretrainingBufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * This is the model implementation of DL4JModelTester.
 *
 *
 * @author KNIME
 */
public class PretrainingInputToDL4JVectorNodeModel extends AbstractDLNodeModel {

    private DataTableSpec m_outputSpec;

    /**
     * Constructor for the node model.
     */
    protected PretrainingInputToDL4JVectorNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {

        DataSetIterator input = new PretrainingBufferedDataTableDataSetIterator(inData[0], 1, true);

        final BufferedDataContainer container = exec.createDataContainer(m_outputSpec);
        int i = 0;
        while (input.hasNext()) {
            final DataSet n = input.next();
            final INDArray features = n.getFeatureMatrix();
            final INDArray targets = n.getLabels();
            final List<DataCell> cells = new ArrayList<DataCell>();

            cells.add(INDArrayToDoubleVector(features));
            cells.add(INDArrayToDoubleVector(targets));

            container.addRowToTable(new DefaultRow(new RowKey("Row" + i), cells));
            i++;
        }
        container.close();

        return new BufferedDataTable[]{container.getTable()};
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec newSpec = new DataTableSpec();
        newSpec = TableUtils.appendColumnSpec(newSpec, "dl4j_vector", DoubleVectorCellFactory.TYPE);
        newSpec = TableUtils.appendColumnSpec(newSpec, "target_vector", DoubleVectorCellFactory.TYPE);
        m_outputSpec = newSpec;
        return new DataTableSpec[]{newSpec};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        return new ArrayList<SettingsModel>();
    }

    private DataCell INDArrayToDoubleVector(final INDArray arr) {
        final double[] content = new double[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            content[i] = arr.getDouble(i);
        }
        return DoubleVectorCellFactory.createCell(content);
    }
}
