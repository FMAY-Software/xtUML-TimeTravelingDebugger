package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class FullFastForwardHandler extends DebugCommandHandler {
   
	@Override
	protected Class<FullFastForwardActionDelegate> getCommandType() {
        return FullFastForwardActionDelegate.class;
    }
	
}
