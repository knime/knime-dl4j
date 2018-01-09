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
 * KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
package org.knime.ext.dl4j.base.exception;

/**
 * This exception indicates that DL4J ran out of memory, e.g. if an {@link OutOfMemoryError} is thrown by DL4J.
 *
 * @author David Kolb, KNIME GmbH, Konstanz, Germany
 */
public class DL4JOutOfMemoryException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@link DL4JOutOfMemoryException} with the specified detail message. Use a helpful message here as
     * it will be displayed to the user, and it is the only hint ones understands to actual the problem.
     *
     * @param s the detail message.
     */
    public DL4JOutOfMemoryException(final String s) {
        super(s);
    }

    /**
     * Constructs an {@link DL4JOutOfMemoryException} with the specified cause.
     *
     * @param cause the original cause of the execption
     */
    public DL4JOutOfMemoryException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an {@link DL4JOutOfMemoryException} with the specified detail message and a cause. Use a helpful
     * message here as it will be displayed to the user, and it is the only hint ones understands to actual the problem.
     *
     * @param msg the detail message
     * @param cause the root cause
     */
    public DL4JOutOfMemoryException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
