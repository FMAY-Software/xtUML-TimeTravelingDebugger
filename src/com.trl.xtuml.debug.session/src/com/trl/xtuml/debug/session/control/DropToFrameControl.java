package com.trl.xtuml.debug.session.control;

import org.eclipse.debug.core.DebugException;
import org.xtuml.bp.core.CorePlugin;
import org.xtuml.bp.debug.ui.model.BPThread;
import org.xtuml.bp.debug.ui.session.control.IDropToFrameControl;

public class DropToFrameControl implements IDropToFrameControl {

	@Override
	public boolean canDropToFrame(BPThread thread) {
		try {
			if(!thread.isTerminated() && !(thread.getDebugTarget().getThreads().length == 0)) {
				ExecutionDeltaManager manager = (ExecutionDeltaManager) ExecutionDeltaManager
						.get(thread.getEngine(), FrameExecutionDeltaManager.class);
				return manager.canAdjustExecutionPointer(new Object[] { this }, IExecutionDeltaManager.REVERSE);
			}
		} catch (DebugException e) {
			CorePlugin.logError("Unable to access threads.", e);
		}
		return false;
	}

	@Override
	public void dropToFrame(BPThread thread) throws DebugException {
		dropFrame(thread);
	}

	public static void dropFrame(BPThread thread) throws DebugException {
		if(!thread.isTerminated() && !(thread.getDebugTarget().getThreads().length == 0)) {
			ExecutionDeltaManager manager = (ExecutionDeltaManager) ExecutionDeltaManager
					.get(thread.getEngine(), FrameExecutionDeltaManager.class);
			manager.adjustExecutionPointer(IExecutionDeltaManager.REVERSE, true);
		}
	}
}
