package com.trl.xtuml.debug.session.ui.commands;

import com.trl.xtuml.debug.session.control.IExecutionDeltaManager;

public class FastForwardCommand extends ExecutionAdjustCommand implements IFastForwardHandler {

	public FastForwardCommand() {
		super(false, IExecutionDeltaManager.FORWARD);
	}

}
