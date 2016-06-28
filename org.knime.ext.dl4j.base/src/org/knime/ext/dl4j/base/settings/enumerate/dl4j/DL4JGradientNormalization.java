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
package org.knime.ext.dl4j.base.settings.enumerate.dl4j;

import org.deeplearning4j.nn.conf.GradientNormalization;

/**
 * Wrapper for {@link GradientNormalization} for better String representation
 * of values.
 * 
 * @author David Kolb, KNIME.com GmbH
 */
public enum DL4JGradientNormalization {
	/** Rescale gradients by dividing by the L2 norm of all gradients for the layer */
	RenormalizeL2PerLayer(GradientNormalization.RenormalizeL2PerLayer),
	/** 
	 * <p> Rescale gradients by dividing by the L2 norm of the gradients, separately for
	 * each type of parameter within the layer.<br>
	 * This differs from RenormalizeL2PerLayer in that here, each parameter type (weight, bias etc) is normalized separately.<br>
	 * For example, in a MLP/FeedForward network (where G is the gradient vector), the output is as follows:
	 * <ul style="list-style-type:none">
	 *     <li>GOut_weight = G_weight / l2(G_weight)</li>
	 *     <li>GOut_bias = G_bias / l2(G_bias)</li>
	 * </ul>
	 * </p> 
	 * */
    RenormalizeL2PerParamType(GradientNormalization.RenormalizeL2PerParamType),
    /**
    *<p>Clip the gradients on a per-element basis.<br>
    * For each gradient g, set g <- sign(g)*max(maxAllowedValue,|g|).<br>
    * i.e., if a parameter gradient has absolute value greater than the threshold, truncate it.<br>
    * For example, if threshold = 5, then values in range -5&lt;g&lt;5 are unmodified; values &lt;-5 are set
    * to -5; values &gt;5 are set to 5.<br>
    * This was proposed by Mikolov (2012), <i>Statistical Language Models Based on Neural Networks</i> (thesis),
    * <a href="http://www.fit.vutbr.cz/~imikolov/rnnlm/thesis.pdf">http://www.fit.vutbr.cz/~imikolov/rnnlm/thesis.pdf</a>
    * in the context of learning recurrent neural networks.<br>
    * Threshold for clipping can be set in Layer configuration, using gradientNormalizationThreshold(double threshold)
    * </p> 
    * */
    ClipElementWiseAbsoluteValue(GradientNormalization.ClipElementWiseAbsoluteValue),
    /**
     * <p>Conditional renormalization. Somewhat similar to RenormalizeL2PerLayer, this strategy
     * scales the gradients <i>if and only if</i> the L2 norm of the gradients (for entire layer) exceeds a specified
     * threshold. Specifically, if G is gradient vector for the layer, then:
     * <ul style="list-style-type:none">
     *     <li>GOut = G &nbsp;&nbsp;&nbsp; if l2Norm(G) < threshold (i.e., no change) </li>
     *     <li>GOut = threshold * G / l2Norm(G) &nbsp;&nbsp;&nbsp; otherwise </li>
     * </ul>
     * Thus, the l2 norm of the scaled gradients will not exceed the specified threshold, though may be smaller than it<br>
     * See: Pascanu, Mikolov, Bengio (2012), <i>On the difficulty of training Recurrent Neural Networks</i>,
     * <a href="http://arxiv.org/abs/1211.5063">http://arxiv.org/abs/1211.5063</a><br>
     * Threshold for clipping can be set in Layer configuration, using gradientNormalizationThreshold(double threshold)
     * </p>
     */
    ClipL2PerLayer(GradientNormalization.ClipL2PerLayer),
    /**
     * <p>Conditional renormalization. Very similar to ClipL2PerLayer, however instead of clipping
     * per layer, do clipping on each parameter type separately.<br>
     * For example in a recurrent neural network, input weight gradients, recurrent weight gradients and bias gradient are all
     * clipped separately. Thus if one set of gradients are very large, these may be clipped while leaving the other gradients
     * unmodified.<br>
     * Threshold for clipping can be set in Layer configuration, using gradientNormalizationThreshold(double threshold)</p>
     */
    ClipL2PerParamType(GradientNormalization.ClipL2PerParamType),;
	
	/** the corresponding dl4j value of this enum */
	private GradientNormalization m_DL4JValue;
	
	private DL4JGradientNormalization(GradientNormalization norm) {
		m_DL4JValue = norm;
	}
	
	/**
     * Converts string representation of this enum back to this enum
     * 
     * @param toString the value from toString of this enum
     * @return this enum corresponding to toString
     */
	public static DL4JGradientNormalization fromToString(String toString){
        for(DL4JGradientNormalization e : DL4JGradientNormalization.values()){
            if(e.toString().equals(toString)){
                return e;
            }
        }
        return null;
    }


	/**
	 * Get the in dl4j usable {@link GradientNormalization} corresponding to this enum
	 * 
	 * @return dl4j usable {@link GradientNormalization}
	 */
    public GradientNormalization getDL4JValue(){
        return m_DL4JValue;
    } 
	
	public String toString(){
		switch (this) {
		case ClipElementWiseAbsoluteValue:
			return "Clip Element Wise Absolute Value";
		case ClipL2PerLayer:
			return "Clip L2 Per Layer";
		case ClipL2PerParamType:
			return "Clip L2 Per Param Type";
		case RenormalizeL2PerLayer:
			return "Renormalize L2 Per Layer";
		case RenormalizeL2PerParamType:
			return "Renormalize L2 Per Param Type";
		default:
			return super.toString();
		}
	}
}
