package org.knime.ext.dl4j.testing.nodes.conversion;

import org.knime.core.data.NominalValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

/**
 * <code>NodeDialog</code> for the "DL4JModelTester" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author KNIME
 */
public class TableToDL4JVectorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the DL4JModelTester node.
     */
    protected TableToDL4JVectorNodeDialog() {
        addDialogComponent(
            new DialogComponentColumnNameSelection(TableToDL4JVectorNodeModel.createLabelColumnSettings(),
                "Label Column", 0, false, true, NominalValue.class));

    }
}
