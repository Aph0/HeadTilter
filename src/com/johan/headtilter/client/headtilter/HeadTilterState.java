package com.johan.headtilter.client.headtilter;

import com.vaadin.shared.AbstractComponentState;

public class HeadTilterState extends AbstractComponentState {

	
	public HeadTilterMode mode = HeadTilterMode.TILT_WIDGETS;
	
	public Integer leftCalibrated = null;
	public Integer rightCalibrated = null;
	public Integer topCalibrated = null;
	public Integer bottomCalibrated = null;

}
