package com.trl.xtuml.debug.session.control.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.xtuml.bp.core.Modeleventnotification_c;
import org.xtuml.bp.core.common.IModelDelta;

public abstract class BreakpointControlHandler implements IBreakpointControlHandler {
	
	IBreakpoint breakpoint;

	public BreakpointControlHandler(IBreakpoint breakpoint) {
		this.breakpoint = breakpoint;
	}
	
	@Override
	public boolean isSetOn(IModelDelta delta) throws CoreException {
		if(delta.getKind() == Modeleventnotification_c.EXECUTION_STEP_DELTA) {
			return true;
		}
		return false;
	}

	@Override
	public void setBreakpoint(IBreakpoint breakpoint) {
		this.breakpoint = breakpoint;
	}

	@Override
	public boolean enabled() throws CoreException {
		IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		if(breakpointManager.isEnabled()) {
			return breakpoint.isEnabled();
		}
		return false;
	}

}
