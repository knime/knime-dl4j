package org.knime.ext.dl4j.testing.nodes.conversion;

import org.knime.core.data.NominalValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

/**
 * <code>NodeDialog</code> for the "VectorConversionTester" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author KNIME
 */
public class VectorConversionTesterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the VectorConversionTester node.
     */
    protected VectorConversionTesterNodeDialog() {
    	addDialogComponent(new DialogComponentColumnNameSelection(
    			VectorConversionTesterNodeModel.createMnistColumnSelectionModel(),
    			"MNIST Column",
    			0,
    			true,
    			NominalValue.class
                ));
    	addDialogComponent(new DialogComponentBoolean(
        		VectorConversionTesterNodeModel.createExpectBinaryImagesModel(), 
        		"Expect Binary Images?"));
    }
}

