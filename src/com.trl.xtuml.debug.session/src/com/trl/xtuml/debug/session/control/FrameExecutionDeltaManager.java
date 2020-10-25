package com.trl.xtuml.debug.session.control;

import org.eclipse.debug.core.model.IThread;
import org.xtuml.bp.core.BlockInStackFrame_c;
import org.xtuml.bp.core.Block_c;
import org.xtuml.bp.core.Body_c;
import org.xtuml.bp.core.ComponentInstance_c;
import org.xtuml.bp.core.Modeleventnotification_c;
import org.xtuml.bp.core.StackFrame_c;
import org.xtuml.bp.core.Stack_c;
import org.xtuml.bp.core.Statement_c;
import org.xtuml.bp.core.common.IModelDelta;
import org.xtuml.bp.core.common.NonRootModelElement;
import org.xtuml.bp.debug.ui.model.BPThread;

import com.trl.xtuml.debug.session.control.notification.IDropToFrameEvent;

public class FrameExecutionDeltaManager extends StepExecutionDeltaManager {

	@Override
	public boolean adjustExecutionPointer(int direction, boolean full) {
		/* Always truncate to current statement location */
		int difference = getDifferenceToCurrentStatement();
		/* In this case we do not want to truncate from
		 * the current position, but one delta ahead
		 */
		truncateCollection(executionPointer + 1 - difference);
		/* Adjust until at the top of the frame */
		boolean atTop = false;
		while (!atTop) {
			atTop = isAtTopStatement();
			super.adjustExecutionPointer(direction, full);
		}
		/*
		 * Notify any drop to frame listeners of the new next delta
		 */
		notifyFramework(getDelta(1));
		return true;
	}

	@Override
	public boolean canAdjustExecutionPointer(Object[] targets, int direction) {
		boolean canAdjust = super.canAdjustExecutionPointer(targets, direction);
		if(canAdjust) {
			/*
			 * additionally check that the current execution point
			 * is ahead of the beginning statement
			 */
			return !isAtTopStatement();
		}
		return canAdjust;
	}

	@Override
	public void notifyFramework() {
		/* do nothing here, our implementation taking a delta handles
		 * refreshing
		 */
	}
	
	protected void notifyFramework(IModelDelta delta) {
		/* notify framework of drop to frame */
		super.fireDropToFrameEvent(IDropToFrameEvent.createDefault(this, delta));
		super.notifyThread(getThread());
		/* reset the frame */
		BPThread.getThreadExecuting((ComponentInstance_c) managing).resetFrame();
	}

	private IThread getThread() {
		return BPThread.getThreadExecuting((ComponentInstance_c) managing);
	}

	private boolean isAtTopStatement() {
		if (super.getBeginningPointer() == executionPointer) {
			return true;
		}
		IModelDelta delta = getDelta(0);
		if (delta.getKind() == Modeleventnotification_c.EXECUTION_STEP_DELTA) {
			if (delta.getModelElement() instanceof Statement_c) {
				Statement_c statement = (Statement_c) delta.getModelElement();
				Block_c block = Block_c.getOneACT_BLKOnR2923(BlockInStackFrame_c.getOneI_BSFOnR2923(
						StackFrame_c.getOneI_STFOnR2929(Stack_c.getOneI_STACKOnR2930((ComponentInstance_c) managing))));
				Body_c outerBody = Body_c.getOneACT_ACTOnR666(block);
				Statement_c topStatement = Statement_c.getOneACT_SMTOnR602(Block_c.getOneACT_BLKOnR601(outerBody));
				Statement_c prev = Statement_c.getOneACT_SMTOnR661Precedes(topStatement);
				while(prev != null) {
					topStatement = prev;
					prev = Statement_c.getOneACT_SMTOnR661Precedes(topStatement);
				}
				if (statement == topStatement) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean deltaHasEnabledBreakpoint(IModelDelta delta) {
		/* we do not care about breakpoints */
		return false;
	}

	@Override
	public boolean isUnderManagement(Object object) {
		/*
		 * object is under management if the model-root matches that of this
		 * managers ComponentInstance
		 */
		if (managing instanceof ComponentInstance_c && object instanceof NonRootModelElement) {
			return ((ComponentInstance_c) managing).getModelRoot() == ((NonRootModelElement) object).getModelRoot();
		}
		return false;
	}

}
