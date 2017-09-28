/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
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
package org.knime.ext.dl4j.libs.prefs;


import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.knime.ext.dl4j.libs.DL4JPluginActivator;

/**
 * Preference page for the KINME Deeplearning4J Integration.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class DL4JPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    /** Key for GPU checkbox. */
    public static final String P_BACKEND_TYPE = "backendType";

    /** Default value if GPU should be used */
    public static boolean DEFAULT_USE_GPU = false;

    /** Key for verbose logging checkbox */
    public static final String P_ENABLE_VERBOSE_LOGGING = "enableVerboseLogging";

    /** Default enable verbose logging value */
    public static boolean DEFAULT_ENABLE_VERBOSE_LOGGING = false;

    /** Key for off heap memory limit number field */
    public static final String P_OFF_HEAP_MEMORY_LIMIT = "offHeapMemoryLimit";

    /** Default DL4J off heap limit */
    public static int DEFAULT_OFF_HEAP_LIMIT = 2000;

    private boolean m_useGPU;

    private boolean m_enableVerbose;

    /** DL4J off heap memory limit in MB */
    private int m_offHeapSize;

    /**
     * Constructor for class DL4JPreferencePage.
     */
    public DL4JPreferencePage() {
        super(GRID);
        setPreferenceStore(DL4JPluginActivator.getDefault().getPreferenceStore());
        setDescription("Preferences for the KNIME Deeplearning4J Integration.");
        m_useGPU = DL4JPluginActivator.getDefault().getPreferenceStore().getBoolean(P_BACKEND_TYPE);
        m_enableVerbose = DL4JPluginActivator.getDefault().getPreferenceStore().getBoolean(P_ENABLE_VERBOSE_LOGGING);
        m_offHeapSize = DL4JPluginActivator.getDefault().getPreferenceStore().getInt(P_OFF_HEAP_MEMORY_LIMIT);
    }

    @Override
    protected void createFieldEditors() {
        addField(new LabelField(getFieldEditorParent(), "By default CPU is used for calculations. For GPU usage you need to have \n"
                + "a CUDA (7.5 or 8.0) compatible Graphics Card and the corresponding CUDA Toolkit \n"
                + "version installed on your system."));
        addField(new BooleanFieldEditor(P_BACKEND_TYPE, "Use GPU for calculations?",
            BooleanFieldEditor.SEPARATE_LABEL, getFieldEditorParent()));

        Label label = new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

        addField(new LabelField(getFieldEditorParent(), "Apart from the java heap space, DL4J additionally uses off-heap memory.\n"
            + "The off-heap is used to store all data needed for network learning and execution.\n"
            + "For further information how to configure the value see: https://deeplearning4j.org/memory"));
        addField(new IntegerFieldEditor(P_OFF_HEAP_MEMORY_LIMIT, "Off Heap Memory Limit (in MB):", getFieldEditorParent()));

        label = new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

        addField(new BooleanFieldEditor(P_ENABLE_VERBOSE_LOGGING, "Enable verbose logging?",
            BooleanFieldEditor.SEPARATE_LABEL, getFieldEditorParent()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performApply() {
        // call super method to not show restart dialog
        super.performOk();
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        checkChanges();
        return result;
    }

    private void checkChanges() {
        boolean currentUseGPU = DL4JPluginActivator.getDefault().getPreferenceStore()
                .getBoolean(P_BACKEND_TYPE);
        boolean useGPUChanged = m_useGPU != currentUseGPU;

        boolean currentEnableVerbose = DL4JPluginActivator.getDefault().getPreferenceStore()
                .getBoolean(P_ENABLE_VERBOSE_LOGGING);
        boolean enableVerboseChanged = m_enableVerbose != currentEnableVerbose;

        int currentOffHeapLimit = DL4JPluginActivator.getDefault().getPreferenceStore()
                .getInt(P_OFF_HEAP_MEMORY_LIMIT);
        boolean offHeapLimitCanged = m_offHeapSize != currentOffHeapLimit;

        if (offHeapLimitCanged || enableVerboseChanged || useGPUChanged) {
            m_offHeapSize = currentOffHeapLimit;
            m_useGPU = currentUseGPU;
            m_useGPU = currentEnableVerbose;
            promptRestartWithMessage("Changes become first available after restarting the workbench.\n"
                    + "Do you want to restart the workbench now?");
        }
    }

    private void promptRestartWithMessage(final String message){
        MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        mb.setText("Restart workbench...");
        mb.setMessage(message);
        if (mb.open() != SWT.YES) {
            return;
        }
        PlatformUI.getWorkbench().restart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }
}
