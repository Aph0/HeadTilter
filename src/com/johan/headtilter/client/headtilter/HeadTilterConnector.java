package com.johan.headtilter.client.headtilter;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.shared.ui.Connect;

import com.johan.headtilter.HeadTilter;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;

@Connect(HeadTilter.class)
public class HeadTilterConnector extends AbstractExtensionConnector {

	private static Widget extendedWidget;
	private String rotationStringBrowserSpecific;
	private Element canvas;
	private Element video;

	public HeadTilterConnector() {

	}

	@Override
	protected void init() {
		super.init();
        rotationStringBrowserSpecific = "transform";
        String prefix = getBrowserPrefix();
        if (prefix != null) {
            rotationStringBrowserSpecific = getBrowserPrefix()
                    + "Transform";
        }
		declareHeadMoveEventMethod(this);
		registerHeadTiltingListeners();
	}
	
    private String getBrowserPrefix() {
        String prefix = null;
        final BrowserInfo bi = BrowserInfo.get();
        if (bi.isWebkit()) {
            prefix = "webkit";
        } else if (bi.isIE()) {
            prefix = "ms";
        } else if (bi.isOpera()) {
            prefix = "O";
        } else if (bi.isFirefox()) {
            prefix = "Moz";
        }
        return prefix;
    }

	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

	}

	@Override
	protected void extend(ServerConnector target) {
		extendedWidget = ((ComponentConnector) target).getWidget();

	}

	private void registerHeadTiltingListeners() {
		canvas = DOM.createElement("canvas");
		canvas.setId("inputCanvas");
		canvas.setAttribute("width", "320");
		canvas.setAttribute("height", "240");
		// canvas.getStyle().setDisplay(Display.NONE);

		video = DOM.createElement("video");
		video.setId("inputVideo");
		video.setAttribute("autoplay", null);
		video.setAttribute("loop", null);
		video.getStyle().setWidth(1, Unit.PX);
		video.getStyle().setHeight(1, Unit.PX);
		video.getStyle().setVisibility(Visibility.HIDDEN);

		Document.get().getBody().appendChild(canvas);
		Document.get().getBody().appendChild(video);
		initiateHeadTilter();
	}

	public void handleHeadMoveEvent(float x, float y, float angle,
			float height, float width) {
		if (extendedWidget != null) {
			extendedWidget.getElement()
            .getStyle()
            .setProperty(rotationStringBrowserSpecific,
                    "rotate(" + (-angle * (180 / 3.1415) + 90) + "deg)");
		}
		System.out.println("moved x: " + x + " y: " + y + " angle: " + angle
				+ "   height: " + height + " width: " + width);
	}

	public static native void declareHeadMoveEventMethod(HeadTilterConnector headTilterConnector) /*-{
													var headTilterConnector = headTilterConnector;
													
													$wnd.handleHeadMoveEvent = function(x, y, angle, height, width) {
														$entry(headTilterConnector.@com.johan.headtilter.client.headtilter.HeadTilterConnector::handleHeadMoveEvent(FFFFF)(x, y, angle, height, width));
													}
													}-*/;

	private native void initiateHeadTilter() /*-{
												var videoInput = $wnd.document.getElementById('inputVideo');
												var canvasInput = $wnd.document.getElementById('inputCanvas');
												var htracker = new $wnd.headtrackr.Tracker({detectionInterval : 50, calcAngles : true});
												$entry(htracker);
												htracker.init(videoInput, canvasInput);
												htracker.start();
												// minns  headtrackingEvent
												$wnd.document.addEventListener('facetrackingEvent', function(event) {
													$wnd.handleHeadMoveEvent(event.x, event.y, event.angle, event.height, event.width, event.time);
												}, false);
												}-*/;

	private native void clearUp() /*-{
			$wnd.htracker.stop();
			// TODO: remove facetrackingEvent also
	}-*/;
	
	@Override
	public void onUnregister() {
		Document.get().getBody().removeChild(canvas);
		Document.get().getBody().removeChild(video);
		clearUp();
		super.onUnregister();
	}

}
