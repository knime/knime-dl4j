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

import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 * Wrapper for {@link LossFunction} for better String representation
 * of values. Also hides unsupported LossFunction {@link LossFunction#CUSTOM}
 * 
 * @author David Kolb, KNIME.com GmbH
 */
public enum DL4JLossFunction {
	/** Mean Squared Error: Linear Regression */
	MSE(LossFunction.MSE),
	/** Exponential log likelihood: Poisson Regression */
    EXPLL(LossFunction.EXPLL),
    /** Cross Entropy: Binary Classification */
    XENT(LossFunction.XENT),
    /** Multiclass Cross Entropy */
    MCXENT(LossFunction.MCXENT),
    /** RMSE Cross Entropy */
    RMSE_XENT(LossFunction.RMSE_XENT),
    /** Squared Loss */
    SQUARED_LOSS(LossFunction.SQUARED_LOSS),
    /** Reconstruction Cross Entropy */
    RECONSTRUCTION_CROSSENTROPY(LossFunction.RECONSTRUCTION_CROSSENTROPY),
    /** Negative Log Likelihood */
    NEGATIVELOGLIKELIHOOD(LossFunction.NEGATIVELOGLIKELIHOOD);
	
	/** the corresponding dl4j value of this enum */
	private LossFunction m_DL4JValue;
	
	private DL4JLossFunction(LossFunction loss) {
		m_DL4JValue = loss;
	}
	
	/**
     * Converts string representation of this enum back to this enum
     * 
     * @param toString the value from toString of this enum
     * @return this enum corresponding to toString
     */
	public static DL4JLossFunction fromToString(String toString){
        for(DL4JLossFunction e : DL4JLossFunction.values()){
            if(e.toString().equals(toString)){
                return e;
            }
        }
        return null;
	}

	/**
	 * Get the in dl4j usable {@link LossFunction} corresponding to this enum
	 * 
	 * @return dl4j usable {@link LossFunction}
	 */
	public LossFunction getDL4JValue(){
	    return m_DL4JValue;
	} 
	
	public String toString(){
		switch (this) {		
		case EXPLL:
			return "Exponential Log Likelihood";
		case MCXENT:
			return "Multiclass Cross Entropy";
		case MSE:
			return "Mean Squared Error";
		case NEGATIVELOGLIKELIHOOD:
			return "Negative Log Likelihood";
		case RECONSTRUCTION_CROSSENTROPY:
			return "Reconstruction Cross Entropy";
		case RMSE_XENT:
			return "RMSE Cross Entropy";
		case SQUARED_LOSS:
			return "Squared Error";
		case XENT:
			return "Cross Entropy";
		default:
			return super.toString();
		}
	}
}
