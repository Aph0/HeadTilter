package com.johan.headtilter.client.headtilter;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.touch.client.Point;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.shared.communication.SharedState;
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
	private String transformStringBrowserSpecific;
	private Element canvas;
	private Element video;
	private HeadTilterMode mode = HeadTilterMode.MOUSE_CURSOR;
	private HeadClickTracker hcTracker;

	private float firstHeadWidth = -1;
	private Element mousePtr;
	private int canvasWidth = 320;
	private int canvasHeight = 240;

	// This guy decides when and where a click has occurred
	private class HeadClickTracker {
		/**
		 * Move head to the left, over 12 degree limit, then inside some time,
		 * move back = click
		 * 
		 * @author johan
		 * 
		 */
		private float angleSensitivity = 0.12f; // radians, TODO
		private float leftRightSensitivity = 2f;
		private float upDownSensitivity = 5f;
		private int calibratedUpdate = 0;
		
		private int headCenteredX = 0;
		private int headCenteredY = 0;
		

		private LinkedList<Point> lastHeadPositions = new LinkedList<Point>();

		private LinkedList<Float> lastAngles = new LinkedList<Float>();
		private int currentX = -1;
		private int currentY = -1;



		private boolean checkIfClickPatternFound() {
			boolean containsOverLimit = false;
			boolean containsUnderLimit = false;

			for (Float angle : lastAngles) {
				// 90 degrees ~= 1.57 radians
				if (angle > 1.57 + angleSensitivity)
					containsOverLimit = true;
				if (angle < 1.57 - angleSensitivity)
					containsUnderLimit = true;
			}

			return containsOverLimit && containsUnderLimit;
		}

		private Point createAveragePointFrom(List<Point> pList) {
			int lSize = pList.size();
			double xTot = 0;
			double yTot = 0;
			for (Point p : lastHeadPositions) {
				xTot += p.getX();
				yTot += p.getY();
			}
			return new Point(xTot / lSize, yTot / lSize);
		}
		
		private Point transformPointToConformCalibration(Point p) {
			int scrWidth = Window.getClientWidth();
			int scrHeight = Window.getClientHeight();

			return new Point(scrWidth / 2  - (p.getX() - headCenteredX) * leftRightSensitivity, 
					scrHeight / 2  - (headCenteredY - p.getY()) * upDownSensitivity);
		}

		// Only estimates for the canvas. After this, you
		// Need to transform the point in accordance with the calibration
		private Point estimateHeadClickPosition() {
			int limit = 10; // TODO, create some var

			if (lastHeadPositions.size() <= 0)
				return null;

			if (lastHeadPositions.size() >= limit) {
				float xAverageVal = 0;
				int occurrences = 0;
				float yAverageVal = 0;
				int examined = 0;

				for (Point point : lastHeadPositions) {
					if (examined >= limit || occurrences >= 4)
						break; // Only step back 'limit' amount of steps or that
								// we have enough occurrences

					if (xAverageVal > 0
							|| (point.getX() <= xAverageVal / occurrences
									&& point.getX() >= xAverageVal
											/ occurrences
									&& point.getY() <= yAverageVal
											/ occurrences && point
									.getY() >= yAverageVal / occurrences)) {
						xAverageVal += point.getX();
						yAverageVal += point.getY();
						occurrences++;
					} else {
						xAverageVal = (float) point.getX();
						yAverageVal = (float) point.getY();
						occurrences = 1;
					}
					examined++;
				}

				if (occurrences >= 4) {
					return new Point(xAverageVal / occurrences, yAverageVal
							/ occurrences);
				} else {
					return createAveragePointFrom(lastHeadPositions.subList(0,
							limit));
				}

			} else {
				return createAveragePointFrom(lastHeadPositions);

			}
		}

		/**
		 * Returns a Point(x, y) if there was a click, otherwise null
		 */
		public Point isHeadClick(float x, float y, float angle) {
			lastHeadPositions.addFirst(new Point(x, y));
			lastAngles.addFirst(angle);
			if (lastHeadPositions.size() > 20) {
				lastHeadPositions.removeLast();
				lastAngles.removeLast();
			}
			if (checkIfClickPatternFound()) {
				Point clicked = estimateHeadClickPosition();
				lastHeadPositions.clear();
				lastAngles.clear();
				return clicked;
			}
			return null;
		}

		public void updateCursorScreenPosition(float x, float y) {
			currentX = (int) x;
			currentY = (int) y;
			if (mousePtr == null) createOrRemoveArtificialMousePointer();
			
			Point transformedPt = transformPointToConformCalibration(new Point(x, y));
			mousePtr.getStyle().setLeft(transformedPt.getX(), Unit.PX);
			mousePtr.getStyle().setTop(transformedPt.getY(), Unit.PX);

		}

		public void setHeadCenteredRequest() {
			if (currentX == -1) return;
			headCenteredX = currentX;
			headCenteredY = currentY;
			
		}

		public float getLeftRightSensitivity() {
			return leftRightSensitivity;
		}

		public void setLeftRightSensitivity(float leftRightSensitivity) {
			this.leftRightSensitivity = leftRightSensitivity;
		}

		public float getUpDownSensitivity() {
			return upDownSensitivity;
		}

		public void setUpDownSensitivity(float upDownSensitivity) {
			this.upDownSensitivity = upDownSensitivity;
		}

	}

	public HeadTilterConnector() {
		hcTracker = new HeadClickTracker();
	}

	@Override
	protected void init() {
		super.init();
		transformStringBrowserSpecific = "transform";
		String prefix = getBrowserPrefix();
		if (prefix != null) {
			transformStringBrowserSpecific = getBrowserPrefix() + "Transform";
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
		
		hcTracker.setUpDownSensitivity(getState().upDownSensitivity);
		hcTracker.setLeftRightSensitivity(getState().leftRightSensitivity);
		if (stateChangeEvent.hasPropertyChanged("headCalibratedForMiddleCounter")) {
			hcTracker.setHeadCenteredRequest();
		}

	}

	@Override
	public HeadTilterState getState() {
		return (HeadTilterState) super.getState();
	}

	@Override
	protected void extend(ServerConnector target) {
		extendedWidget = ((ComponentConnector) target).getWidget();

	}

	private void registerHeadTiltingListeners() {
		if (DOM.getElementById("inputCanvas") == null) {
			canvas = DOM.createElement("canvas");
			canvas.setId("inputCanvas");
			canvas.setAttribute("width", "" + canvasWidth );
			canvas.setAttribute("height", "" + canvasHeight);
			// canvas.getStyle().setDisplay(Display.NONE);
			Document.get().getBody().appendChild(canvas);
		}

		if (DOM.getElementById("video") == null) {

			video = DOM.createElement("video");
			video.setId("inputVideo");
			video.setAttribute("autoplay", null);
			video.setAttribute("loop", null);
			video.getStyle().setWidth(1, Unit.PX);
			video.getStyle().setHeight(1, Unit.PX);
			video.getStyle().setVisibility(Visibility.HIDDEN);
			Document.get().getBody().appendChild(video);
		}
		initiateHeadTilter();
	}

	public void handleHeadMoveEvent(float x, float y, float angle,
			float height, float width) {
		boolean clickedWithHead = false;
		if (getMode() == HeadTilterMode.TILT_WIDGETS) {
			if (extendedWidget != null) {
				extendedWidget
						.getElement()
						.getStyle()
						.setProperty(
								transformStringBrowserSpecific,
								"rotate(" + (-angle * (180 / 3.1415) + 90)
										+ "deg)");

			}
			
		} else if (getMode() == HeadTilterMode.MAGNIFIER) {
			if (firstHeadWidth == -1)
				firstHeadWidth = width;
			extendedWidget
			.getElement()
			.getStyle()
			.setProperty(
					transformStringBrowserSpecific,
					"scale(" + (width / firstHeadWidth) + ")");

		} else {
			hcTracker.updateCursorScreenPosition(x, y);
			Point clickPt = hcTracker.isHeadClick(x, y, angle);
			if (clickPt != null) {
				System.out.println("Headclick at: " + clickPt);
			}
		}
		System.out.println("moved x: " + x + " y: " + y + " angle: " + angle
				+ "   height: " + height + " width: " + width);
		if (clickedWithHead)
			System.out.println("HEADLICK!");
	}

	public static native void declareHeadMoveEventMethod(
			HeadTilterConnector headTilterConnector) /*-{
														var headTilterConnector = headTilterConnector;
														
														$wnd.handleHeadMoveEvent = function(x, y, angle, height, width) {
														$entry(headTilterConnector.@com.johan.headtilter.client.headtilter.HeadTilterConnector::handleHeadMoveEvent(FFFFF)(x, y, angle, height, width));
														}
														}-*/;

	private native void initiateHeadTilter() /*-{
												var videoInput = $wnd.document.getElementById('inputVideo');
												var canvasInput = $wnd.document.getElementById('inputCanvas');
												var htracker = new $wnd.headtrackr.Tracker({detectionInterval : 75, calcAngles : true});
												$entry(htracker);
												htracker.init(videoInput, canvasInput);
												htracker.start();
												// minns  headtrackingEvent
												$wnd.document.addEventListener('facetrackingEvent', function(event) {
													$wnd.handleHeadMoveEvent(event.x, event.y, event.angle, event.height, event.width, event.time);
												}, false);
												}-*/;

	// TODO: remove facetrackingEvent also
	private native void clearUp() /*-{
									$wnd.htracker.stop();
									}-*/;

	@Override
	public void onUnregister() {
		Document.get().getBody().removeChild(canvas);
		Document.get().getBody().removeChild(video);
		if (mousePtr != null) {
			Document.get().getBody().removeChild(mousePtr);
		}
		// TODO: this does not seem to work (?)
		clearUp();
		super.onUnregister();
	}

	public HeadTilterMode getMode() {
		return mode;
	}

	public void setMode(HeadTilterMode mode) {
		this.mode = mode;
		createOrRemoveArtificialMousePointer();
	}

	private void createOrRemoveArtificialMousePointer() {
		// Lets only create one element, or remove if not needed
		Element elem = DOM.getElementById("mouse_cursor_headtilter");
		if (mode == HeadTilterMode.MOUSE_CURSOR) {
			if (elem == null) {
				mousePtr = DOM.createDiv();
				mousePtr.setInnerText("X");
				// Stuff like this has to be taken into account when scrolling
				mousePtr.getStyle().setPosition(Position.ABSOLUTE);
				Document.get().getBody().appendChild(mousePtr);
			} else {
				mousePtr = elem;
			}
		} else {
			if (elem != null) {
				// TODO: Check this works
				elem.getParentElement().removeChild(elem);
			} 
			mousePtr = null;		
		}
		
	}

}
