package com.trl.xtuml.debug.session.control.breakpoints;

import java.util.Arrays;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;

public class StepPointManager implements IBreakpointListener {
	
	public StepPointManager() {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		Arrays.asList(breakpoints).forEach(breakpoint -> breakpointAdded(breakpoint));
	}
	
	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		BreakpointControlHandlerFactory.createHandler(breakpoint);
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		BreakpointControlHandlerFactory.removeHandler(breakpoint);
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		/* do nothing */
	}

}
