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
package org.knime.ext.dl4j.libs;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.log4j.Logger;
import org.bytedeco.javacpp.Pointer;
import org.eclipse.osgi.internal.loader.EquinoxClassLoader;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.internal.loader.classpath.FragmentClasspath;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.ext.dl4j.libs.cuda.CudaNotFoundException;
import org.knime.ext.dl4j.libs.cuda.CudaVersionChecker;
import org.knime.ext.dl4j.libs.cuda.UnsupportedCudaVersionException;
import org.knime.ext.dl4j.libs.prefs.DL4JPreferencePage;
import org.nd4j.linalg.factory.Nd4j;
import org.osgi.framework.BundleContext;

import com.google.common.collect.ImmutableList;

/**
 * Activator choosing between GPU and CPU fragements.
 *
 * @author David Kolb, KNIME.com GmbH
 */
@SuppressWarnings("restriction")
public class DL4JPluginActivator extends AbstractUIPlugin {

    private static final Logger LOGGER = Logger.getLogger(DL4JPluginActivator.class);

    private static DL4JPluginActivator plugin;

    /**
     * Enum identifying the DL4J backend.
     */
    public enum BackendType {
        /** CUDA 8.0. */
        GPU_CUDA8_0("org\\.knime\\.ext\\.dl4j\\.bin\\.[^\\.]+\\.x86_64\\.gpu\\.cuda8\\_0.*", "GPU CUDA Toolkit 8.0"),
        /** CUDA 7.5. */
        GPU_CUDA7_5("org\\.knime\\.ext\\.dl4j\\.bin\\.[^\\.]+\\.x86_64\\.gpu\\.cuda7\\_5.*", "GPU CUDA Toolkit 7.5"),
        /** CPU. */
        CPU("org\\.knime\\.ext\\.dl4j\\.bin\\.[^\\.]+\\.x86_64\\.cpu.*", "CPU");

        /** Convenience list containing all available CUDA versions */
        public static final List<BackendType> ALL_CUDA_VERSIONS = ImmutableList.of(GPU_CUDA7_5, GPU_CUDA8_0);

        private final String m_fragmentRegex;

        private final String m_description;

        private BackendType(final String fragmentRegex, final String description) {
            m_fragmentRegex = fragmentRegex;
            m_description = description;
        }

        @Override
        public String toString() {
            return m_description;
        }

        /**
         * Returns a regualar expression for the corresponding fragment.
         *
         * @return a regular expression
         */
        public String getFragmentRegex() {
            return m_fragmentRegex;
        }
    }

    /**
     * Constructor for class DL4JPluginActivator. Sets the getDefault to this class.
     */
    public DL4JPluginActivator() {
        plugin = this;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        initPreferencePageDefaults();

        //Set off heap limit system properties. See: https://deeplearning4j.org/memory
        //Note: all lower case!
        int offHeapLimit = plugin.getPreferenceStore().getInt(DL4JPreferencePage.P_OFF_HEAP_MEMORY_LIMIT);
        System.setProperty("org.bytedeco.javacpp.maxbytes", offHeapLimit + "M");

        LOGGER.debug("DL4J off-heap sizes: org.bytedeco.javacpp.Pointer.maxBytes(): " + Pointer.maxBytes());
        LOGGER.debug("DL4J off-heap sizes: org.bytedeco.javacpp.Pointer.maxPhysicalBytes(): " + Pointer.maxPhysicalBytes());
        LOGGER.debug("JVM maxMemory: Runtime.getRuntime().maxMemory(): " + Runtime.getRuntime().maxMemory());

        final boolean useGPU = plugin.getPreferenceStore().getBoolean(DL4JPreferencePage.P_BACKEND_TYPE);

        BackendType backendType = BackendType.CPU;
        if (useGPU) {
            try {
                backendType = CudaVersionChecker.getCudaVersion();
            } catch (CudaNotFoundException e) {
                String message = "CUDA seems not to be installed on the system!";
                LOGGER.error(message);
                LOGGER.debug(message, e);
                backendType = BackendType.CPU;
            } catch (UnsupportedCudaVersionException e) {
                String message = "No compatible CUDA version seems to be installed on the system! Supported versions are: "
                        + BackendType.GPU_CUDA7_5.toString() + " and " + BackendType.GPU_CUDA8_0.toString();
                LOGGER.error(message);
                LOGGER.debug(message, e);
                backendType = BackendType.CPU;
            }
        }

        LOGGER.info(backendType + " backend will be used.");

        final EquinoxClassLoader el = (EquinoxClassLoader)getClass().getClassLoader();
        final ClasspathManager manager = el.getClasspathManager();

        // manually remove either cpu or gpu fragment from EquinoxClassLoader
        // cp must contain only one backend at a time
        final Field f = manager.getClass().getDeclaredField("fragments");
        f.setAccessible(true);
        final FragmentClasspath[] frags = (FragmentClasspath[])f.get(manager);
        final FragmentClasspath fragmentOverwrite = findBackendFragment(frags, backendType);
        if (fragmentOverwrite != null) {
            LOGGER.debug("The following backend fragment will be used: "
                + fragmentOverwrite.getGeneration().getBundleFile().getBaseFile().getName());
            f.set(manager, new FragmentClasspath[]{fragmentOverwrite});
        } else {
            throw new Exception("Backend Fragment for: " + backendType + " could not be found.");
        }

        f.setAccessible(false);

        //Since 0.9.1 we are using DL4J workspaces. Therefore, we can reduce the number of GC calls made by DL4J
        if (BackendType.ALL_CUDA_VERSIONS.contains(backendType)) {
            Nd4j.getMemoryManager().setAutoGcWindow(10000);
        }

    }

    /**
     * Searches for a fragment corresponding to the specified {@link BackendType} in the specified array of
     * {@link FragmentClasspath}s. Fragment is searched via base file name that needs to match a static member regex of
     * this class. If no matching fragment can be found null is returned.
     *
     * @param frags the fragments to search
     * @param backend the backend fragment type to search for
     * @return found fragment or null if not found
     */
    private FragmentClasspath findBackendFragment(final FragmentClasspath[] frags, final BackendType backend) {
        for (final FragmentClasspath fcp : frags) {
            final String fragmentFileName = fcp.getGeneration().getBundleFile().getBaseFile().getName();
            if (fragmentFileName.matches(backend.getFragmentRegex())) {
                LOGGER.debug(fragmentFileName + " matches " + backend.getFragmentRegex());
                return fcp;
            } else {
                LOGGER.debug(fragmentFileName + " does not match " + backend.getFragmentRegex());
            }
        }
        return null;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        // nothing to do here
    }

    /**
     * Returns the shared instance.
     *
     * @return Singleton instance of the Plugin
     */
    public static DL4JPluginActivator getDefault() {
        return plugin;
    }

    private void initPreferencePageDefaults() {
        plugin.getPreferenceStore().setDefault(DL4JPreferencePage.P_BACKEND_TYPE, DL4JPreferencePage.DEFAULT_USE_GPU);
        plugin.getPreferenceStore().setDefault(DL4JPreferencePage.P_ENABLE_VERBOSE_LOGGING,
            DL4JPreferencePage.DEFAULT_ENABLE_VERBOSE_LOGGING);
        plugin.getPreferenceStore().setDefault(DL4JPreferencePage.P_OFF_HEAP_MEMORY_LIMIT,
            DL4JPreferencePage.DEFAULT_OFF_HEAP_LIMIT);
    }
}
