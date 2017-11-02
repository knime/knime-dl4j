/*******************************************************************************
 * Copyright by KNIME AG, Zurich, Switzerland
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
package org.knime.ext.dl4j.base.nodes.learn.feedforward;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deeplearning4j.nn.weights.WeightInit;
import org.knime.core.data.DataValue;
import org.knime.core.data.NominalValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.data.convert.extension.DL4JConverterRegistry;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.knime.ext.dl4j.base.settings.enumerate.LayerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.enumerate.TrainingMode;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JActivationFunction;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JGradientNormalization;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JLossFunction;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JOptimizationAlgorithm;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JUpdater;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.util.EnumUtils;

/**
 * <code>NodeDialog</code> for the "DL4JLearner" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author David Kolb, KNIME.com GmbH
 * @deprecated
 */
@Deprecated
public class FeedforwardLearnerNodeDialog extends DefaultNodeSettingsPane {

    private final int DEFAULT_WIDTH = 4;

    /**
     * New pane for configuring the DL4JLearner node.
     */
    protected FeedforwardLearnerNodeDialog() {
        final LearnerParameterSettingsModels learnerSettingsModels = new LearnerParameterSettingsModels();
        final DataParameterSettingsModels dataSettingsModels = new DataParameterSettingsModels();
        final LayerParameterSettingsModels layerSettingsModels = new LayerParameterSettingsModels();

        setDefaultTabTitle("Learning Parameters");
        final SettingsModelString trainingModeSettings =
            (SettingsModelString)learnerSettingsModels.createParameter(LearnerParameter.TRAINING_MODE);
        final SettingsModelString labelColumnSettings =
            (SettingsModelString)dataSettingsModels.createParameter(DataParameter.LABEL_COLUMN);
        final SettingsModelFilterString columnSelectionSettings =
            (SettingsModelFilterString)dataSettingsModels.createParameter(DataParameter.FEATURE_COLUMN_SELECTION);
        final SettingsModelIntegerBounded numberOfOutputs =
            (SettingsModelIntegerBounded)layerSettingsModels.createParameter(LayerParameter.NUMBER_OF_OUTPUTS);

        trainingModeSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent arg0) {
                final TrainingMode mode = TrainingMode.valueOf(trainingModeSettings.getStringValue());
                switch (mode) {
                    case SUPERVISED:
                        labelColumnSettings.setEnabled(true);
                        columnSelectionSettings.setEnabled(true);
                        numberOfOutputs.setEnabled(false);
                        break;
                    case UNSUPERVISED:
                        labelColumnSettings.setEnabled(false);
                        columnSelectionSettings.setEnabled(true);
                        numberOfOutputs.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        });

        addDialogComponent(new DialogComponentButtonGroup(trainingModeSettings, false, "Training Mode",
            EnumUtils.getStringListFromToString(TrainingMode.values())));

        createNewGroup("Seed");
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_SEED), "Use Seed"));

        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelIntegerBounded)learnerSettingsModels.createParameter(LearnerParameter.SEED), "Seed", DEFAULT_WIDTH));
        closeCurrentGroup();

        createNewGroup("Training Method");
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelIntegerBounded)learnerSettingsModels.createParameter(LearnerParameter.TRAINING_ITERATIONS),
            "Number of Training Iterations", 4));
        addDialogComponent(new DialogComponentStringSelection(
            (SettingsModelString)learnerSettingsModels.createParameter(LearnerParameter.OPTIMIZATION_ALGORITHM),
            "Optimization Algorithm", EnumUtils.getStringCollectionFromToString(DL4JOptimizationAlgorithm.values())));
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_BACKPROP),
            "Do Backpropagation"));
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_PRETRAIN),
            "Do Pretraining"));
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_FINETUNE),
            "Do Finetuning"));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Updater");
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_PRETRAINED_UPDATER),
            "Use Pretrained Updater"));
        addDialogComponent(new DialogComponentStringSelection(
            (SettingsModelString)learnerSettingsModels.createParameter(LearnerParameter.UPDATER), "Updater Type",
            EnumUtils.getStringCollectionFromToString(DL4JUpdater.values())));
        closeCurrentGroup();

        createNewGroup("Regularization");
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_REGULARIZATION),
            "Use Regularization"));
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelDoubleBounded)learnerSettingsModels.createParameter(LearnerParameter.L1),
            "L1 Regularization Coefficient", DEFAULT_WIDTH));
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelDoubleBounded)learnerSettingsModels.createParameter(LearnerParameter.L2),
            "L2 Regularization Coefficient", DEFAULT_WIDTH));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Gradient Normalization");
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_GRADIENT_NORMALIZATION),
            "Use Gradient Normalization"));
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentStringSelection(
            (SettingsModelString)learnerSettingsModels.createParameter(LearnerParameter.GRADIENT_NORMALIZATION),
            "Gradient Normalization Strategy",
            EnumUtils.getStringCollectionFromToString(DL4JGradientNormalization.values())));
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelDoubleBounded)learnerSettingsModels
                .createParameter(LearnerParameter.GRADIENT_NORMALIZATION_THRESHOLD),
            "Gradient Normalization Threshold", DEFAULT_WIDTH));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Momentum");
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_MOMENTUM),
            "Use Momentum"));
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelDoubleBounded)learnerSettingsModels.createParameter(LearnerParameter.MOMENTUM),
            "Momentum Rate", DEFAULT_WIDTH));
        addDialogComponent(new DialogComponentString(
            (SettingsModelString)learnerSettingsModels.createParameter(LearnerParameter.MOMENTUM_AFTER),
            "Momentum After", false, DEFAULT_WIDTH));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Drop Connect");
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_DROP_CONNECT),
            "Use Drop Connect"));
        closeCurrentGroup();

        createNewTab("Global Parameters");

        createNewGroup("Global Learning Rate");
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_GLOBAL_LEARNING_RATE),
            "Use Global Learning Rate"));
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelDoubleBounded)learnerSettingsModels.createParameter(LearnerParameter.GLOBAL_LEARNING_RATE),
            "Global Learning Rate", DEFAULT_WIDTH));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Global Drop Out Rate");
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_GLOBAL_DROP_OUT),
            "Use Global Drop Out Rate"));
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelDoubleBounded)learnerSettingsModels.createParameter(LearnerParameter.GLOBAL_DROP_OUT),
            "Global Drop Our Rate", DEFAULT_WIDTH));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Global Weight Initialization");
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(
            (SettingsModelBoolean)learnerSettingsModels.createParameter(LearnerParameter.USE_GLOBAL_WEIGHT_INIT),
            "Use Global Weight Initialization Strategy"));
        addDialogComponent(new DialogComponentStringSelection(
            (SettingsModelString)learnerSettingsModels.createParameter(LearnerParameter.GLOBAL_WEIGHT_INIT),
            "Global Weight Initialization Strategy", EnumUtils.getStringCollectionFromToString(WeightInit.values())));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewTab("Data Parameters");
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelIntegerBounded)dataSettingsModels.createParameter(DataParameter.BATCH_SIZE), "Batch Size",
            DEFAULT_WIDTH));
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelIntegerBounded)dataSettingsModels.createParameter(DataParameter.EPOCHS), "Epochs", DEFAULT_WIDTH));
        setHorizontalPlacement(false);
        addDialogComponent(new DialogComponentString(
            (SettingsModelString)dataSettingsModels.createParameter(DataParameter.IMAGE_SIZE), "Size of Input Image"));

        createNewTab("Output Layer Parameters");
        addDialogComponent(new DialogComponentNumberEdit(numberOfOutputs, "Number of Output Units", DEFAULT_WIDTH));
        addDialogComponent(new DialogComponentNumberEdit(
            (SettingsModelDoubleBounded)layerSettingsModels.createParameter(LayerParameter.LEARNING_RATE),
            "Learning Rate", DEFAULT_WIDTH));
        addDialogComponent(new DialogComponentStringSelection(
            (SettingsModelString)layerSettingsModels.createParameter(LayerParameter.WEIGHT_INIT),
            "Weight Initialization Strategy", EnumUtils.getStringCollectionFromToString(WeightInit.values())));
        addDialogComponent(new DialogComponentStringSelection(
            (SettingsModelString)layerSettingsModels.createParameter(LayerParameter.LOSS_FUNCTION), "Loss Function",
            EnumUtils.getStringCollectionFromToString(DL4JLossFunction.values())));
        addDialogComponent(new DialogComponentStringSelection(
            (SettingsModelString)layerSettingsModels.createParameter(LayerParameter.ACTIVATION), "Activation Function",
            EnumUtils.getStringCollectionFromToString(DL4JActivationFunction.values())));

        createNewTab("Column Selection");
        addDialogComponent(
            new DialogComponentColumnNameSelection(labelColumnSettings, "Label Column", 1, false, NominalValue.class));
        addDialogComponent(new DialogComponentColumnFilter(columnSelectionSettings, 1, true, getAllowedTypes()));
    }

    /**
     * Retrieve all convertible classes from the DL4JConverterRegistry.
     *
     * @return array of all allowed types that can be converted to double[]
     */
    private Class<? extends DataValue>[] getAllowedTypes() {
        return DL4JConverterRegistry.getInstance().getDataValueClassesForDestinationType(double[].class);
    }
}
