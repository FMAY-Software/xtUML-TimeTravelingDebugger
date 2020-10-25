package com.trl.xtuml.debug.session.control.breakpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.debug.core.model.IBreakpoint;
import org.xtuml.bp.debug.ui.model.BPLineBreakpoint;
import org.xtuml.bp.debug.ui.model.BPStateBreakpoint;

public class BreakpointControlHandlerFactory {
	static Map<IBreakpoint, IBreakpointControlHandler> handlers = new HashMap<IBreakpoint, IBreakpointControlHandler>();

	public static void createHandler(IBreakpoint breakpoint) {
		if (breakpoint instanceof BPLineBreakpoint) {
			handlers.put(breakpoint, new LineBreakpointControlHandler(breakpoint));
		} else if (breakpoint instanceof BPStateBreakpoint) {
			handlers.put(breakpoint, new StateEntryBreakpointControlHandler(breakpoint));
		}
	}

	public static void removeHandler(IBreakpoint breakpoint) {
		handlers.remove(breakpoint);
	}

	public static IBreakpointControlHandler getHandler(IBreakpoint breakpoint) {
		return handlers.get(breakpoint);
	}

	public static List<IBreakpointControlHandler> getHandlers() {
		List<IBreakpointControlHandler> controlHandlers = new ArrayList<IBreakpointControlHandler>();
		Set<Entry<IBreakpoint, IBreakpointControlHandler>> entrySet = handlers.entrySet();
		entrySet.forEach(entry -> {
			controlHandlers.add(entry.getValue());
		});
		return controlHandlers;
	}
}
