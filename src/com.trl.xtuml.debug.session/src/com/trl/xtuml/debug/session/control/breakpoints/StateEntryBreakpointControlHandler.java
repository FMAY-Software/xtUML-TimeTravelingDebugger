package com.trl.xtuml.debug.session.control.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.xtuml.bp.core.ClassInEngine_c;
import org.xtuml.bp.core.Instance_c;
import org.xtuml.bp.core.PendingEvent_c;
import org.xtuml.bp.core.StateBreakpoint_c;
import org.xtuml.bp.core.StateMachineState_c;
import org.xtuml.bp.core.common.IModelDelta;
import org.xtuml.bp.debug.ui.model.BPStateBreakpoint;

public class StateEntryBreakpointControlHandler extends BreakpointControlHandler {

	public StateEntryBreakpointControlHandler(IBreakpoint breakpoint) {
		super(breakpoint);
	}

	@Override
	public boolean isSetOn(IModelDelta delta) throws CoreException {
		boolean result = super.isSetOn(delta);
		if(result) {
			if(breakpoint instanceof BPStateBreakpoint && delta.getModelElement() instanceof PendingEvent_c) {
				BPStateBreakpoint stateBreakpoint = (BPStateBreakpoint) breakpoint;
				PendingEvent_c event = (PendingEvent_c) delta.getModelElement();
				StateBreakpoint_c targetBP = stateBreakpoint.getTargetBreakpoint();
				StateMachineState_c state = StateMachineState_c.getOneSM_STATEOnR3104(targetBP);
				StateMachineState_c instEventState = StateMachineState_c.getOneSM_STATEOnR2915(Instance_c.getOneI_INSOnR2907(event));
				if(state == instEventState) {
					return true;
				}
				StateMachineState_c classEventState = StateMachineState_c.getOneSM_STATEOnR2932(ClassInEngine_c.getOneCSME_CIEOnR2931(event));
				if(state == classEventState) {
					return true;
				}
			}
		}
		return false;
	}

}
