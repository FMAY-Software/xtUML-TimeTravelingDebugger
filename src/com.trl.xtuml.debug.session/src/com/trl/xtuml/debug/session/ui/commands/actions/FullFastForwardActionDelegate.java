package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.jface.action.IAction;

public class FullFastForwardActionDelegate extends ModelDebugActionDelegate {
    
	public FullFastForwardActionDelegate() {
        super();
        setAction(new FullFastForwardAction());
    }

    @Override
	public void init(IAction action) {
        super.init(action);
    }
    
}
