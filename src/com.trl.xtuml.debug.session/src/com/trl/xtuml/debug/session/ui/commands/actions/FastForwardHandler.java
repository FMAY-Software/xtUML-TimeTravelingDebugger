package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class FastForwardHandler extends DebugCommandHandler {
   
	@Override
	protected Class<FastForwardActionDelegate> getCommandType() {
        return FastForwardActionDelegate.class;
    }
	
}
