package org.knime.ext.dl4j.libs;

import java.lang.reflect.Field;

import org.eclipse.osgi.internal.loader.EquinoxClassLoader;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.internal.loader.classpath.FragmentClasspath;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.ext.dl4j.libs.prefs.DL4JPreferencePage;
import org.osgi.framework.BundleContext;

public class DL4JPluginActivator extends AbstractUIPlugin {
	private static DL4JPluginActivator plugin;
	
	private final String GPU_FRAG_REGEX = "org\\.knime\\.ext\\.dl4j\\.bin\\.(linux|macosx|windows)\\.x86_64\\.gpu";
	private final String CPU_FRAG_REGEX = "org\\.knime\\.ext\\.dl4j\\.bin\\.(linux|macosx|windows)\\.x86_64\\.cpu";
	
	private enum BackendType{
		GPU,
		CPU
	}
	
	public DL4JPluginActivator() {
		plugin = this;
	}
	
	@SuppressWarnings("restriction")
	@Override
	public void start(BundleContext context) throws Exception {	
		boolean useGPU = plugin.getPreferenceStore().getBoolean(DL4JPreferencePage.P_BACKEND_TYPE);
		
		BackendType backendType = BackendType.CPU;		
		if(useGPU){
			backendType = BackendType.GPU;
		} 		
		
		EquinoxClassLoader el = (EquinoxClassLoader)getClass().getClassLoader();
		ClasspathManager manager = el.getClasspathManager();
		
		//manually remove either cpu or gpu fragment from EquinoxClassLoader 
		//cp must contain only one backend at a time
		Field f = manager.getClass().getDeclaredField("fragments");
		f.setAccessible(true);
		FragmentClasspath[] frags = (FragmentClasspath[])f.get(manager);	
		FragmentClasspath fragmentOverwrite = findBackendFragment(frags, backendType);
		if(fragmentOverwrite != null){
			f.set(manager, new FragmentClasspath[]{fragmentOverwrite});
		} else {
			throw new Exception("Backend Fragment for: " + backendType + " could not be found.");
		}
		
		f.setAccessible(false);
		
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
	 * Searches for a fragment corresponding to the specified {@link BackendType} in the specified
	 * array of {@link FragmentClasspath}s. Fragment is searched via base file name that needs to
	 * match a static member regex of this class. If no matching fragment can be found null is
	 * returned.
	 * 
	 * @param frags the fragments to search
	 * @param backend the backend fragment type to search for
	 * @return found fragment or null if not found
	 */
	@SuppressWarnings("restriction")
	private FragmentClasspath findBackendFragment(FragmentClasspath[] frags, BackendType backend){
		String regex;
		switch (backend) {
		case CPU:
			regex = CPU_FRAG_REGEX;
			break;
		case GPU:
			regex = GPU_FRAG_REGEX;
			break;
		default:
			throw new IllegalStateException("No case for backend type: " + backend + " defined.");
		}
		for(FragmentClasspath fcp : frags){
			String fragmentFileName = fcp.getGeneration().getBundleFile().getBaseFile().getName();
			if(fragmentFileName.matches(regex)){
				return fcp;
			}
		}
		return null;
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
