package com.johan.headtilter;

import com.johan.headtilter.client.headtilter.HeadTilterMode;
import com.johan.headtilter.client.headtilter.HeadTilterState;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractExtension;

@JavaScript({"headtrackr.js"})
public class HeadTilter extends AbstractExtension {


    public HeadTilter() {
    	
    }
    
    @Override
    protected HeadTilterState getState() {
    	return (HeadTilterState) super.getState();
    }

    @Override
    protected void extend(AbstractClientConnector target) {
    	super.extend(target);
    }
    
    public void setHeadTilterMode(HeadTilterMode mode) {
    	getState().setMode(mode);
    }
    
    public HeadTilterMode getHeadTilterMode() {
    	return getState().getMode();
    }
    
    /**
     * Only needed if HeadTilterMode is MousePointer
     */
    public void updateHeadCalibratedInMiddle() {
    	getState().headCalibratedForMiddleCounter++;
    }
    
    /**
     * Only needed if HeadTilterMode is MousePointer
     */
    public void setHorizontalSensitivity(float sensitivity) {
    	getState().leftRightSensitivity = sensitivity;
    }
    
    /**
     * Only needed if HeadTilterMode is MousePointer
     */
    public void setVerticalSensitivity(float sensitivity) {
    	getState().upDownSensitivity = sensitivity;
    }
    
}
