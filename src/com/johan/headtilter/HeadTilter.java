package com.johan.headtilter;

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
}
