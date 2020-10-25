package com.trl.xtuml.debug.session.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.xtuml.bp.als.oal.ParserAllActivityModifier;
import org.xtuml.bp.core.BlockInStackFrame_c;
import org.xtuml.bp.core.Block_c;
import org.xtuml.bp.core.Body_c;
import org.xtuml.bp.core.ComponentInstance_c;
import org.xtuml.bp.core.ComponentReference_c;
import org.xtuml.bp.core.Component_c;
import org.xtuml.bp.core.Modeleventnotification_c;
import org.xtuml.bp.core.Package_c;
import org.xtuml.bp.core.StackFrame_c;
import org.xtuml.bp.core.Stack_c;
import org.xtuml.bp.core.common.AttributeChangeModelDelta;
import org.xtuml.bp.core.common.IDeltaCollector;
import org.xtuml.bp.core.common.IModelDelta;
import org.xtuml.bp.core.common.ModelChangeAdapter;
import org.xtuml.bp.core.common.ModelChangedEvent;
import org.xtuml.bp.core.common.ModelElement;
import org.xtuml.bp.core.common.NonRootModelElement;
import org.xtuml.bp.core.common.OALPersistenceUtil;
import org.xtuml.bp.core.common.PersistableModelComponent;
import org.xtuml.bp.core.common.Transaction;
import org.xtuml.bp.core.common.TransactionException;
import org.xtuml.bp.debug.ui.model.BPDebugTarget;
import org.xtuml.bp.debug.ui.model.BPStackFrame;
import org.xtuml.bp.debug.ui.model.BPThread;

import com.trl.xtuml.debug.session.control.ExecutionDeltaManager;
import com.trl.xtuml.debug.session.control.FrameExecutionDeltaManager;

public class ModelSynchronizationListener extends ModelChangeAdapter implements IDeltaCollector {

	@Override
	public boolean isSynchronous() {
		return true;
	}

	@Override
	public void modelElementCreated(ModelChangedEvent event, IModelDelta delta) {
		if (delta.getModelElement() instanceof ComponentInstance_c) {
			/* start of verification thread */
			addThreadManagers((ComponentInstance_c) delta.getModelElement());
		}
	}

	@Override
	public void modelElementDeleted(ModelChangedEvent event, IModelDelta delta) {
		if (delta.getModelElement() instanceof ComponentInstance_c) {
			/* end of verification thread */
			deleteThreadManager((ComponentInstance_c) delta.getModelElement());
		}
	}

	private void parseExecutionData(ModelElement modelElement) {
		List<BPThread> threads = getAssociatedThreads(modelElement);
		/* parse each thread owner */
		threads.forEach(thread -> parseThreadOwner(thread, modelElement));
		/* remove dangling references from all managers */
		ExecutionDeltaManager.getManagers().forEach(ExecutionDeltaManager::clearDanglingDeltas);
	}

	private void parseThreadOwner(BPThread thread, ModelElement modelElement) {
		if(thread.isSuspended() && !thread.isTerminated()) {
			/* parse the data under the associated thread */
			List<StackFrame_c> frames = Arrays.asList(StackFrame_c.getManyI_STFsOnR2943(Stack_c.getOneI_STACKOnR2930(thread.getEngine())));
			frames.forEach(frame -> parseFrame(frame, thread, modelElement));
		}
	}

	private void parseFrame(StackFrame_c frame, BPThread thread, ModelElement modelElement) {
		Component_c component = Component_c.getOneC_COnR2955(thread.getEngine());
		if (component == null) {
			component = Component_c.getOneC_COnR4201(ComponentReference_c.getOneCL_ICOnR2963(thread.getEngine()));
		}
		Body_c body = Body_c
				.getOneACT_ACTOnR666(Block_c.getOneACT_BLKOnR2923(BlockInStackFrame_c.getOneI_BSFOnR2923(frame)));
		boolean modifiedActivity = false;
		ModelElement activityOwningElement = modelElement;
		if (body == null) {
			modifiedActivity = true;
			body = OALPersistenceUtil.getOALModelElement(modelElement);
		} else {
			activityOwningElement = (ModelElement) BPStackFrame.getActivityContainerElement(frame);
		}
		body.Dispose();
		ParserAllActivityModifier parseAllActivityModifier = new ParserAllActivityModifier(component,
				new NullProgressMonitor());
		parseAllActivityModifier.parseAction(activityOwningElement, thread.getEngine().getModelRoot());
		body = OALPersistenceUtil.getOALModelElement(activityOwningElement);
		body.Initialize();
		if(modifiedActivity) {
			ExecutionDeltaManager.getManagers().forEach(manager -> manager.setIgnoreDeltas(true));
			frame.Resetifrequired(body.getAction_id());
			ExecutionDeltaManager.getManagers().forEach(manager -> manager.setIgnoreDeltas(false));
		}
	}

	/** 
	 * See the description for {@code NonRootModelElement.getFirstParentPackage()}
	 * 
	 */
	public Component_c getFirstParentComponent(NonRootModelElement element) {
		PersistableModelComponent parent = element.getPersistableComponent();
		if(element instanceof Package_c) {
			// we are looking for the parent of the package
			parent = parent.getParent();
		}
		while (parent != null) {
			if (parent.getRootModelElement() instanceof Component_c) {
				return (Component_c) parent.getRootModelElement();
			}
			parent = parent.getParent();
		}
		return null;
	}

	private List<BPThread> getAssociatedThreads(ModelElement modelElement) {
		List<BPThread> threads = new ArrayList<BPThread>();
		List<BPDebugTarget> targets = BPDebugTarget.getTargets();
		targets.forEach(target -> {
			try {
				Arrays.asList(target.getThreads()).forEach(thread -> {
					try {
						BPThread bpThread = (BPThread) thread;
						Component_c component = Component_c.getOneC_COnR2955(bpThread.getEngine());
						if(component == null) {
							component = Component_c
									.getOneC_COnR4201(ComponentReference_c.getOneCL_ICOnR2963(bpThread.getEngine()));
						}
						Component_c firstComponent = getFirstParentComponent((NonRootModelElement) modelElement);
						if (component == firstComponent) {
							threads.add(bpThread);
						}
					} catch (Exception e) {
						/* ignore */
					}
				});
			} catch (CoreException e) {
				/* ignore */
			}
		});
		return threads;
	}

	private void addThreadManagers(ComponentInstance_c componentInstance) {
		ExecutionDeltaManager.get(componentInstance, FrameExecutionDeltaManager.class);
	}

	private void deleteThreadManager(ComponentInstance_c componentInstance) {
		ExecutionDeltaManager manager = (ExecutionDeltaManager) ExecutionDeltaManager.get(componentInstance,
				FrameExecutionDeltaManager.class);
		manager.dispose();
	}

	@Override
	public void waitIfLocked() {
		/* no locking */
	}

	@Override
	public void startCollecting(Transaction transaction) throws TransactionException {
		/* start or end do not matter */
	}

	@Override
	public void endCollecting() {
		/* start or end do not matter */
	}

	@Override
	public void addToCollection(IModelDelta delta) {
		/* overload usage here responding to
		 * action semantics changes
		 */
		if(delta.getKind() == Modeleventnotification_c.DELTA_ATTRIBUTE_CHANGE) {
			AttributeChangeModelDelta attributeChangeDelta = (AttributeChangeModelDelta) delta;
			if(attributeChangeDelta.getAttributeName().contains("Action_semantics_internal")) { //$NON-NLS-1$
				parseExecutionData(attributeChangeDelta.getModelElement());
			}
		}
	}

}
