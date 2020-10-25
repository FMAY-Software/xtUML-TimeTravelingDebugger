package com.trl.xtuml.debug.session.ui.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.xtuml.bp.core.CorePlugin;
import org.xtuml.bp.debug.ui.model.BPDebugElement;
import org.xtuml.bp.debug.ui.model.BPDebugTarget;
import org.xtuml.bp.debug.ui.session.control.ExecutionModelRoot;

import com.trl.xtuml.debug.session.control.ExecutionDeltaManager;
import com.trl.xtuml.debug.session.control.IExecutionDeltaManager;
import com.trl.xtuml.debug.session.control.StepExecutionDeltaManager;

public abstract class ExecutionAdjustCommand extends AbstractDebugCommand {

	private boolean full;
	private int direction;
	IExecutionDeltaManager manager = null;

	public ExecutionAdjustCommand(boolean full, int direction) {
		this.full = full;
		this.direction = direction;
	}

	@Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
		boolean run = true;
		while (run) {
			run = manager.adjustExecutionPointer(direction, full);
		}
	}

	@Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
			throws CoreException {
		if (manager != null) {
			return manager.canAdjustExecutionPointer(targets, direction);
		}
		return false;
	}

	@Override
	protected Object getTarget(Object element) {
		if (element instanceof BPDebugElement) {
			ILaunch launch = ((BPDebugElement) element).getLaunch();
			BPDebugTarget target = ((BPDebugTarget) ((BPDebugElement) element).getDebugTarget());
			try {
				if (!target.isTerminated() && target.getThreads().length != 0) {
					String systemName = target.getSystem().getName();
					/*
					 * key for this is the launch model root and the launch
					 * instance
					 */
					Object key = StepExecutionDeltaManager.createKey(
							ExecutionModelRoot.getInstance("/" + systemName + "/" + launch.toString()), launch);
					manager = ExecutionDeltaManager.get(key, StepExecutionDeltaManager.class);
				}
			} catch (DebugException e) {
				CorePlugin.logError("Unable to acess threads.", e);
			}
		}
		return element;
	}

}
