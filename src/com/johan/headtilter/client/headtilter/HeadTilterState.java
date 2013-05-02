package com.johan.headtilter.client.headtilter;

import com.vaadin.shared.AbstractComponentState;

public class HeadTilterState extends AbstractComponentState {

	
	private HeadTilterMode mode = HeadTilterMode.TILT_WIDGETS;
	
	// When this counter is incremented, head position will be used as middle
	public int headCalibratedForMiddleCounter = 0;
	
	public float upDownSensitivity = 7;
	
	public float leftRightSensitivity = 4;

	public HeadTilterMode getMode() {
		return mode;
	}

	public void setMode(HeadTilterMode mode) {
		this.mode = mode;
	}


}
