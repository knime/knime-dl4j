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
package org.knime.ext.dl4j.base.util;

import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;

/**
 * Utility class for parameter validation and conversion of
 * Deeplearning4J Integration nodes.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class ParameterUtils {

    /** regex pattern for expected format of image size parameter */
    private final static String imageSizePattern =
            "\\d+,\\d+,\\d+";
    /** regex pattern for expected format of kernel parameter */
    private final static String strideKernelSizePattern =
            "\\d+,\\d+";
    /** regex pattern for expected format of a string representation of a map.
     *  e.g. momentum after parameter */
    private final static String mapAsStringPattern =
            "(\\d+:\\d+\\.\\d+)?(\\d+:\\d+\\.\\d+,)*(\\d+:\\d+\\.\\d+)";

    private ParameterUtils(){
        // Utility class
    }

    /**
     * Check if string of stride parameter matches corresponding regex
     *
     * @param stride the string to check
     * @throws InvalidSettingsException if string doesn't match regex
     */
    public static void validateStrideParameter(final String stride)
            throws InvalidSettingsException{
        if(!stride.matches(strideKernelSizePattern)){
            throw new InvalidSettingsException("string for parameter Stride is incorrect. "
                    + "Has to be two Integers separated by a comma");
        }
    }

    /**
     * Check if string of image size parameter matches corresponding regex
     *
     * @param stride the string to check
     * @throws InvalidSettingsException if string doesn't match regex
     */
    public static void validateImageSizeParameter(final String imageSize, final boolean isConv)
            throws InvalidSettingsException{
        if(isConv){
            if(imageSize.equals(DataParameter.DEFAULT_IMAGE_SIZE)){
                throw new InvalidSettingsException("Image size needs to be set for convolutional"
                        + " networks. Set image size in learner dialog -> Data Parameters.");
            }
            if(!imageSize.matches(imageSizePattern)){
                throw new InvalidSettingsException("string for image size is incorrect. "
                        + "Has to be three Integers separated by a comma (x-size,y-size,channels)");
            }
        }
    }

    /**
     * Check if string of kernel size parameter matches corresponding regex
     *
     * @param stride the string to check
     * @throws InvalidSettingsException if string doesn't match regex
     */
    public static void validateKernelSizeParameter(final String kernel)
            throws InvalidSettingsException{
        if(!kernel.matches(strideKernelSizePattern)){
            throw new InvalidSettingsException("string for parameter Kernel Size is incorrect. "
                    + "Has to be two Integers separated by a comma");
        }
    }

    /**
     * Check if string of momentum after parameter matches corresponding regex
     *
     * @param stride the string to check
     * @throws InvalidSettingsException if string doesn't match regex
     */
    public static void validateMomentumAfterParameter(final String momentumAfter)
            throws InvalidSettingsException{
        if(momentumAfter.isEmpty()){
            return;
        }
        if(!momentumAfter.matches(mapAsStringPattern)){
            throw new InvalidSettingsException("string for parameter Momentum After is incorrect. "
                    + "Has to be list of 'Integer:Double' separated by a comma");
        }
    }

    /**
     * Converts a String representation of a number list to an array of int. The numbers in the
     * String need to be separated by a comma.
     * e.g. '1,2' or '132,3'
     *
     * @param intsAsString numbers separated by commas
     * @return array of numbers contained in the string
     */
    public static int[] convertIntsAsStringToInts(final String intsAsString){
        final String[] split = intsAsString.split(",");
        final int[] ints = new int[split.length];
        int i = 0;
        for(final String s : split){
            ints[i] = new Integer(s);
            i++;
        }
        return ints;
    }


    /**
     * Converts a string array to an int array. Assumes that each String of the array contains
     * one number. Every string needs to be in the correct number format.
     *
     * @param strings array of strings in correct number format
     * @return array of ints
     */
    public static int[] convertStringsToInts(final String[] strings){
        final int[] ints = new int[strings.length];
        int i = 0;
        for(final String s : strings){
            ints[i] = new Integer(s);
            i++;
        }
        return ints;
    }


    /**
     * Converts a string representation of a map to a actual {@link Map}.
     * The string need to be in the following format:
     * 'Integer:Double' separated by commas.
     * e.g. '1:0.2,2:0.9'
     *
     * @param mapAsString the string to parse
     * @return HashMap with the Integer values as keys and Double
     * values as values
     */
    public static Map<Integer,Double> convertStringToMap(final String mapAsString){
        if(mapAsString.isEmpty()){
            return new HashMap<>();
        }
        final String[] keysValues = mapAsString.split(",");

        final Integer[] keys = new Integer[keysValues.length];
        final Double[] values = new Double[keysValues.length];

        int i = 0;
        for(final String keyValue : keysValues){
            final String[] kV = keyValue.split(":");
            keys[i] = new Integer(kV[0]);
            values[i] = new Double(kV[1]);
            i++;
        }

        final Map<Integer,Double> m = new HashMap<>();

        for(int j = 0 ; j < keys.length ; j++){
            m.put(keys[j], values[j]);
        }

        return m;
    }
}
