package slider;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * A basic UI delegate for the RangeSlider component. RangeSliderUI paints two
 * thumbs, one for the lower value and one for the upper value.
 */
public class BasicRangeSliderUI extends BasicSliderUI {

	protected Rectangle upperThumbRect;

	private boolean upperThumbSelected;

	protected Color rangeColor;

	private transient boolean isDragging;

	protected Color getRangeColor() {
		return rangeColor;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// ComponentUI Interface Implementation methods
	// ///////////////////////////////////////////////////////////////////////////
	public static ComponentUI createUI(JComponent b) {
		return new BasicRangeSliderUI((RangeSlider) b);
	}

	public BasicRangeSliderUI(RangeSlider b) {
		super(b);
	}

	/**
	 * Installs this UI delegate on the specified component.
	 */
	@Override
	public void installUI(JComponent c) {
		upperThumbRect = new Rectangle();
		super.installUI(c);
	}

	@Override
	protected void installDefaults(JSlider slider) {
		super.installDefaults(slider);
		rangeColor = UIManager.getColor("RangeSlider.range");
		if (rangeColor == null)
			rangeColor = Color.GREEN;
	}

	/**
	 * Creates a listener to handle change events in the specified slider.
	 */
	@Override
	protected ChangeListener createChangeListener(JSlider slider) {
		return new ChangeHandler();
	}

	/**
	 * Creates a listener to handle track events in the specified slider.
	 */
	@Override
	protected TrackListener createTrackListener(JSlider slider) {
		return new RangeTrackListener();
	}

	/**
	 * Updates the dimensions for both thumbs.
	 */
	@Override
	protected void calculateThumbSize() {
		// Call superclass method for lower thumb size.
		super.calculateThumbSize();

		// Set upper thumb size.
		upperThumbRect.setSize(thumbRect.width, thumbRect.height);
	}

	private int getTickSpacing() {
		int majorTickSpacing = slider.getMajorTickSpacing();
		int minorTickSpacing = slider.getMinorTickSpacing();

		int result;

		if (minorTickSpacing > 0) {
			result = minorTickSpacing;
		} else if (majorTickSpacing > 0) {
			result = majorTickSpacing;
		} else {
			result = 0;
		}

		return result;
	}

	/**
	 * Updates the locations for both thumbs.
	 */
	@Override
	protected void calculateThumbLocation() {
		// Call superclass method for lower thumb location.
		super.calculateThumbLocation();

		// Adjust upper value to snap to ticks if necessary.
		if (slider.getSnapToTicks()) {
			int upperValue = slider.getValue() + slider.getExtent();
			int snappedValue = upperValue;
			int tickSpacing = getTickSpacing();

			if (tickSpacing != 0) {
				// If it's not on a tick, change the value
				if ((upperValue - slider.getMinimum()) % tickSpacing != 0) {
					float temp = (float) (upperValue - slider.getMinimum())
							/ (float) tickSpacing;
					int whichTick = Math.round(temp);

					snappedValue = slider.getMinimum()
							+ (whichTick * tickSpacing);
				}

				if (snappedValue != upperValue) {
					slider.setExtent(snappedValue - slider.getValue());
				}
			}
		}

		// Calculate upper thumb location. The thumb is centered over its
		// value on the track.
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int upperPosition = xPositionForValue(slider.getValue()
					+ slider.getExtent());
			upperThumbRect.x = upperPosition - (upperThumbRect.width / 2);
			upperThumbRect.y = trackRect.y;

		} else {
			int upperPosition = yPositionForValue(slider.getValue()
					+ slider.getExtent());
			upperThumbRect.x = trackRect.x;
			upperThumbRect.y = upperPosition - (upperThumbRect.height / 2);
		}
	}

	private static Rectangle hackRect = new Rectangle();

	/**
	 * Paints the slider. The selected thumb is always painted on top of the
	 * other thumb.
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);

		Rectangle clipRect = g.getClipBounds();
		if (clipRect.intersects(upperThumbRect)) {
			hackRect.setBounds(thumbRect);
			thumbRect.setBounds(upperThumbRect);
			paintThumb(g);
			thumbRect.setBounds(hackRect);
		}
	}

	/**
	 * Paints the track.
	 */
	@Override
	public void paintTrack(Graphics g) {
		// Draw track.
		super.paintTrack(g);

		Rectangle trackBounds = trackRect;

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			// Determine position of selected range by moving from the middle
			// of one thumb to the other.
			int lowerX = thumbRect.x + (thumbRect.width / 2);
			int upperX = upperThumbRect.x + (upperThumbRect.width / 2);

			// Determine track position.
			int cy = (trackBounds.height / 2) - 2;

			// Save color and shift position.
			Color oldColor = g.getColor();
			g.translate(trackBounds.x, trackBounds.y + cy);

			// Draw selected range.
			if (slider.isEnabled())
				g.setColor(rangeColor);
			else
				g.setColor(new Color(0, 127, 0));
			for (int y = 0; y <= 3; y++) {
				g.drawLine(lowerX - trackBounds.x, y, upperX - trackBounds.x, y);
			}

			// Restore position and color.
			g.translate(-trackBounds.x, -(trackBounds.y + cy));
			g.setColor(oldColor);

		} else {
			// Determine position of selected range by moving from the middle
			// of one thumb to the other.
			int lowerY = thumbRect.x + (thumbRect.width / 2);
			int upperY = upperThumbRect.x + (upperThumbRect.width / 2);

			// Determine track position.
			int cx = (trackBounds.width / 2) - 2;

			// Save color and shift position.
			Color oldColor = g.getColor();
			g.translate(trackBounds.x + cx, trackBounds.y);

			// Draw selected range.
			if (slider.isEnabled())
				g.setColor(rangeColor);
			else
				g.setColor(new Color(0, 127, 0));
			for (int x = 0; x <= 3; x++) {
				g.drawLine(x, lowerY - trackBounds.y, x, upperY - trackBounds.y);
			}

			// Restore position and color.
			g.translate(-(trackBounds.x + cx), -trackBounds.y);
			g.setColor(oldColor);
		}
	}

	@Override
	public void setThumbLocation(int x, int y) {
		assert !upperThumbSelected;
		super.setThumbLocation(x, y);
	}

	private static Rectangle upperUnionRect = new Rectangle();

	public void setUpperThumbLocation(int x, int y) {
		assert upperThumbSelected;

		upperUnionRect.setBounds(upperThumbRect);

		upperThumbRect.setLocation(x, y);

		SwingUtilities.computeUnion(upperThumbRect.x, upperThumbRect.y,
				upperThumbRect.width, upperThumbRect.height, upperUnionRect);
		slider.repaint(upperUnionRect.x, upperUnionRect.y,
				upperUnionRect.width, upperUnionRect.height);
	}

	/**
	 * Moves the selected thumb in the specified direction by a block increment.
	 * This method is called when the user presses the Page Up or Down keys.
	 */
	@Override
	public void scrollByBlock(int direction) {
		synchronized (slider) {
			int blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
			if (blockIncrement == 0) {
				blockIncrement = 1;
			}

			if (slider.getSnapToTicks()) {
				int tickSpacing = getTickSpacing();

				if (blockIncrement < tickSpacing) {
					blockIncrement = tickSpacing;
				}
			}

			int delta = blockIncrement
					* ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

			if (upperThumbSelected) {
				int oldValue = ((RangeSlider) slider).getUpperValue();
				((RangeSlider) slider).setUpperValue(oldValue + delta);
			} else {
				int oldValue = slider.getValue();
				slider.setValue(oldValue + delta);
			}
		}
	}

	/**
	 * Moves the selected thumb in the specified direction by a unit increment.
	 * This method is called when the user presses one of the arrow keys.
	 */
	@Override
	public void scrollByUnit(int direction) {
		synchronized (slider) {
			int delta = (direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;

			if (slider.getSnapToTicks()) {
				delta *= getTickSpacing();
			}

			if (upperThumbSelected) {
				int oldValue = ((RangeSlider) slider).getUpperValue();
				((RangeSlider) slider).setUpperValue(oldValue + delta);
			} else {
				int oldValue = slider.getValue();
				slider.setValue(oldValue + delta);
			}
		}
	}

	/**
	 * Listener to handle model change events. This calculates the thumb
	 * locations and repaints the slider if the value change is not caused by
	 * dragging a thumb.
	 */
	public class ChangeHandler extends BasicSliderUI.ChangeHandler {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (!isDragging) {
				super.stateChanged(e);
			}
		}
	}

	/**
	 * Listener to handle mouse movements in the slider track.
	 */
	public class RangeTrackListener extends TrackListener {

		@Override
		public void mousePressed(MouseEvent e) {
			if (!slider.isEnabled()) {
				return;
			}

			// We should recalculate geometry just before
			// calculation of the thumb movement direction.
			// It is important for the case, when JSlider
			// is a cell editor in JTable. See 6348946.
			calculateGeometry();

			currentMouseX = e.getX();
			currentMouseY = e.getY();

			if (slider.isRequestFocusEnabled()) {
				slider.requestFocus();
			}

			boolean inLower = thumbRect.contains(currentMouseX, currentMouseY);
			boolean inUpper = upperThumbRect.contains(currentMouseX,
					currentMouseY);

			if (inLower || inUpper) {
				if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag")
						&& !SwingUtilities.isLeftMouseButton(e)) {
					return;
				}

				if (inLower && inUpper) {
					int disLower, disUpper;
					switch (slider.getOrientation()) {
					case JSlider.VERTICAL:
						disLower = Math.abs(thumbRect.y + thumbRect.height / 2
								- currentMouseY);
						disUpper = Math.abs(upperThumbRect.y
								+ upperThumbRect.height / 2 - currentMouseY);
						break;
					case JSlider.HORIZONTAL:
						disLower = Math.abs(thumbRect.x + thumbRect.width / 2
								- currentMouseX);
						disUpper = Math.abs(upperThumbRect.x
								+ upperThumbRect.width / 2 - currentMouseX);
						break;
					default: // seriously?
						disLower = disUpper = 0;
						break;
					}
					upperThumbSelected = disUpper < disLower;
				} else {
					upperThumbSelected = inUpper;
				}

				Rectangle rect;
				if (upperThumbSelected)
					rect = upperThumbRect;
				else
					rect = thumbRect;

				switch (slider.getOrientation()) {
				case JSlider.VERTICAL:
					offset = currentMouseY - rect.y;
					break;
				case JSlider.HORIZONTAL:
					offset = currentMouseX - rect.x;
					break;
				}
				isDragging = true;
				return;
			}

			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			isDragging = false;
			slider.setValueIsAdjusting(true);

			int direction = 0;
			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:
				if (currentMouseY < thumbRect.y)
					direction = POSITIVE_SCROLL;
				else if (currentMouseY > upperThumbRect.y)
					direction = NEGATIVE_SCROLL;
				break;
			case JSlider.HORIZONTAL:
				if (currentMouseX < thumbRect.x)
					direction = NEGATIVE_SCROLL;
				else if (currentMouseX > upperThumbRect.x)
					direction = POSITIVE_SCROLL;
				break;
			}

			if (direction == 0)
				return;

			if (drawInverted())
				direction = -direction;

			if (shouldScroll(direction)) {
				scrollDueToClickInTrack(direction);
			}
			if (shouldScroll(direction)) {
				scrollTimer.stop();
				scrollListener.setDirection(direction);
				scrollTimer.start();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			isDragging = false;
			super.mouseReleased(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (!slider.isEnabled()) {
				return;
			}

			currentMouseX = e.getX();
			currentMouseY = e.getY();

			if (!isDragging) {
				return;
			}

			slider.setValueIsAdjusting(true);
			if (upperThumbSelected) {
				moveUpperThumb();
			} else {
				moveLowerThumb();
			}
		}

		@Override
		public boolean shouldScroll(int direction) {
			return false; // TODO implement
		}

		/**
		 * Moves the location of the lower thumb, and sets its corresponding
		 * value in the slider.
		 */
		private void moveLowerThumb() {
			int thumbMiddle = 0;

			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:
				int halfThumbHeight = thumbRect.height / 2;
				int thumbTop = currentMouseY - offset;
				int trackTop = trackRect.y;
				int trackBottom = trackRect.y + (trackRect.height - 1);
				int vMax = yPositionForValue(slider.getValue()
						+ slider.getExtent());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackBottom = vMax;
				} else {
					trackTop = vMax;
				}
				thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

				setThumbLocation(thumbRect.x, thumbTop);

				// Update slider value.
				thumbMiddle = thumbTop + halfThumbHeight;
				slider.setValue(valueForYPosition(thumbMiddle));
				break;

			case JSlider.HORIZONTAL:
				int halfThumbWidth = thumbRect.width / 2;
				int thumbLeft = currentMouseX - offset;
				int trackLeft = trackRect.x;
				int trackRight = trackRect.x + (trackRect.width - 1);
				int hMax = xPositionForValue(slider.getValue()
						+ slider.getExtent());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackLeft = hMax;
				} else {
					trackRight = hMax;
				}
				thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

				setThumbLocation(thumbLeft, thumbRect.y);

				// Update slider value.
				thumbMiddle = thumbLeft + halfThumbWidth;
				slider.setValue(valueForXPosition(thumbMiddle));
				break;
			}
		}

		/**
		 * Moves the location of the upper thumb, and sets its corresponding
		 * value in the slider.
		 */
		private void moveUpperThumb() {
			int thumbMiddle = 0;

			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:
				int halfThumbHeight = thumbRect.height / 2;
				int thumbTop = currentMouseY - offset;
				int trackTop = trackRect.y;
				int trackBottom = trackRect.y + (trackRect.height - 1);
				int vMin = yPositionForValue(slider.getValue());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackTop = vMin;
				} else {
					trackBottom = vMin;
				}
				thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

				setUpperThumbLocation(thumbRect.x, thumbTop);

				// Update slider extent.
				thumbMiddle = thumbTop + halfThumbHeight;
				slider.setExtent(valueForYPosition(thumbMiddle)
						- slider.getValue());
				break;
			case JSlider.HORIZONTAL:
				int halfThumbWidth = thumbRect.width / 2;
				int thumbLeft = currentMouseX - offset;
				int trackLeft = trackRect.x;
				int trackRight = trackRect.x + (trackRect.width - 1);
				int hMin = xPositionForValue(slider.getValue());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackRight = hMin;
				} else {
					trackLeft = hMin;
				}
				thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

				setUpperThumbLocation(thumbLeft, thumbRect.y);

				// Update slider extent.
				thumbMiddle = thumbLeft + halfThumbWidth;
				slider.setExtent(valueForXPosition(thumbMiddle)
						- slider.getValue());
				break;
			}
		}
	}
}
