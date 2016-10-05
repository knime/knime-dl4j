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
package org.knime.ext.dl4j.base.nodes.layer.deepbelief.rbm;

import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.weights.WeightInit;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.enumerate.LayerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JActivationFunction;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JLossFunction;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.EnumUtils;

/**
 * <code>NodeDialog</code> for the "RBMLayer" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class RBMLayerNodeDialog extends DefaultNodeSettingsPane {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(RBMLayerNodeModel.class);

    /**
     * New pane for configuring RBMLayer node dialog. This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected RBMLayerNodeDialog() {
        final LayerParameterSettingsModels2 dnnSettingsModels = new LayerParameterSettingsModels2();

        try {
            addDialogComponent(new DialogComponentNumberEdit(
                (SettingsModelIntegerBounded)dnnSettingsModels.createParameter(LayerParameter.NUMBER_OF_OUTPUTS),
                "Number of Output Units", 4));
            addDialogComponent(new DialogComponentNumberEdit(
                (SettingsModelIntegerBounded)dnnSettingsModels.createParameter(LayerParameter.RBM_ITERATIONS),
                "Contrastive Divergence Iterations", 4));
            addDialogComponent(new DialogComponentNumberEdit(
                (SettingsModelDoubleBounded)dnnSettingsModels.createParameter(LayerParameter.DROP_OUT), "Drop Out Rate",
                4));
            addDialogComponent(new DialogComponentNumberEdit(
                (SettingsModelDoubleBounded)dnnSettingsModels.createParameter(LayerParameter.LEARNING_RATE),
                "Learning Rate", 4));
            addDialogComponent(new DialogComponentStringSelection(
                (SettingsModelString)dnnSettingsModels.createParameter(LayerParameter.HIDDEN_UNIT),
                "Hidden Unit Transformation", EnumUtils.getStringCollectionFromToString(RBM.HiddenUnit.values())));
            addDialogComponent(new DialogComponentStringSelection(
                (SettingsModelString)dnnSettingsModels.createParameter(LayerParameter.VISIBLE_UNIT),
                "Visible Unit Transformation", EnumUtils.getStringCollectionFromToString(RBM.VisibleUnit.values())));
            addDialogComponent(new DialogComponentStringSelection(
                (SettingsModelString)dnnSettingsModels.createParameter(LayerParameter.WEIGHT_INIT),
                "Weight Initialization Strategy", EnumUtils.getStringCollectionFromToString(WeightInit.values())));
            addDialogComponent(new DialogComponentStringSelection(
                (SettingsModelString)dnnSettingsModels.createParameter(LayerParameter.LOSS_FUNCTION), "Loss Function",
                EnumUtils.getStringCollectionFromToString(DL4JLossFunction.values())));
            addDialogComponent(new DialogComponentStringSelection(
                (SettingsModelString)dnnSettingsModels.createParameter(LayerParameter.ACTIVATION),
                "Activation Function", EnumUtils.getStringCollectionFromToString(DL4JActivationFunction.values())));
        } catch (final IllegalStateException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}
