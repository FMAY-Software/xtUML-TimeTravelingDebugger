package com.trl.xtuml.debug.session.control.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.xtuml.bp.core.common.IModelDelta;

public interface IBreakpointControlHandler {
	void setBreakpoint(IBreakpoint breakpoint);
	boolean isSetOn(IModelDelta delta) throws CoreException;
	boolean enabled() throws CoreException;
}
