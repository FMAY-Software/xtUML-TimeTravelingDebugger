package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.jface.action.IAction;

public class RewindActionDelegate extends ModelDebugActionDelegate {
    
	public RewindActionDelegate() {
        super();
        setAction(new RewindAction());
    }

    @Override
	public void init(IAction action) {
        super.init(action);
    }
    
}
