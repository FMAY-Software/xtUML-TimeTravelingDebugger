package com.trl.xtuml.debug.session;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.xtuml.bp.core.Ooaofooa;

import com.trl.xtuml.debug.session.control.breakpoints.StepPointManager;
import com.trl.xtuml.debug.session.model.ModelSynchronizationListener;
import com.trl.xtuml.debug.session.ui.commands.actions.ModelDebugCommandFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.trl.xtuml.debug.session"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private ModelSynchronizationListener synchronizationListener;
	private StepPointManager stepManager = new StepPointManager();
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
        IAdapterManager manager = Platform.getAdapterManager();
		ModelDebugCommandFactory actionFactory = new ModelDebugCommandFactory();
		manager.registerAdapters(actionFactory, ILaunch.class);
		manager.registerAdapters(actionFactory, IProcess.class);
		manager.registerAdapters(actionFactory, IDebugElement.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		if(synchronizationListener != null) {
			Ooaofooa.getDefaultInstance().removeModelChangeListener(synchronizationListener);
			Ooaofooa.getDefaultInstance().deregisterDeltaCollector(synchronizationListener);
		}
		if(stepManager != null) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(stepManager);
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	public void earlyStartup() {
		/* Register a synchronization listener with xtuml core */
		synchronizationListener = new ModelSynchronizationListener();
		Ooaofooa.getDefaultInstance().addModelChangeListener(synchronizationListener);
		Ooaofooa.getDefaultInstance().registerDeltaCollector(synchronizationListener);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(stepManager);
	}

}
