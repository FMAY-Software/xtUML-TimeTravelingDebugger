package com.trl.xtuml.debug.session.control.notification;

import org.xtuml.bp.core.common.IModelDelta;

import com.trl.xtuml.debug.session.control.IExecutionDeltaManager;

public interface IDropToFrameEvent {
	
	IExecutionDeltaManager getSource();
	IModelDelta getDelta();

	static IDropToFrameEvent createDefault(IExecutionDeltaManager source, IModelDelta delta) {
		return new IDropToFrameEvent() {

			@Override
			public IExecutionDeltaManager getSource() {
				return source;
			}

			@Override
			public IModelDelta getDelta() {
				return delta;
			}

		};
	}
	
}
