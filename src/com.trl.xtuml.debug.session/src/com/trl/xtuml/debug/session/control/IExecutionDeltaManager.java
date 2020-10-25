package com.trl.xtuml.debug.session.control;

import org.xtuml.bp.core.common.IModelDelta;

public interface IExecutionDeltaManager {

	int REVERSE = 0;
	int FORWARD = 1;
	
	default void dispose(){};
	
    boolean adjustExecutionPointer(int direction, boolean full);

	int getExecutionPointer();

	void increaseExecutionPointer();
	void decreaseExecutionPointer();
	int getDecrement();
	int getIncrement();

	boolean canFastForward();

	boolean canRewind();

	boolean canAdjustExecutionPointer(Object[] targets, int direction);
	
	int getBeginningPointer();
	int getEndingPointer();
	
	int getInitializationDeltaKind();
	
	boolean shouldCollectDelta(IModelDelta delta);
    void truncateCollection();

	void setManaging(Object key);
	
	IModelDelta getDelta(int offset);
	
	default public void setIgnoreDeltas(boolean disabled) {}
	
	boolean isUnderManagement(Object object);

	default void notifyFramework() {};

}