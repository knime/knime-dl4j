/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 * Abstract implementation of the {@link DL4JConverter} interface. This class handles uniqueness of the converter
 * identifier. The default converter priority is 500. If the number is smaller the priority will be smaller for this
 * converter. Use this class to create new converters. In order create a new converter extend this class and specify the
 * KNIME source type, the Java destination type, as well as the converter priority in the constructor. Then supply the
 * desired implementation of the {@link DL4JConverter#convert(DataValue)} method.
 *
 * @author David Kolb, KNIME.com GmbH
 * @param <S> the KNIME type
 * @param <D> the Java type
 */
public abstract class BaseDL4JConverter<S extends DataValue, D> implements DL4JConverter<S, D> {
    /** Default priority value: {@value}. */
    public static final int DEFAULT_PRIORITY = 500;

    private final Class<S> m_sourceClass;

    private final Class<D> m_destinationClass;

    private final int m_priority;

    /**
     * Constructor for class BaseDL4JConverter specifying the source and destination types as well as the priority value
     * of the converter.
     *
     * @param from the KNIME source type
     * @param to the Java destination type
     * @param converterPriority the priority value of this converter
     */
    public BaseDL4JConverter(final Class<S> from, final Class<D> to, final int converterPriority) {
        m_sourceClass = from;
        m_destinationClass = to;
        m_priority = converterPriority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<D> getDestination() {
        return m_destinationClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<S> getSource() {
        return m_sourceClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return getClass().getName() + "(" + m_sourceClass.getSimpleName() + "," + m_destinationClass.getSimpleName()
            + ")";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return m_priority;
    }
}
