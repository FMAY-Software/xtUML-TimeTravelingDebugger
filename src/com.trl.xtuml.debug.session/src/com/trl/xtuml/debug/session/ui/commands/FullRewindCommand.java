package com.trl.xtuml.debug.session.ui.commands;

import com.trl.xtuml.debug.session.control.IExecutionDeltaManager;

public class FullRewindCommand extends ExecutionAdjustCommand implements IFullRewindHandler {

	public FullRewindCommand() {
		super(true, IExecutionDeltaManager.REVERSE);
	}

}
