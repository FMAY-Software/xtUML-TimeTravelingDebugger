package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.jface.action.IAction;

public class FastForwardActionDelegate extends ModelDebugActionDelegate {
    
	public FastForwardActionDelegate() {
        super();
        setAction(new FastForwardAction());
    }

    @Override
	public void init(IAction action) {
        super.init(action);
    }
    
}
