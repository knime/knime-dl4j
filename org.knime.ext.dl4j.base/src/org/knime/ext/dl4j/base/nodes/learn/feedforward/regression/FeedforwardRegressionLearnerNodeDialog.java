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
package org.knime.ext.dl4j.base.nodes.learn.feedforward.regression;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.ext.dl4j.base.nodes.learn.dialog.DefaultLearnerNodeDialogPane;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.BiasParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.DataParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.DropOutParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.GradientNormalizationParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.LearningRateParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.OutputLayerParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.RegressionColumnSelectionComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.RegularizationParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.SeedParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.TrainingMethodParametersComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.UpdaterParameterComponentGroup;
import org.knime.ext.dl4j.base.nodes.learn.dialog.component.group.WeightInitParameterComponentGroup;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels2;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels2;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels2;

/**
 * <code>NodeDialog</code> for the "DL4JLearner" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class FeedforwardRegressionLearnerNodeDialog extends DefaultLearnerNodeDialogPane {

    /**
     * New pane for configuring the DL4JLearner node.
     */
    protected FeedforwardRegressionLearnerNodeDialog() {
        final LearnerParameterSettingsModels2 learnerSettings = new LearnerParameterSettingsModels2();
        final DataParameterSettingsModels2 dataSettings = new DataParameterSettingsModels2();
        final LayerParameterSettingsModels2 layerSettings = new LayerParameterSettingsModels2();

        setDefaultTabTitle("Learning Parameter");

        TrainingMethodParametersComponentGroup trainingComp =
            new TrainingMethodParametersComponentGroup(learnerSettings, false);
        addDialogComponentGroupWithBorder(trainingComp, "Training Method");

        UpdaterParameterComponentGroup updaterComp = new UpdaterParameterComponentGroup(learnerSettings);
        addDialogComponentGroupWithBorder(updaterComp, "Updater");

        SeedParameterComponentGroup seedComp = new SeedParameterComponentGroup(learnerSettings);
        addDialogComponentGroupWithBorder(seedComp, "Random Seed");

        RegularizationParameterComponentGroup regularizationComp =
            new RegularizationParameterComponentGroup(learnerSettings);
        addDialogComponentGroupWithBorder(regularizationComp, "Regularization");

        GradientNormalizationParameterComponentGroup gradientNorComp =
            new GradientNormalizationParameterComponentGroup(learnerSettings);
        addDialogComponentGroupWithBorder(gradientNorComp, "Gradient Normalization");

        createNewTab("Global Parameter");

        LearningRateParameterComponentGroup learningRateComp = new LearningRateParameterComponentGroup(learnerSettings);
        addDialogComponentGroupWithBorder(learningRateComp, "Global Learning Rate");

        DropOutParameterComponentGroup dropOutComp = new DropOutParameterComponentGroup(learnerSettings);
        addDialogComponentGroupWithBorder(dropOutComp, "Global Drop-Out Rate");

        WeightInitParameterComponentGroup weightInitComp = new WeightInitParameterComponentGroup(learnerSettings);
        addDialogComponentGroupWithBorder(weightInitComp, "Global Weight Initialization Strategy");

        BiasParameterComponentGroup biasComp = new BiasParameterComponentGroup(learnerSettings);
        addDialogComponentGroupWithBorder(biasComp, "Global Bias");

        createNewTab("Data Parameter");

        DataParameterComponentGroup dataComp = new DataParameterComponentGroup(dataSettings, true);
        addDialogComponentGroup(dataComp);

        createNewTab("Output Layer Parameter");

        OutputLayerParameterComponentGroup outputComp =
            new OutputLayerParameterComponentGroup(layerSettings, false, false);
        addDialogComponentGroup(outputComp);

        createNewTab("Column Selection");

        RegressionColumnSelectionComponentGroup columnSelectionComp =
            new RegressionColumnSelectionComponentGroup(dataSettings, 1);
        addDialogComponentGroup(columnSelectionComp);
    }
}
