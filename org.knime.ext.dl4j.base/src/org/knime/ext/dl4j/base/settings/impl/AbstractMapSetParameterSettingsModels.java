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
package org.knime.ext.dl4j.base.settings.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.IParameterSettingsModels;

/**
 *
 * Abstract implementation of {@link IParameterSettingsModels} which stores all {@link SettingsModel}s in a
 * {@link HashMap}. For each key only one model will be stored. Settings with standard data types (boolean, String,
 * Integer, Double) can be retrieved directly using the corresponding enumeration as identifier. For other types, the
 * {@link SettingsModel} can be retrieved.
 *
 * @author David Kolb, KNIME.com GmbH
 * @param <E> the enum identifying the parameters
 */
public abstract class AbstractMapSetParameterSettingsModels<E extends Enum<?>> implements IParameterSettingsModels<E> {

    private final Map<E, SettingsModel> m_settingsModels = new HashMap<E, SettingsModel>();

    /**
     * Returns .
     *
     * @param key the enum identifying the {@link SettingsModel}
     * @return the {@link SettingsModel} corresponding to the specified enum
     */
    public SettingsModel getParameter(final E key) {
        return m_settingsModels.get(key);
    }

    /**
     * Puts the specified {@link SettingsModel}to the map if it does not already contain an entry with that key.
     *
     * @param key
     * @param model
     */
    private void addToSet(final E key, final SettingsModel model) {
        if (!m_settingsModels.containsKey(key)) {
            m_settingsModels.put(key, model);
        }
    }

    @Override
    public List<SettingsModel> getAllInitializedSettings() {
        return new ArrayList<SettingsModel>(m_settingsModels.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParameter(final E enumerate) throws IllegalArgumentException {
        addToSet(enumerate, createParameter(enumerate));
    }

    /**
     * Returns the boolean contained in the {@link SettingsModel} identified by the specified enum. Assumes that the
     * SettingsModel is of type boolean. If not or if no settings are available, a {@link IllegalArgumentException} is
     * thrown.
     *
     * @param enumerate the enum identifying the settings
     * @return the value contained in the settings
     */
    public boolean getBoolean(final E enumerate) {
        try {
            SettingsModelBoolean settings = (SettingsModelBoolean)m_settingsModels.get(enumerate);
            return settings.getBooleanValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                "Settings corresponding to " + enumerate + " available but not of type boolean.", e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("No boolean settings corresponding to " + enumerate + " available.", e);
        }
    }

    /**
     * Returns the boolean contained in the {@link SettingsModel} identified by the specified enum or the specified
     * default if no such settings are available.
     *
     * @param enumerate the enum identifying the settings
     * @param def the default value
     * @return the value contained in the settings or the specified default
     */
    public boolean getBoolean(final E enumerate, final boolean def) {
        try {
            return getBoolean(enumerate);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    /**
     * Returns the String contained in the {@link SettingsModel} identified by the specified enum. Assumes that the
     * SettingsModel is of type String. If not or if no settings are available, a {@link IllegalArgumentException} is
     * thrown.
     *
     * @param enumerate the enum identifying the settings
     * @return the value contained in the settings
     */
    public String getString(final E enumerate) {
        try {
            SettingsModelString settings = (SettingsModelString)m_settingsModels.get(enumerate);
            return settings.getStringValue();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                "Settings corresponding to " + enumerate + " available but not of type String.", e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("No String settings corresponding to " + enumerate + " available.", e);
        }
    }

    /**
     * Returns the String contained in the {@link SettingsModel} identified by the specified enum or the specified
     * default if no such settings are available.
     *
     * @param enumerate the enum identifying the settings
     * @param def the default value
     * @return the value contained in the settings or the specified default
     */
    public String getString(final E enumerate, final String def) {
        try {
            return getString(enumerate);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    /**
     * Returns the Integer contained in the {@link SettingsModel} identified by the specified enum. Assumes that the
     * SettingsModel is of type String. If not or if no settings are available, a {@link IllegalArgumentException} is
     * thrown.
     *
     * @param enumerate the enum identifying the settings
     * @return the value contained in the settings
     */
    public Integer getInteger(final E enumerate) {
        try {
            SettingsModel settings = m_settingsModels.get(enumerate);
            if (settings instanceof SettingsModelIntegerBounded) {
                SettingsModelIntegerBounded intSettings = (SettingsModelIntegerBounded)settings;
                return intSettings.getIntValue();
            } else {
                SettingsModelInteger intSettings = (SettingsModelInteger)settings;
                return intSettings.getIntValue();
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                "Settings corresponding to " + enumerate + " available but not of type Integer.", e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("No Integer settings corresponding to " + enumerate + " available.", e);
        }
    }

    /**
     * Returns the Integer contained in the {@link SettingsModel} identified by the specified enum or the specified
     * default if no such settings are available.
     *
     * @param enumerate the enum identifying the settings
     * @param def the default value
     * @return the value contained in the settings or the specified default
     */
    public Integer getInteger(final E enumerate, final Integer def) {
        try {
            return getInteger(enumerate);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    /**
     * Returns the Double contained in the {@link SettingsModel} identified by the specified enum. Assumes that the
     * SettingsModel is of type String. If not or if no settings are available, a {@link IllegalArgumentException} is
     * thrown.
     *
     * @param enumerate the enum identifying the settings
     * @return the value contained in the settings
     */
    public Double getDouble(final E enumerate) {
        try {
            SettingsModel settings = m_settingsModels.get(enumerate);
            if (settings instanceof SettingsModelDoubleBounded) {
                SettingsModelDoubleBounded doubleSettings = (SettingsModelDoubleBounded)settings;
                return doubleSettings.getDoubleValue();
            } else {
                SettingsModelDouble doubleSettings = (SettingsModelDouble)settings;
                return doubleSettings.getDoubleValue();
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                "Settings corresponding to " + enumerate + " available but not of type Double.", e);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("No Double settings corresponding to " + enumerate + " available.", e);
        }
    }

    /**
     * Returns the Double contained in the {@link SettingsModel} identified by the specified enum or the specified
     * default if no such settings are available.
     *
     * @param enumerate the enum identifying the settings
     * @param def the default value
     * @return the value contained in the settings or the specified default
     */
    public Double getDouble(final E enumerate, final Double def) {
        try {
            return getDouble(enumerate);
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}
