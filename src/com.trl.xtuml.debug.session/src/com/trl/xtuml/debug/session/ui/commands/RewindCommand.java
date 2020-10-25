package com.trl.xtuml.debug.session.ui.commands;

import com.trl.xtuml.debug.session.control.IExecutionDeltaManager;

public class RewindCommand extends ExecutionAdjustCommand implements IRewindHandler {

	public RewindCommand() {
		super(false, IExecutionDeltaManager.REVERSE);
	}

}
