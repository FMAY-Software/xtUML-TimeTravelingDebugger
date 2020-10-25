package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDropToFrame;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

import com.trl.xtuml.debug.session.ui.commands.FastForwardCommand;
import com.trl.xtuml.debug.session.ui.commands.FullFastForwardCommand;
import com.trl.xtuml.debug.session.ui.commands.FullRewindCommand;
import com.trl.xtuml.debug.session.ui.commands.IFastForwardHandler;
import com.trl.xtuml.debug.session.ui.commands.IFullFastForwardHandler;
import com.trl.xtuml.debug.session.ui.commands.IFullRewindHandler;
import com.trl.xtuml.debug.session.ui.commands.IRewindHandler;
import com.trl.xtuml.debug.session.ui.commands.RewindCommand;

public class ModelDebugCommandFactory implements IAdapterFactory {

	private static IRewindHandler fgRewindCommand = new RewindCommand();
	private static IFullRewindHandler fgFullRewindCommand = new FullRewindCommand();
	private static IFastForwardHandler fgFastForwardCommand = new FastForwardCommand();
	private static IFullFastForwardHandler fgFullFastForwardCommand = new FullFastForwardCommand();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
	 * java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IDebugElement || adaptableObject instanceof ILaunch
				|| adaptableObject instanceof IProcess || adaptableObject instanceof IThread
				|| adaptableObject instanceof IDropToFrame) {
			if (IRewindHandler.class.equals(adapterType)) {
				return (T) fgRewindCommand;
			} else if (IFullRewindHandler.class.equals(adapterType)) {
				return (T) fgFullRewindCommand;
			} else if (IFastForwardHandler.class.equals(adapterType)) {
				return (T) fgFastForwardCommand;
			} else if (IFullFastForwardHandler.class.equals(adapterType)) {
				return (T) fgFullFastForwardCommand;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IRewindHandler.class, IFullRewindHandler.class, IFastForwardHandler.class,
				IFullFastForwardHandler.class};
	}

}
