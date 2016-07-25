package org.knime.ext.dl4j.testing.nodes.conversion.regression;

import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.data.DoubleValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.convert.java.DataCellToJavaConverterRegistry;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * <code>NodeDialog</code> for the "DL4JModelTester" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author KNIME
 */
public class RegressionInputToDL4JVectorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the DL4JModelTester node.
     */
    protected RegressionInputToDL4JVectorNodeDialog() {

      //get all types from where we can convert to INDArray
        final Set<Class<?>> possibleTypes =
            DataCellToJavaConverterRegistry.getInstance().getFactoriesForDestinationType(INDArray.class)
                // Get the destination type of factories which can handle mySourceType
                .stream().map((factory) -> factory.getSourceType())
                // Put all the destination types into a set
                .collect(Collectors.toSet());
        //we can also convert collections
        possibleTypes.add(CollectionDataValue.class);
        final Class<? extends DoubleValue>[] possibleTypesArray =
            (Class<? extends DoubleValue>[])possibleTypes.toArray(new Class<?>[possibleTypes.size()]);

        createNewGroup("Target Columns");
        addDialogComponent(new DialogComponentColumnFilter(
            RegressionInputToDL4JVectorNodeModel.createTargetColumnsSettings(), 0, true, possibleTypesArray));
        closeCurrentGroup();
    }
}
