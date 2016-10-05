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
package org.knime.ext.dl4j.base.mln;

import java.util.List;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels2;

/**
 * Factory class for creating {@link MultiLayerNetwork}s specific for convolutional networks. Uses additional
 * information about image dimensionality.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class ConvMultiLayerNetFactory2 extends MultiLayerNetFactory2 {

    /** Image height in pixel. */
    private int m_height;

    /** Image width in pixel. */
    private int m_width;

    /** Number of channels of image. */
    private int m_channels;

    /**
     * Constructor for class ConvMultiLayerNetFactory specifying image dimensionality of images the created networks of
     * this factory should be used for.
     *
     * @param height the height of expected images in pixel
     * @param width the width of expected images in pixel
     * @param channels the number of channels of expected images
     */
    public ConvMultiLayerNetFactory2(final int height, final int width, final int channels) {
        super(0);
        m_channels = channels;
        m_height = height;
        m_width = width;
    }

    @Override
    protected MultiLayerNetwork createMlnWithLearnerParameters(final List<Layer> layers,
        final LearnerParameterSettingsModels2 learnerParameters) {
        final NeuralNetConfiguration.ListBuilder listBuilder =
            createListBuilderWithLearnerParameters(layers, learnerParameters);
        listBuilder.setInputType(InputType.convolutionalFlat(m_height, m_width, m_channels));

        final MultiLayerConfiguration layerConf = listBuilder.build();
        final MultiLayerNetwork mln = new MultiLayerNetwork(layerConf);
        mln.init();
        return mln;
    }

    @Override
    protected MultiLayerNetwork createMlnWithoutLearnerParameters(final List<Layer> layers) {
        final NeuralNetConfiguration.ListBuilder listBuilder = new NeuralNetConfiguration.Builder().list();
        listBuilder.setInputType(InputType.convolutionalFlat(m_height, m_width, m_channels));

        int currentLayerIndex = 0;
        for (final Layer layer : layers) {
            listBuilder.layer(currentLayerIndex, layer);
            currentLayerIndex++;
        }

        final MultiLayerConfiguration layerConf = listBuilder.build();
        final MultiLayerNetwork mln = new MultiLayerNetwork(layerConf);
        mln.init();
        return mln;
    }

    /**
     * Set height of images the network should expect.
     *
     * @param height in pixel
     */
    public void setImageHeight(final int height) {
        m_height = height;
    }

    /**
     * Set width of images the network should expect.
     *
     * @param width in pixel
     */
    public void setImageWidth(final int width) {
        m_width = width;
    }

    /**
     * Set the number of channels of images the network should expect.
     *
     * @param numberOfChannels
     */
    public void setImageChannels(final int numberOfChannels) {
        m_channels = numberOfChannels;
    }
}
