package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class RewindHandler extends DebugCommandHandler {
   
	@Override
	protected Class<RewindActionDelegate> getCommandType() {
        return RewindActionDelegate.class;
    }
	
}
