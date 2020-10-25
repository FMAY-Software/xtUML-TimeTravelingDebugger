package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.jface.action.IAction;

public class FullRewindActionDelegate extends ModelDebugActionDelegate {
    
	public FullRewindActionDelegate() {
        super();
        setAction(new FullRewindAction());
    }

    @Override
	public void init(IAction action) {
        super.init(action);
    }
    
}
