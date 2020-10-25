package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class FullRewindHandler extends DebugCommandHandler {
   
	@Override
	protected Class<FullRewindActionDelegate> getCommandType() {
        return FullRewindActionDelegate.class;
    }
	
}
