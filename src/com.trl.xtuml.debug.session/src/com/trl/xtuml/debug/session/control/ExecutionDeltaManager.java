package com.trl.xtuml.debug.session.control;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.xtuml.bp.core.BlockInStackFrame_c;
import org.xtuml.bp.core.Block_c;
import org.xtuml.bp.core.Breakpoint_c;
import org.xtuml.bp.core.ComponentInstance_c;
import org.xtuml.bp.core.Condition_c;
import org.xtuml.bp.core.CorePlugin;
import org.xtuml.bp.core.EventBreakpoint_c;
import org.xtuml.bp.core.Modeleventnotification_c;
import org.xtuml.bp.core.OalBreakpoint_c;
import org.xtuml.bp.core.Ooaofooa;
import org.xtuml.bp.core.Runstatetype_c;
import org.xtuml.bp.core.StackFrame_c;
import org.xtuml.bp.core.StateBreakpoint_c;
import org.xtuml.bp.core.Statement_c;
import org.xtuml.bp.core.common.AttributeChangeModelDelta;
import org.xtuml.bp.core.common.IDeltaCollector;
import org.xtuml.bp.core.common.IModelDelta;
import org.xtuml.bp.core.common.Transaction;
import org.xtuml.bp.debug.ui.model.BPStackFrame;
import org.xtuml.bp.debug.ui.model.BPThread;

import com.trl.xtuml.debug.session.control.notification.IDropToFrameEvent;
import com.trl.xtuml.debug.session.control.notification.IDropToFrameListener;

abstract public class ExecutionDeltaManager implements IExecutionDeltaManager, IDeltaCollector, IDebugEventSetListener {

	private List<IModelDelta> deltaCollection = new ArrayList<>();
	private static Map<Object, Map<Class<?>, IExecutionDeltaManager>> managersMap = new HashMap<>();
	private static List<IExecutionDeltaManager> managers = new ArrayList<>();
	protected int executionPointer = -1;
	protected Object managing;
	private boolean initialized;

	private static boolean collectionDisabled = false;

	private static List<IDropToFrameListener> dropToFrameListeners = new ArrayList<>();

	public ExecutionDeltaManager() {
		Ooaofooa.getDefaultInstance().registerDeltaCollector(this);
		// add us as a listener to the debug framework,
		// this allows us to insert the appropriate
		// marker deltas
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	protected IModelDelta adjustToPointer(int direction, boolean full) {
		/*
		 * Note the execution pointer is at the location of the already executed
		 * delta, so to move forward we execute the next delta
		 */
		int deltaIndex = direction == 0 ? executionPointer : executionPointer + 1;
		IModelDelta delta = deltaCollection.get(deltaIndex);
		try {
			setIgnoreDeltas(true);
			switch (direction) {
			case FORWARD:
				/* process the delta */
				Transaction.processDelta(true, delta, Ooaofooa.getDefaultInstance(), null);
				if (delta.getModelElement() instanceof Statement_c) {
					Statement_c statement = (Statement_c) delta.getModelElement();
					System.err.println("Executing statement: " + statement.getLabel());
				}
				increaseExecutionPointer();
				break;
			case REVERSE:
				/* revert the delta */
				Transaction.revertDelta(delta, Ooaofooa.getDefaultInstance(), true, null);
				decreaseExecutionPointer();
				break;
			default:
				break;
			}
		} finally {
			setIgnoreDeltas(false);
		}
		return delta;
	}

	@Override
	public IModelDelta getDelta(int offset) {
		if (deltaCollection == null) {
			return null;
		}
		if (executionPointer + offset > deltaCollection.size() - 1) {
			return null;
		}
		if (executionPointer + offset < 0) {
			return null;
		}
		return deltaCollection.get(executionPointer + offset);
	}

	@Override
	public int getExecutionPointer() {
		return executionPointer;
	}

	protected void clearCollection() {
		deltaCollection.clear();
	}

	@Override
	public void dispose() {
		Ooaofooa.getDefaultInstance().deregisterDeltaCollector(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		deltaCollection = null;
		executionPointer = -1;
		Map<Class<?>, IExecutionDeltaManager> classMap = managersMap.get(managing);
		classMap.remove(getClass(), this);
		managersMap.remove(managing);
		managers.remove(this);
		setManaging(null);
	}

	@Override
	public void waitIfLocked() {
		/* locking not used at this time */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xtuml.bp.debug.ui.model.IExecutionDeltaManager#addToCollection(org.
	 * xtuml.bp.core.common.IModelDelta)
	 */
	@Override
	public void addToCollection(IModelDelta delta) {
		/* if the delta is null this is an error but outside of this class */
		if (delta == null) {
			CorePlugin.logError("Attempted collection on a null delta.", null);
			return;
		}
		/* skip deltas which are not managed by this class */
		if (!isUnderManagement(delta.getModelElement())) {
			return;
		}
		boolean add = false;
		// Always add the initialization detla
		if (delta.getKind() == getInitializationDeltaKind()) {
			initialized = true;
			add = true;
		} else {
			/*
			 * Deltas are ignored when suspended, or when an instance of this
			 * class is adjusting the execution pointer
			 */
			if (isRuntimeControlDelta(delta)) {
				return;
			}
			if (ignoreDeltas()) {
				return;
			}
			if (initialized && shouldCollectDelta(delta)) {
				add = true;
			}
		}
		if (add) {
			if(deltaCollection != null) {
				synchronized (deltaCollection) {
					deltaCollection.add(delta);
					executionPointer += 1;					
				}
			}
		}
	}

	private boolean isRuntimeControlDelta(IModelDelta delta) {
		/* we want to ignore run state changes */
		if (delta instanceof AttributeChangeModelDelta) {
			AttributeChangeModelDelta attributeChangeDelta = (AttributeChangeModelDelta) delta;
			if (attributeChangeDelta.getModelElement() instanceof Breakpoint_c
					|| attributeChangeDelta.getModelElement() instanceof OalBreakpoint_c
					|| attributeChangeDelta.getModelElement() instanceof Condition_c
					|| attributeChangeDelta.getModelElement() instanceof StateBreakpoint_c
					|| attributeChangeDelta.getModelElement() instanceof EventBreakpoint_c) {
				return true;
			}
			if (attributeChangeDelta.getAttributeName().equals("Runstate")) { // $NON-NLS-1$
				/*
				 * if this is a resume, then truncate the deltas collected
				 * allowing for recalculation
				 */
				if (((Integer) attributeChangeDelta.getNewValue()) == Runstatetype_c.Running
						|| ((Integer) attributeChangeDelta.getNewValue()) == Runstatetype_c.Stepping) {
					if (canFastForward()) {
						truncateCollection();
					}
					setIgnoreDeltas(false);
				}
				return true;
			}
			if (attributeChangeDelta.getAttributeName().equals("Suspendreason")) { // $NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xtuml.bp.debug.ui.model.IExecutionDeltaManager#canFastForward()
	 */
	@Override
	public boolean canFastForward() {
		if (deltaCollection == null || deltaCollection.isEmpty())
			return false;
		return executionPointer < deltaCollection.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xtuml.bp.debug.ui.model.IExecutionDeltaManager#canRewind()
	 */
	@Override
	public boolean canRewind() {
		return executionPointer > -1;
	}

	public void setManaging(Object managing) {
		this.managing = managing;
	}

	/**
	 * Get or create an execution delta manager of the given class type.
	 * 
	 * @param modelRoot
	 *            - modelroot that will be managed by the delta manager
	 * @param type
	 *            - class type of the delta manager to get
	 * @return - the existing or created delta manager or null if unable to
	 *         instantiate
	 */
	public static IExecutionDeltaManager get(Object key, Class<?> type) {
		/* given type must implement IExecutionDeltaManager */
		boolean argumentImplements = classImplementsExecutionDeltaManager(type);
		if (!argumentImplements) {
			throw new IllegalArgumentException("Class type must be an interface of IExecutionDeltaManager");
		}
		Map<Class<?>, IExecutionDeltaManager> rootManagers = managersMap.get(key);
		if (rootManagers == null) {
			rootManagers = new HashMap<Class<?>, IExecutionDeltaManager>();
			managersMap.put(key, rootManagers);
		}
		IExecutionDeltaManager manager = rootManagers.get(type);
		if (manager == null) {
			Constructor<?> constructor;
			try {
				constructor = type.getConstructor(new Class[0]);
				manager = (IExecutionDeltaManager) constructor.newInstance(new Object[0]);
				manager.setManaging(key);
				rootManagers.put(type, manager);
				managers.add(manager);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				CorePlugin.logError("Unable to instantiate class of type: " + type.getName(), e);
			}
		}
		return manager;
	}

	private static boolean classImplementsExecutionDeltaManager(Class<?>[] interfaces) {
		for (Class<?> implemented : interfaces) {
			if (implemented == IExecutionDeltaManager.class) {
				return true;
			}
		}
		return false;
	}

	private static boolean classImplementsExecutionDeltaManager(Class<?> type) {
		Class<?>[] interfaces = type.getInterfaces();
		boolean implemented = classImplementsExecutionDeltaManager(interfaces);
		if (!implemented) {
			Class<?> superclass = type.getSuperclass();
			while (superclass != null && !implemented) {
				interfaces = superclass.getInterfaces();
				implemented = classImplementsExecutionDeltaManager(interfaces);
				superclass = superclass.getSuperclass();
			}
		}
		return implemented;
	}

	@Override
	public boolean shouldCollectDelta(IModelDelta delta) {
		return true;
	};

	protected int getDifferenceToCurrentStatement() {
		BPThread thread = BPThread.getThreadExecuting((ComponentInstance_c) managing);
		if (thread == null) {
			return 0;
		}
		int decrement = 0;
		try {
			BPStackFrame topBPStackFrame = (BPStackFrame) thread.getTopStackFrame();
			if (topBPStackFrame != null) {
				StackFrame_c topFrame = topBPStackFrame.getStackFrame();
				Statement_c executingStatement = Statement_c
						.getOneACT_SMTOnR2941(BlockInStackFrame_c.getOneI_BSFOnR2923(topFrame));
				/* traverse the deltas until we hit the current statement */
				IModelDelta currentDelta = getDelta(decrement);
				while (currentDelta != null && currentDelta.getModelElement() != executingStatement) {
					decrement -= 1;
					currentDelta = getDelta(decrement);
				}
				/*
				 * if we did not find the statement in the execution list then
				 * the pointer is current
				 */
				if (currentDelta == null) {
					decrement = 0;
				}
			}
		} catch (DebugException e) {
			CorePlugin.logError("Unable to retrieve current execution top frame.", e);
		}
		return decrement;
	}

	public void truncateCollection() {
		/* truncate starting at execution pointer by default */
		truncateCollection(executionPointer + getDecrement());
	}

	/**
	 * After an action semantic change a re-parse occurs
	 * This requires existing  stack frames to get
	 * re-evaluated.  The block in stack frame will contain
	 * dangling deltas in the delta manager, remove these and
	 * any deltas up to them.
	 * 
	 * @param manager
	 */
	public static void clearDanglingDeltas(IExecutionDeltaManager manager) {
		IModelDelta delta = manager.getDelta(0);
		int toDecrease = 0;
		boolean startOfDanglingDeltasFound = false;
		while (delta != null) {
			toDecrease++;
			if (delta.getKind() == Modeleventnotification_c.EXECUTION_STEP_DELTA
					&& delta.getModelElement() instanceof Statement_c) {
				Block_c block = Block_c.getOneACT_BLKOnR602((Statement_c) delta.getModelElement());
				if (block == null || !startOfDanglingDeltasFound) {
					if(block == null) {
						startOfDanglingDeltasFound = true;
					}
					while (toDecrease != 0) {
						manager.decreaseExecutionPointer();
						toDecrease--;
					}
				}
			}
			delta = manager.getDelta(-toDecrease);
		}
		manager.truncateCollection();
		manager.notifyFramework();
	}
	
	protected void truncateCollection(int startPoint) {
		/*
		 * remove collected deltas from this point forward this indicates a
		 * re-evaluation
		 */
		if (executionPointer == -1) {
			deltaCollection.clear();
		} else {
			if (startPoint >= deltaCollection.size()) {
				/*
				 * being asked to truncate pass the limit of this collection
				 */
				return;
			}
			deltaCollection.subList(startPoint, deltaCollection.size()).clear();
		}
	}

	@Override
	public void startCollecting(Transaction transaction) {
		/* nothing to do on start */ };

	@Override
	public void endCollecting() {
		/* nothing to do on end */ };

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			/* clear the deltas on terminate */
			if (event.getKind() == DebugEvent.TERMINATE) {
				dispose();
				break;
			}
			if (event.getKind() == DebugEvent.SUSPEND) {
				/* stop collecting deltas */
				setIgnoreDeltas(true);
			}
			if (event.getKind() == DebugEvent.RESUME) {
				/* begin collecting deltas again */
				setIgnoreDeltas(false);
			}
		}
	}

	public static void addDropToFrameListener(IDropToFrameListener listener) {
		dropToFrameListeners.add(listener);
	}

	public static void removeDropToFrameListener(IDropToFrameListener listener) {
		dropToFrameListeners.remove(listener);
	}

	public static void fireDropToFrameEvent(IDropToFrameEvent event) {
		dropToFrameListeners.forEach(listener -> {
			listener.handleDrop(event);
		});
	}

	@Override
	public boolean canAdjustExecutionPointer(Object[] targets, int direction) {
		switch (direction) {
		case REVERSE:
			return canRewind();
		case FORWARD:
			return canFastForward();
		default:
			return false;
		}
	}

	private boolean ignoreDeltas() {
		return collectionDisabled;
	}

	public synchronized void setIgnoreDeltas(boolean disabled) {
		collectionDisabled = disabled;
	}

	@Override
	public int getBeginningPointer() {
		return -1;
	}

	@Override
	public int getEndingPointer() {
		return deltaCollection.size() - 1;
	}

	@Override
	public void increaseExecutionPointer() {
		executionPointer += getIncrement();
	}

	@Override
	public void decreaseExecutionPointer() {
		executionPointer -= getDecrement();
	}

	public static List<IExecutionDeltaManager> getManagers() {
		return managers;
	}
}
