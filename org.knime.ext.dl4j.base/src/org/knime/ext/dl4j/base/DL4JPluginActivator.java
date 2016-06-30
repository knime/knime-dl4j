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
package org.knime.ext.dl4j.base;

import java.net.URL;
import java.util.Enumeration;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class DL4JPluginActivator extends AbstractUIPlugin {

	private static DL4JPluginActivator plugin;
	
	private final String GPU_IDENT = "gpu";
	private final String CPU_IDENT = "cpu";
	
	private enum BackendType{
		GPU,
		CPU
	}
	
	public DL4JPluginActivator() {
		plugin = this;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {	
//		boolean useGPU = false;
//		useGPU = plugin.getPreferenceStore().getBoolean(DL4JPreferencePage.P_BACKEND_TYPE);
//		Bundle[] fragments = Platform.getFragments(context.getBundle());
		//fragments[0].uninstall();
		//System.out.println(fragments[0].getState());

		
		
//		Bundle[] bundles = new Bundle[]{fragments[0]};
//		PackageAdmin pckAdmin = context.getService(context.getServiceReference(PackageAdmin.class));
//		pckAdmin.refreshPackages(bundles);
//		pckAdmin.resolveBundles(bundles);

		
		
//		BackendType backendType = BackendType.CPU;
//		if(useGPU){
//			backendType = BackendType.GPU;
//		} 		
//		
//		context.getBundle().uninstall();
//		
//		//search platform fragments for backend_fragments folder, backend fragments need to be 
//		//org.knime.deepelarning.base host fragments containing necessary backend libs
//		Enumeration<URL> fragmentURLs = context.getBundle().findEntries("backend_fragments/","*.jar",true);
//		URL fragmentToLoad = findBackendFragment(fragmentURLs, backendType);
//		if(fragmentToLoad == null){
//			throw new Exception("Fragment containing backend of type: " + backendType + " could not be found.");
//		}		
//		
//		//dynamically install backend fragment depending on user selection on preference page
//		Bundle[] bundles = new Bundle[]{context.installBundle(fragmentToLoad.toExternalForm()), context.getBundle()};
//		PackageAdmin pckAdmin = context.getService(context.getServiceReference(PackageAdmin.class));
//		pckAdmin.refreshPackages(bundles);
//		pckAdmin.resolveBundles(bundles);
	}

	/**
	 * Finds the fragment containing the backend libraries corresponding to the specified {@link BackendType}
	 * form the Enumeration of URL.
	 * 
	 * @param urls URLs to search in
	 * @param bt type of backend to find
	 * @return URL of found fragment or null if fragment wasn't found
	 */
	private URL findBackendFragment(Enumeration<URL> urls, BackendType bt){
		URL fragmentURL = null;
		while(urls.hasMoreElements()){
			URL url = urls.nextElement();
			switch (bt) {			
			case CPU:
				if(url.getFile().contains(CPU_IDENT)){
					fragmentURL = url;
				}
				break;
			case GPU:
				if(url.getFile().contains(GPU_IDENT)){
					fragmentURL = url;
				}
				break;
			default:
				break;
			}
		}
		return fragmentURL;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		//nothing to do here
	}
	
	/**
     * Returns the shared instance.
     *
     * @return Singleton instance of the Plugin
     */
    public static DL4JPluginActivator getDefault() {
        return plugin;
    }

}
