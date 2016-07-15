package org.knime.ext.dl4j.testing.nodes.model;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;

/**
 * <code>NodeDialog</code> for the "DL4JModelTester" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author KNIME
 */
public class DL4JModelTesterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the DL4JModelTester node.
     */
    protected DL4JModelTesterNodeDialog() {
        addDialogComponent(new DialogComponentBoolean(DL4JModelTesterNodeModel.createCompareModelsModel(),
            "Check Models for Differences?"));
        addDialogComponent(new DialogComponentBoolean(DL4JModelTesterNodeModel.createOutputModelsModel(),
            "Output Model Configurations?"));
    }
}
