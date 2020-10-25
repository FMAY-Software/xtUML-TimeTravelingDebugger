package com.trl.xtuml.debug.session.control.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.xtuml.bp.core.OalBreakpoint_c;
import org.xtuml.bp.core.Statement_c;
import org.xtuml.bp.core.common.IModelDelta;
import org.xtuml.bp.debug.ui.model.BPLineBreakpoint;

public class LineBreakpointControlHandler extends BreakpointControlHandler {

	public LineBreakpointControlHandler(IBreakpoint breakpoint) {
		super(breakpoint);
	}

	@Override
	public boolean isSetOn(IModelDelta delta) throws CoreException {
		if (super.isSetOn(delta)
				&& delta.getModelElement() instanceof Statement_c) {
			Statement_c statement = (Statement_c) delta.getModelElement();
			OalBreakpoint_c oalBP = OalBreakpoint_c.getOneBP_OALOnR3101(statement);
			if(oalBP != null) {
				return statement.getLinenumber() == ((BPLineBreakpoint) breakpoint).getLineNumber();
			}
		}
		return false;
	}

}
