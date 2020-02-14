package org.knime.ext.dl4j.testing.nodes.model;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.DLModelPortObject;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.util.DLModelPortObjectUtils;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * This is the model implementation of DL4JModelTester.
 *
 *
 * @author KNIME
 */
public class DL4JModelTesterNodeModel extends AbstractDLNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(DL4JModelTesterNodeModel.class);

    private final static int IN_PORT1 = 0;

    private final static int IN_PORT2 = 1;

    private SettingsModelBoolean m_compareModels;

    private SettingsModelBoolean m_outputModels;

    /**
     * Constructor for the node model.
     */
    protected DL4JModelTesterNodeModel() {
        super(new PortType[]{DLModelPortObject.TYPE, DLModelPortObject.TYPE},
            new PortType[]{BufferedDataTable.TYPE, BufferedDataTable.TYPE});
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final DLModelPortObject model1 = (DLModelPortObject)inObjects[IN_PORT1];
        final DLModelPortObject model2 = (DLModelPortObject)inObjects[IN_PORT2];

        final boolean appendOutput = m_outputModels.getBooleanValue();
        final boolean compareModels = m_compareModels.getBooleanValue();
        final List<BufferedDataTable> outputTables = new ArrayList<>();

        if (compareModels) {
            compareModels(model1, model2);
        }

        if (appendOutput) {
            outputTables.add(convertDNNModelToTable(model1, exec));
            outputTables.add(convertDNNModelToTable(model2, exec));
        } else {
            final BufferedDataContainer container = exec.createDataContainer(createOutputSpec());
            container.close();
            outputTables.add(container.getTable());
            outputTables.add(container.getTable());
        }

        return outputTables.toArray(new BufferedDataTable[outputTables.size()]);
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new PortObjectSpec[]{createOutputSpec(), createOutputSpec()};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        final List<SettingsModel> settings = new ArrayList<>();
        m_compareModels = createCompareModelsModel();
        m_outputModels = createOutputModelsModel();
        settings.add(m_compareModels);
        settings.add(m_outputModels);

        return settings;
    }

    /**
     * Creates the compare models settings model.
     *
     * @return the compare models setting model
     */
    public static SettingsModelBoolean createCompareModelsModel() {
        return new SettingsModelBoolean("compate_models", false);
    }

    /**
     * Creates the output models settings model.
     *
     * @return the output models settings model
     */
    public static SettingsModelBoolean createOutputModelsModel() {
        return new SettingsModelBoolean("output_models", false);
    }

    private DataTableSpec createOutputSpec() {
        final boolean appendOutput = m_outputModels.getBooleanValue();
        if (appendOutput) {
            final DataColumnSpecCreator listColSpecCreator =
                new DataColumnSpecCreator("Model", DataType.getType(StringCell.class));
            final DataColumnSpec colSpecs = listColSpecCreator.createSpec();
            return new DataTableSpec(colSpecs);
        }
        return new DataTableSpec();
    }

    private static void compareModels(final DLModelPortObject model1, final DLModelPortObject model2) throws Exception {
        compareLayerLists(model1.getLayers(), model2.getLayers());
        compareMLN(model1.getMultilayerLayerNetwork(), model2.getMultilayerLayerNetwork());
        compareSpec(model1.getSpec(), model2.getSpec());
    }

    private static void compareSpec(final DLModelPortObjectSpec s1, final DLModelPortObjectSpec s2) throws Exception {
        if ((s1 != null) && (s2 != null)) {
            if (!s1.equals(s2)) {
                logger.error("Spec of model 1 is different than Spec of model 2");
            }
        }
    }

    private static void compareMLN(final MultiLayerNetwork m1, final MultiLayerNetwork m2) throws Exception {
        if ((m1 != null) && (m2 != null)) {
            final String m1Conf = m1.getLayerWiseConfigurations().toJson();
            final String m2Conf = m2.getLayerWiseConfigurations().toJson();
            if (!m1Conf.equals(m2Conf)) {
                logger.error("MultiLayerNetwork Configuration of model 1 is different from MultiLayerNetwork "
                    + "Configuration of model 2");
            }

            boolean m1ContainsParams = true;
            INDArray m1params = null;
            try {
                m1params = m1.params();
            } catch (final Exception e) {
                m1ContainsParams = false;
            }

            boolean m2ContainsParams = true;
            INDArray m2params = null;
            try {
                m2params = m2.params();
            } catch (final Exception e) {
                m2ContainsParams = false;
            }
            if (m1ContainsParams && m2ContainsParams) {
                if(m1params == null) {
                    logger.error("Parameters of model 1 were null");
                }
                else if (!m1params.equals(m2params)) {
                    logger.error("Parameters of model 1 are different from Parameter of model 2");
                }
            } else if (!m1ContainsParams && !m2ContainsParams) {
                //nothing to check here
            } else if (m1ContainsParams && !m2ContainsParams) {
                logger.error("model 1 contains parameters but model2 doesnt");
            } else if (!m1ContainsParams && m2ContainsParams) {
                logger.error("model 2 contains parameters but model1 doesnt");
            }
        } else if ((m1 == null) && (m2 != null)) {
            logger.error("model 1 does not contain MultiLayerNetwork");
        } else if ((m2 == null) && (m1 != null)) {
            logger.error("model 2 does not contain MultiLayerNetwork");
        }
    }

    private static void compareLayerLists(final List<Layer> l1, final List<Layer> l2) throws Exception {
        if ((l1 != null) && (l2 != null)) {
            if (l1.size() != l2.size()) {
                logger.error("Different number of Layers. Number of Layers model 1: " + l1.size()
                    + " Number of Layers model 2: " + l2.size());
                return;
            }
            for (int i = 0; i < l1.size(); i++) {
                if (!l1.get(i).equals(l2.get(i))) {
                    logger.error("Layer " + (i + 1) + " of model 1 is different from Layer " + (i + 1) + " of model 2");
                    return;
                }
            }
        }
    }

    private BufferedDataTable convertDNNModelToTable(final DLModelPortObject model, final ExecutionContext exec) {
        final BufferedDataContainer container = exec.createDataContainer(createOutputSpec());

        final List<String> jsons = DLModelPortObjectUtils.convertLayersToJSONs(model.getLayers());
        for (int i = 0; i < jsons.size(); i++) {
            final StringCell c = new StringCell(jsons.get(i));
            container.addRowToTable(new DefaultRow("Layer" + i, c));
        }

        if (model.getMultilayerLayerNetwork() != null) {
            final String modelJson = model.getMultilayerLayerNetwork().getLayerWiseConfigurations().toJson();
            final StringCell c = new StringCell(modelJson);
            container.addRowToTable(new DefaultRow("Model", c));
        }
        container.close();
        return container.getTable();
    }

}
