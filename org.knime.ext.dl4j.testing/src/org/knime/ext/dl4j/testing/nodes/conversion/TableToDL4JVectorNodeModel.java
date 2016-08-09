package org.knime.ext.dl4j.testing.nodes.conversion;

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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.data.iter.ClassificationBufferedDataTableDataSetIterator;
import org.knime.ext.dl4j.base.util.ConverterUtils;
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
public class TableToDL4JVectorNodeModel extends AbstractDLNodeModel {

    private SettingsModelString m_labelColumn;

    private DataTableSpec m_outputSpec;

    /**
     * Constructor for the node model.
     */
    protected TableToDL4JVectorNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        final DataTableSpec tableSpec = inData[0].getDataTableSpec();

        DataSetIterator input = null;
        if ((m_labelColumn.getStringValue() != null) && !m_labelColumn.getStringValue().isEmpty()) {
            final String labelColumnName = m_labelColumn.getStringValue();
            final List<String> labels = new ArrayList<String>();
            for (final DataCell cell : tableSpec.getColumnSpec(labelColumnName).getDomain().getValues()) {
                labels.add(ConverterUtils.convertDataCellToJava(cell, String.class));
            }
            input = new ClassificationBufferedDataTableDataSetIterator(inData[0],
                tableSpec.findColumnIndex(labelColumnName), 1, labels, true);
        } else {
            input = new ClassificationBufferedDataTableDataSetIterator(inData[0], 1);
        }

        final BufferedDataContainer container = exec.createDataContainer(m_outputSpec);
        int i = 0;
        while (input.hasNext()) {
            final DataSet n = input.next();
            final INDArray features = n.getFeatureMatrix();
            final INDArray one_hot = n.getLabels();
            final List<DataCell> cells = new ArrayList<DataCell>();

            cells.add(INDArrayToDoubleVector(features));
            if ((m_labelColumn.getStringValue() != null) && !m_labelColumn.getStringValue().isEmpty()) {
                cells.add(INDArrayToDoubleVector(one_hot));
            }

            container.addRowToTable(new DefaultRow(new RowKey("Row" + i), cells));
            i++;
        }
        container.close();

        return new BufferedDataTable[]{container.getTable()};
    }

    public static SettingsModelString createLabelColumnSettings() {
        return new SettingsModelString("label_column", "");
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec newSpec = new DataTableSpec();
        newSpec = TableUtils.appendColumnSpec(newSpec, "dl4j_vector", DoubleVectorCellFactory.TYPE);
        if ((m_labelColumn.getStringValue() != null) && !m_labelColumn.getStringValue().isEmpty()) {
            newSpec = TableUtils.appendColumnSpec(newSpec, "one_hot_label", DoubleVectorCellFactory.TYPE);
        }
        m_outputSpec = newSpec;
        return new DataTableSpec[]{newSpec};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_labelColumn = createLabelColumnSettings();
        final List<SettingsModel> settings = new ArrayList<SettingsModel>();
        settings.add(m_labelColumn);
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
