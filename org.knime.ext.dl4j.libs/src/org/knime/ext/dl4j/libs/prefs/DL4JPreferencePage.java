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
package org.knime.ext.dl4j.libs.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
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

    /**
     * Key use GPU checkbox.
     */
    public static final String P_BACKEND_TYPE = "backendType";

    private boolean m_useGPU;

    /**
     * Constructor for class DL4JPreferencePage.
     */
    public DL4JPreferencePage() {
        super(GRID);
        setPreferenceStore(DL4JPluginActivator.getDefault().getPreferenceStore());
        setDescription("Preferences for the KNIME Deeplearning4J Integration.");
        m_useGPU = DL4JPluginActivator.getDefault().getPreferenceStore().getBoolean(DL4JPreferencePage.P_BACKEND_TYPE);
    }

    @Override
    public void init(final IWorkbench workbench) {
        getPreferenceStore().setDefault(P_BACKEND_TYPE, false);
    }

    @Override
    protected void createFieldEditors() {
        addField(new LabelField(getFieldEditorParent(), "By default CPU is used for calculations. For GPU usage you need to have \n"
                + "a CUDA (7.5 or 8.0) compatible Graphics Card and the corresponding CUDA Toolkit \n"
                + "version installed on your system. A change in this option requires a restart \n"
                + "of KNIME Analytics Platform in order to take effect."));
        addField(new BooleanFieldEditor(P_BACKEND_TYPE, "Use GPU for calculations?",
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
                .getBoolean(DL4JPreferencePage.P_BACKEND_TYPE);
        boolean useGPUChanged = m_useGPU != currentUseGPU;

        if (useGPUChanged) {
            m_useGPU = currentUseGPU;
            MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            mb.setText("Restart workbench...");
            mb.setMessage("Changes of the used backend become first available after restarting the workbench.\n"
                    + "Do you want to restart the workbench now?");
            if (mb.open() != SWT.YES) {
                return;
            }

            PlatformUI.getWorkbench().restart();
        }
    }
}
