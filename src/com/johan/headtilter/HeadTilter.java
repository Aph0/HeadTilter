package com.johan.headtilter;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractJavaScriptExtension;

@JavaScript({"headtrackr.js"})
public class HeadTilter extends AbstractJavaScriptExtension {


    public HeadTilter() {

    }

    @Override
    protected void extend(AbstractClientConnector target) {
    	super.extend(target);
    }
}
