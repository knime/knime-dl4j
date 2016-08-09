package org.knime.ext.dl4j.testing.nodes.conversion.regression;

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
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.data.iter.RegressionBufferedDataTableDataSetIterator;
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
public class RegressionInputToDL4JVectorNodeModel extends AbstractDLNodeModel {

    private SettingsModelFilterString m_targetColumns;

    private DataTableSpec m_outputSpec;

    /**
     * Constructor for the node model.
     */
    protected RegressionInputToDL4JVectorNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        final DataTableSpec tableSpec = inData[0].getDataTableSpec();
        List<String> targetColumnsNames = m_targetColumns.getIncludeList();

        DataSetIterator input = null;
        if ((targetColumnsNames != null) && !targetColumnsNames.isEmpty()) {

            List<Integer> targetColumnsIndices = new ArrayList<Integer>();
            for (String targetColumn : targetColumnsNames) {
                targetColumnsIndices.add(inData[0].getSpec().findColumnIndex(targetColumn));
            }
            input = new RegressionBufferedDataTableDataSetIterator(inData[0], 1, targetColumnsIndices, true);
        } else {
            input = new RegressionBufferedDataTableDataSetIterator(inData[0], 1);
        }

        final BufferedDataContainer container = exec.createDataContainer(m_outputSpec);
        int i = 0;
        while (input.hasNext()) {
            final DataSet n = input.next();
            final INDArray features = n.getFeatureMatrix();
            final INDArray target = n.getLabels();
            final List<DataCell> cells = new ArrayList<DataCell>();

            cells.add(INDArrayToDoubleVector(features));
            if ((targetColumnsNames != null) && !targetColumnsNames.isEmpty()) {
                cells.add(INDArrayToDoubleVector(target));
            }

            container.addRowToTable(new DefaultRow(new RowKey("Row" + i), cells));
            i++;
        }
        container.close();

        return new BufferedDataTable[]{container.getTable()};
    }

    public static SettingsModelFilterString createTargetColumnsSettings() {
        return new SettingsModelFilterString("target_columns");
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec newSpec = new DataTableSpec();
        newSpec = TableUtils.appendColumnSpec(newSpec, "dl4j_vector", DoubleVectorCellFactory.TYPE);
        if ((m_targetColumns.getIncludeList() != null) && !m_targetColumns.getIncludeList().isEmpty()) {
            newSpec = TableUtils.appendColumnSpec(newSpec, "target_vector", DoubleVectorCellFactory.TYPE);
        }
        m_outputSpec = newSpec;
        return new DataTableSpec[]{newSpec};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_targetColumns = createTargetColumnsSettings();
        final List<SettingsModel> settings = new ArrayList<SettingsModel>();
        settings.add(m_targetColumns);
        return settings;
    }

    private DataCell INDArrayToDoubleVector(final INDArray arr) {
        final double[] content = new double[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            content[i] = arr.getDouble(i);
        }
        return DoubleVectorCellFactory.createCell(content);
    }
}
