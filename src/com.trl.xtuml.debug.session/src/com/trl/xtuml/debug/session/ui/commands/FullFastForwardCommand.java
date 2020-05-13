package com.trl.xtuml.debug.session.ui.commands;

import com.trl.xtuml.debug.session.control.IExecutionDeltaManager;

public class FullFastForwardCommand extends ExecutionAdjustCommand implements IFullFastForwardHandler {

	public FullFastForwardCommand() {
		super(true, IExecutionDeltaManager.FORWARD);
	}

}
