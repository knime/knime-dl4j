/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   10.11.2016 (David Kolb): created
 */
package org.knime.ext.dl4j.base.data.convert.extension;

import org.knime.core.data.DataValue;

/**
 * Interface for DL4JConverters that convert from a KNIME type to a Java type. Do <em>not</em> use this iInterface to
 * create converters. Extend from {@link BaseDL4JConverter} instead.
 *
 * @author David Kolb, KNIME.com GmbH
 * @param <S> the KNIME type
 * @param <D> the Java type
 * @noimplement Do not implement this interface , extend from {@link BaseDL4JConverter} instead.
 */
public interface DL4JConverter<S extends DataValue, D> {
    /**
     * Convert <code>source</code> into an instance of type <D>.
     *
     * @param source data value to convert
     * @return the converted object.
     * @throws Exception When something went wrong during conversion
     */
    D convert(S source) throws Exception;

    /**
     * Returns the destination Java type this class converts to.
     *
     * @return a Java type
     */
    Class<D> getDestination();

    /**
     * Returns the source KNIME data types that this converts accepts.
     *
     * @return a KNIME data type
     */
    Class<S> getSource();

    /**
     * Returns a unique identifier for this converter.
     *
     * @return a unique identifier
     */
    String getIdentifier();

    /**
     * Returns the priority of this converter. Larger values increase the priority of this converter in case several
     * converters are available for the requested conversion.
     *
     * @return a priority
     */
    int getPriority();
}
