/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   30.03.2017 (David Kolb): created
 */
package org.knime.ext.dl4j.base.util;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.deeplearning4j.nn.conf.layers.BaseLayer;
import org.deeplearning4j.nn.conf.layers.Layer;
import org.knime.ext.dl4j.base.settings.enumerate.dl4j.DL4JActivationFunction;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.shade.jackson.core.JsonProcessingException;
import org.nd4j.shade.jackson.databind.JsonNode;
import org.nd4j.shade.jackson.databind.ObjectMapper;

/**
 * Utility class providing methods to support backward compatibility for different DL4J versions.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class DL4JVersionUtils {

    private DL4JVersionUtils() {
        // Utility class
    }

    /**
     * Determines the {@link Activation} of the specified layer in DL4J json format. Either if the json does not contain
     * the expected value "activationFunction" or the parsed value does not match any {@link DL4JActivationFunction} the
     * returned Optional will be empty.
     *
     * @param layerAsJson json representation of the layer to parse
     * @return Optional of the parsed {@link Activation}
     * @throws JsonProcessingException
     * @throws IOException
     */
    public static Optional<Activation> parseLayerActivationFromJson(final String layerAsJson)
        throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode layerJsonNode = mapper.readTree(layerAsJson);

        String asString;
        try {
            asString = layerJsonNode.findValue("layer").findValue("activationFunction").asText();
        } catch (NullPointerException e) {
            // field "activationFunction" not present in json
            return Optional.empty();
        }

        Activation activation = null;
        if (asString != null && !asString.equals("null")) {
            activation = DL4JActivationFunction.valueOf(asString).getDL4JValue();
        }

        return Optional.ofNullable(activation);
    }

    /**
     * Filters all layers which can't be cast to {@link BaseLayer} and returns the filtered list.
     *
     * @param layers the list of layers to filter
     * @return the list containing only base layers
     */
    public static List<BaseLayer> filterBaseLayers(final List<Layer> layers) {
        return layers.stream().filter(layer -> (layer instanceof BaseLayer)).map(layer -> ((BaseLayer)layer))
            .collect(Collectors.toList());
    }

}
