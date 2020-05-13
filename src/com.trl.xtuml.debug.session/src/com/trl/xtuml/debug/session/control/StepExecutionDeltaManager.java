package com.trl.xtuml.debug.session.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;
import org.xtuml.bp.core.CorePlugin;
import org.xtuml.bp.core.Modeleventnotification_c;
import org.xtuml.bp.core.common.IModelDelta;
import org.xtuml.bp.core.common.ModelRoot;
import org.xtuml.bp.core.common.NonRootModelElement;
import org.xtuml.bp.debug.ui.model.BPStackFrame;
import org.xtuml.bp.debug.ui.model.BPThread;
import org.xtuml.bp.debug.ui.session.control.ExecutionModelRoot;

import com.trl.xtuml.debug.session.control.breakpoints.BreakpointControlHandlerFactory;
import com.trl.xtuml.debug.session.control.breakpoints.IBreakpointControlHandler;
import com.trl.xtuml.debug.session.control.notification.IDropToFrameEvent;
import com.trl.xtuml.debug.session.control.notification.IDropToFrameListener;

/**
 * Maintain a delta manager for every step in debugging. This allows us to
 * traverse the delta history forward and backward.
 * 
 * @author Travis London
 *
 */
public class StepExecutionDeltaManager extends ExecutionDeltaManager implements IDropToFrameListener {
	
	static class ManagerKey {
		ModelRoot root;
		ILaunch launch;
		@Override
		public int hashCode() {
			return root.hashCode() * 31;
		}
		@Override
		public boolean equals(Object obj) {
			return !(obj instanceof ManagerKey) ? false : ((ManagerKey) obj).launch == this.launch;
		}
		
	}
	
	public static ManagerKey createKey(ModelRoot root, ILaunch launch) {
		ManagerKey key = new ManagerKey();
		key.root = root;
		key.launch = launch;
		return key;
	}

	public StepExecutionDeltaManager() {
		ExecutionDeltaManager.addDropToFrameListener(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		ExecutionDeltaManager.removeDropToFrameListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xtuml.bp.debug.ui.model.IExecutionDeltaManager#adjustExecutionPointer
	 * (int, boolean)
	 */
	@Override
	public boolean adjustExecutionPointer(int direction, boolean full) {
		/*
		 * For rewind we reverse the current pointer, applying the deltas in
		 * reverse. We always sit at -1, the end of the delta list or on a delta
		 * indicating statement execution
		 */
		if (direction == REVERSE) {
			if (executionPointer == getBeginningPointer()) {
				notifyFramework();
				return false;
			}
			IModelDelta delta = super.adjustToPointer(direction, full);
			if (delta == null || deltaHasEnabledBreakpoint(delta)
					|| (!full && delta.getKind() == Modeleventnotification_c.EXECUTION_STEP_DELTA) || !canRewind()) {
				notifyFramework();
				return false;
			}
		} else {
			/*
			 * For fast forward we progress the current pointer, re-applying the
			 * deltas
			 */
			if (!canFastForward()) {
				notifyFramework();
				/* if at the end or hit a line breakpoint we are done */
				return false;
			}
			IModelDelta delta = super.adjustToPointer(direction, full);
			if (getDelta(1) == null || deltaHasEnabledBreakpoint(getDelta(1))) {
				notifyFramework();
				return false;
			}
			if (delta == null || (!full && getDelta(1).getKind() == Modeleventnotification_c.EXECUTION_STEP_DELTA)
					|| !canFastForward()) {
				notifyFramework();
				return false;
			}
		}
		return true;
	}

	public void notifyFramework() {
		notifyThreads();
		refreshViews();
	}

	protected void notifyThreads() {
		Arrays.asList(getLaunch().getDebugTargets()).forEach(target -> {
			try {
				Arrays.asList(target.getThreads()).forEach(this::notifyThread);
			} catch (Exception e) {
				CorePlugin.logError("Unable to get threads for target.", e);
			}
		});
	}
	
	protected void notifyThread(IThread thread) {
		DebugPlugin.getDefault().fireDebugEventSet(
				new DebugEvent[] { new DebugEvent(thread, DebugEvent.RESUME, DebugEvent.CLIENT_REQUEST) });
		DebugPlugin.getDefault().fireDebugEventSet(
				new DebugEvent[] { new DebugEvent(thread, DebugEvent.SUSPEND, DebugEvent.STEP_END) });

	}
	
	protected void refreshViews() {
		BPThread.refreshCanvases();
		BPThread.refreshVerifierViews();
	}
	
	private ILaunch getLaunch() {
		ManagerKey key = (ManagerKey) managing;
		return key.launch;
	}

	/**
	 * Check for breakpoints of type:
	 * 
	 * <ul>
	 * <li><code>BPLineBreakpoint</code> - OAL Line Breakpoint</li>
	 * <li><code>BPAttributeBreakpoint</code> - Class Attribute Breakpoint</li>
	 * <li><code>BPStateBreakpoint</code> - State entry breakpoint</li>
	 * <li><code>BPEventBreakpoint</code> - Event dequeue breakpoint</li>
	 * <li><code>BPPendingEventBreakpoint</code> - Pending event breakpoint</li>
	 * <li><code>BPClassCreateDeleteBreakpoint</code> - Class creation/deletion breakpoint</li>
	 * <li><code>BPAssocCreateDeleteBreakpoint</code> - Association
	 * creation/deletion breakpoint</li>
	 * </ul>
	 * 
	 * @param delta
	 *            - <code>IModelDelta</code> The model delta being adjusted
	 *            (reverted or processed)
	 * @return - <code>true</code> if a delta was found for the given delta
	 */
	protected boolean deltaHasEnabledBreakpoint(IModelDelta delta) {
		if (delta == null) {
			return false;
		}
		List<IBreakpointControlHandler> handlers = BreakpointControlHandlerFactory.getHandlers();
		for (IBreakpointControlHandler handler : handlers) {
			try {
				if (handler.enabled()) {
					if (handler.isSetOn(delta)) {
						return true;
					}
				}
			} catch (CoreException e) {
				CorePlugin.logError("Could not access breakpoint information.", e);
				return false;
			}
		}
		return false;
	}

	protected ISuspendResume[] getSuspendResumesFromTargets(Object[] targets) {
		List<ISuspendResume> suspendResumes = new ArrayList<ISuspendResume>();
		for (Object target : targets) {
			if (target instanceof ISuspendResume) {
				suspendResumes.add((ISuspendResume) target);
			}
			if (target instanceof ILaunch) {
				IDebugTarget[] launchTargets = ((ILaunch) target).getDebugTargets();
				for (IDebugTarget debugTarget : launchTargets) {
					suspendResumes.add(debugTarget);
				}
			}
		}
		return suspendResumes.toArray(new ISuspendResume[0]);
	}

	@Override
	public boolean canAdjustExecutionPointer(Object[] targets, int direction) {
		/* Do not allow manipulation unless the thread is suspended */
		ISuspendResume[] suspendResumes = getSuspendResumesFromTargets(targets);
		for (ISuspendResume suspendResume : suspendResumes) {
			/*
			 * do not consider event stack frames as they are never considered
			 * suspended
			 */
			if (suspendResume instanceof BPStackFrame && ((BPStackFrame) suspendResume).isEvent()) {
				continue;
			}
			if (!suspendResume.isSuspended()) {
				return false;
			}
		}
		return super.canAdjustExecutionPointer(targets, direction);
	}

	@Override
	public int getInitializationDeltaKind() {
		return Modeleventnotification_c.EXECUTION_STEP_DELTA;
	}

	@Override
	public int getDecrement() {
		return 1;
	}

	@Override
	public int getIncrement() {
		return 1;
	}

	@Override
	public void handleDrop(IDropToFrameEvent event) {
		/*
		 * match our current execution pointer to that caused by the drop to
		 * frame event, note that adjustment is allowed always as the drop to
		 * event will have already verified
		 */
		IModelDelta dropDelta = event.getDelta();
		IModelDelta currentDelta = getDelta(0);
		while (currentDelta != null) {
			executionPointer -= getDecrement();
			if (currentDelta.equals(dropDelta)) {
				break;
			}
			currentDelta = getDelta(0);
		}
		/* truncate the collection */
		truncateCollection();
	}

	@Override
	public boolean isUnderManagement(Object object) {
		if (object instanceof NonRootModelElement && managing instanceof ManagerKey) {
			NonRootModelElement nrme = (NonRootModelElement) object;
			if (nrme.getModelRoot() instanceof ExecutionModelRoot) {
				ModelRoot objectRoot = ((NonRootModelElement) object).getModelRoot();
				ExecutionModelRoot executionRoot = (ExecutionModelRoot) ((ManagerKey) managing).root;
				boolean underManagement = objectRoot == executionRoot;
				if (!underManagement) {
					underManagement = objectRoot.getId().startsWith(executionRoot.getId());
				}
				return underManagement;
			}
		}
		return false;
	}

}
