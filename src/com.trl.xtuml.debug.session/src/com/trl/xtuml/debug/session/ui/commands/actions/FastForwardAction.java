package com.trl.xtuml.debug.session.ui.commands.actions;

import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.resource.ImageDescriptor;

import com.trl.xtuml.debug.session.ui.commands.IFastForwardHandler;

/**
 * This action will fast forward the current program
 * pointer either one step in history, or if
 * configured as such to the end unless a
 * break point is set.  Note that values are not
 * re-evaluated yet re-applied from the execution
 * history.
 * 
 * @author travislondon
 *
 */
public class FastForwardAction extends DebugCommandAction {
	
	public FastForwardAction() {
		setActionDefinitionId("org.eclipse.debug.ui.commands.FastForward"); //$NON-NLS-1$
	}

	@Override
	public String getText() {
		return "Fast Forward";
	}

	@Override
	public String getId() {
		return "org.xtuml.bp.debug.ui.commands.actions.FastForward"; //$NON-NLS-1$
	}

	@Override
	protected Class<IFastForwardHandler> getCommandType() {
		return IFastForwardHandler.class;
	}

	@Override
	public String getHelpContextId() {
		/* Documentation will be in the Help section */
		return null;
	}

	@Override
	public String getToolTipText() {
		/* action name is fine */
		return "";
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		/* handled by the meta-data */
		return null;
	}

	@Override
	public ImageDescriptor getHoverImageDescriptor() {
		/* handled by the meta-data */
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		/* handled by the meta-data */
		return null;
	}
	
}
