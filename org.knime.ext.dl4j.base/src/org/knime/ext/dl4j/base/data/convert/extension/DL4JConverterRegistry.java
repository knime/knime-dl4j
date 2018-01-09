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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.NodeLogger;

/**
 * Service which contains all registered {@link DL4JConverter BaseDL4JConverter}s, that convert from a KNIME type to a
 * Java type.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class DL4JConverterRegistry {
    private final static NodeLogger LOGGER = NodeLogger.getLogger(DL4JConverterRegistry.class);

    private static final DL4JConverterRegistry INSTANCE = new DL4JConverterRegistry();

    private static final String EXTENSION_POINT_ID = "org.knime.ext.dl4j.base.DL4JConverter";

    /* DL4JConverters stored by identifier */
    private final Map<String, DL4JConverter<?, ?>> m_identifierToConverter = new HashMap<>();

    /* DL4JConverters stored by source type they convert from */
    private final Map<Class<?>, Set<DL4JConverter<?, ?>>> m_destinationToConverters = new HashMap<>();

    private DL4JConverterRegistry() {
        for (final IConfigurationElement ce : Platform.getExtensionRegistry()
            .getConfigurationElementsFor(EXTENSION_POINT_ID)) {
            LOGGER.debug("Found DL4JConverter extension: " + ce.getDeclaringExtension());

            try {
                final Object extension = ce.createExecutableExtension("converterClass");

                if (extension instanceof DL4JConverter) {
                    final DL4JConverter<?, ?> converter = (DL4JConverter<?, ?>)extension;
                    register(converter);
                } else {
                    // object was not an instance of ConverterProvider
                    LOGGER.error("Extension \"" + ce.getDeclaringExtension()
                        + "\" is invalid: converterClass does not implement " + DL4JConverter.class.getName());
                }

            } catch (final CoreException e) {
                LOGGER.error("Error while loading extension \"" + ce.getDeclaringExtension() + "\": " + e.getMessage(),
                    e);
            }
        }
    }

    /**
     * Get the converter that converts from the specified {@link DataType} to the specified type. If there are multiple
     * suitable converters, the converter with the highest priority (meaning the greatest priority value) is returned.
     * The priority is specified in the specific converter implementation. If there are several converters with the
     * highest priority the first one is returned.
     *
     * @param from the source type
     * @param to the destination type
     * @return the converter that converts from source to destination
     */
    @SuppressWarnings("unchecked")
    public <D> Optional<DL4JConverter<DataValue, D>> getConverter(final DataType from, final Class<D> to) {
        List<DL4JConverter<DataValue, D>> sourceCompatibleConverters = new ArrayList<>();

        for (Entry<String, DL4JConverter<?, ?>> e : m_identifierToConverter.entrySet()) {
            DL4JConverter<DataValue, ?> converter = (DL4JConverter<DataValue, ?>)e.getValue();
            Class<DataValue> source = converter.getSource();
            Class<?> destination = converter.getDestination();

            if (from.isCompatible(source) && to.equals(destination)) {
                sourceCompatibleConverters.add((DL4JConverter<DataValue, D>)converter);
            }
        }
        if (sourceCompatibleConverters.isEmpty()) {
            return Optional.empty();
        } else if (sourceCompatibleConverters.size() == 1) {
            return Optional.of(sourceCompatibleConverters.get(0));
        } else {
            return Optional.of(getConverterWithHighestPriority(sourceCompatibleConverters, from));
        }
    }

    /**
     * Get the converter with the highest priority from the list of converters. All converters in the list must be
     * compatible with the source type. If a converter is not compatible it will be skipped and a coding error is
     * logged. If there are several converters with the highest priority the first one is returned.
     *
     * @param converters list of converters whose source type is compatible with the specified source type
     * @param source the source type to use for compatibility check
     * @return the converter with the highest priority
     */
    private <D> DL4JConverter<DataValue, D>
        getConverterWithHighestPriority(final List<DL4JConverter<DataValue, D>> converters, final DataType source) {

        DL4JConverter<DataValue, D> highestPriorityConverter = null;

        for (DL4JConverter<DataValue, D> converter : converters) {
            // all converters in the specified list need to be compatible with the source type
            if (!source.isCompatible(converter.getSource())) {
                LOGGER.coding(new IllegalArgumentException(
                    "All converters in the list need to be compatible withe the specified source DataType!"));
                continue;
            }
            if (highestPriorityConverter == null) {
                highestPriorityConverter = converter;
            } else {
                if (converter.getPriority() > highestPriorityConverter.getPriority()) {
                    highestPriorityConverter = converter;
                }
            }
        }

        return highestPriorityConverter;
    }

    /**
     * Gets the list of KNIME types from which registered converters can convert to the specified destination type.
     *
     * @param destType the type to get the KNIME types for
     * @return list of KNIME types that can be converted from
     */
    @SuppressWarnings("unchecked")
    public <D> Class<? extends DataValue>[] getDataValueClassesForDestinationType(final Class<D> destType) {
        List<Class<? extends DataValue>> classes = new ArrayList<>();
        m_destinationToConverters.get(destType).forEach(converter -> classes.add(converter.getSource()));

        return (Class<? extends DataValue>[])classes.toArray(new Class<?>[classes.size()]);
    }

    /**
     * Registers the specified converter in this registry. The identifiers of registered converters need to be unique.
     *
     * @param converter the converter to register
     */
    public void register(final DL4JConverter<?, ?> converter) {
        if (converter == null) {
            throw new IllegalArgumentException("Converter must not be null");
        }

        final Class<?> destination = converter.getDestination();
        Set<DL4JConverter<?, ?>> byDestination = m_destinationToConverters.get(destination);
        if (byDestination == null) {
            byDestination = new HashSet<DL4JConverter<?, ?>>();
            m_destinationToConverters.put(destination, byDestination);
        }
        byDestination.add(converter);

        DL4JConverter<?, ?> previous = m_identifierToConverter.put(converter.getIdentifier(), converter);
        if (previous != null) {
            LOGGER.coding("DL4JConverter identifier is not unique (" + converter.getIdentifier() + ")");
        }
    }

    /**
     * Returns the singleton instance of this registry.
     *
     * @return the DL4JRegistry singleton
     */
    public static DL4JConverterRegistry getInstance() {
        return INSTANCE;
    }
}
