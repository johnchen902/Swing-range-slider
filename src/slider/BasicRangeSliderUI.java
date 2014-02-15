package slider;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * A basic UI delegate for the RangeSlider component. BasicRangeSliderUI paints
 * two thumbs, one for the lower value and one for the upper value.
 */
public class BasicRangeSliderUI extends BasicSliderUI {

	protected Rectangle rangeTrackRect;
	protected Rectangle upperThumbRect;

	protected boolean upperThumbSelected;

	protected boolean paintingUpperThumb;

	protected Color rangeColor;
	protected Color disabledRangeColor;

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
		rangeTrackRect = new Rectangle();
		upperThumbRect = new Rectangle();
		upperThumbSelected = true;
		super.installUI(c);
		slider.setFocusTraversalKeysEnabled(false);
	}

	@Override
	protected void installDefaults(JSlider slider) {
		super.installDefaults(slider);
		rangeColor = UIManager.getColor("RangeSlider.range");
		if (rangeColor == null)
			rangeColor = Color.GREEN;
		disabledRangeColor = UIManager.getColor("RangeSlider.disabled.range");
		if (disabledRangeColor == null)
			disabledRangeColor = new Color(0x3FBF3F);
	}

	@Override
	protected void installKeyboardActions(JSlider slider) {
		super.installKeyboardActions(slider);
		InputMap im = slider.getInputMap();
		im.put(KeyStroke.getKeyStroke("pressed TAB"), "tab");
		im.put(KeyStroke.getKeyStroke("shift pressed TAB"), "shifttab");
		im.put(KeyStroke.getKeyStroke("ctrl pressed TAB"), "ctrltab");
		im.put(KeyStroke.getKeyStroke("ctrl shift pressed TAB"), "ctrlshifttab");
		ActionMap ac = slider.getActionMap();
		ac.put("tab", new TabActions(false, false));
		ac.put("shifttab", new TabActions(false, true));
		ac.put("ctrltab", new TabActions(true, false));
		ac.put("ctrlshifttab", new TabActions(true, true));
		ac.put(MinMaxAction.MIN_SCROLL_INCREMENT, new MinMaxAction(true));
		ac.put(MinMaxAction.MAX_SCROLL_INCREMENT, new MinMaxAction(false));
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

	@Override
	protected void calculateGeometry() {
		super.calculateGeometry();
		calculateRangeTrackRect();
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

	protected void calculateRangeTrackRect() {
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int lowerX = thumbRect.x + (thumbRect.width / 2);
			int upperX = upperThumbRect.x + (upperThumbRect.width / 2);

			rangeTrackRect.setBounds(Math.min(lowerX, upperX), trackRect.y,
					Math.abs(upperX - lowerX), trackRect.height);
		} else {
			int lowerY = thumbRect.y + (thumbRect.height / 2);
			int upperY = upperThumbRect.y + (upperThumbRect.height / 2);

			rangeTrackRect.setBounds(trackRect.x, Math.min(lowerY, upperY),
					trackRect.width, Math.abs(upperY - lowerY));
		}
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

	private static Rectangle bufferRect = new Rectangle();

	private void swapThumbRects() {
		bufferRect.setFrame(upperThumbRect);
		upperThumbRect.setFrame(thumbRect);
		thumbRect.setFrame(bufferRect);
	}

	/**
	 * Paints the slider.
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		recalculateIfInsetsChanged();
		recalculateIfOrientationChanged();
		Rectangle clip = g.getClipBounds();

		if (!clip.intersects(trackRect) && slider.getPaintTrack())
			calculateGeometry();

		if (slider.getPaintTrack() && clip.intersects(trackRect)) {
			paintTrack(g);
		}
		if (slider.getPaintTrack() && clip.intersects(rangeTrackRect)) {
			paintRangeTrack(g);
		}
		if (slider.getPaintTicks() && clip.intersects(tickRect)) {
			paintTicks(g);
		}
		if (slider.getPaintLabels() && clip.intersects(labelRect)) {
			paintLabels(g);
		}
		if (slider.hasFocus() && clip.intersects(focusRect)) {
			paintFocus(g);
		}
		if (upperThumbSelected) {
			if (clip.intersects(thumbRect)) {
				paintingUpperThumb = false;
				paintThumb(g);
			}
			if (clip.intersects(upperThumbRect)) {
				paintingUpperThumb = true;
				swapThumbRects();
				paintThumb(g);
				swapThumbRects();
			}
		} else {
			if (clip.intersects(upperThumbRect)) {
				paintingUpperThumb = true;
				swapThumbRects();
				paintThumb(g);
				swapThumbRects();
			}
			if (clip.intersects(thumbRect)) {
				paintingUpperThumb = false;
				paintThumb(g);
			}
		}
	}

	/**
	 * Paints the track highlight between the two thumbs.
	 */
	protected void paintRangeTrack(Graphics g) {
		Rectangle trackBounds = rangeTrackRect;

		if (slider.isEnabled())
			g.setColor(rangeColor);
		else {
			g.setColor(disabledRangeColor);
		}
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int cy = (trackBounds.height / 2) - 2;
			g.fillRect(trackBounds.x, trackBounds.y + cy, trackBounds.width, 4);
		} else {
			int cx = (trackBounds.width / 2) - 2;
			g.fillRect(trackBounds.x + cx, trackBounds.y, 4, trackBounds.height);
		}
	}

	@Override
	public void paintThumb(Graphics g) {
		super.paintThumb(g);
		if (slider.hasFocus() && (paintingUpperThumb == upperThumbSelected)) {
			g.setColor(getFocusColor());
			BasicGraphicsUtils.drawDashedRect(g, thumbRect.x, thumbRect.y,
					thumbRect.width, thumbRect.height);
		}
	}

	@Override
	public void setThumbLocation(int x, int y) {
		if (upperThumbSelected) {
			setUpperThumbLocation(x, y);
		} else {
			super.setThumbLocation(x, y);
		}
		calculateRangeTrackRect();
	}

	private static Rectangle upperUnionRect = new Rectangle();

	private void setUpperThumbLocation(int x, int y) {
		upperUnionRect.setBounds(upperThumbRect);

		upperThumbRect.setLocation(x, y);

		SwingUtilities.computeUnion(upperThumbRect.x, upperThumbRect.y,
				upperThumbRect.width, upperThumbRect.height, upperUnionRect);
		slider.repaint(upperUnionRect.x, upperUnionRect.y,
				upperUnionRect.width, upperUnionRect.height);
	}

	protected void setUpperThumbSelected(boolean upperThumbSelected) {
		if (this.upperThumbSelected != upperThumbSelected) {
			this.upperThumbSelected = upperThumbSelected;
			slider.repaint(thumbRect);
			slider.repaint(upperThumbRect);
		}
	}

	private void scrollByDelta(int delta) {
		if (upperThumbSelected) {
			int oldValue = ((RangeSlider) slider).getUpperValue();
			if (slider.getExtent() + delta >= 0) {
				((RangeSlider) slider).setUpperValue(oldValue + delta);
			} else {
				int oldLowerValue = slider.getValue();
				slider.setValue(oldValue + delta);
				((RangeSlider) slider).setUpperValue(oldLowerValue);
				setUpperThumbSelected(!upperThumbSelected);
			}
		} else {
			int oldValue = slider.getValue();
			if (delta <= slider.getExtent()) {
				slider.setValue(oldValue + delta);
			} else {
				int oldUpperValue = slider.getValue();
				((RangeSlider) slider).setUpperValue(oldValue + delta);
				slider.setValue(oldUpperValue);
				setUpperThumbSelected(!upperThumbSelected);
			}
		}
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

			scrollByDelta(delta);
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

			scrollByDelta(delta);
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
				calculateRangeTrackRect();
				slider.repaint();
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

				setUpperThumbSelected(inUpper);

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
		public boolean shouldScroll(int direction) {
			Rectangle r = upperThumbSelected ? upperThumbRect : thumbRect;
			if (slider.getOrientation() == JSlider.VERTICAL) {
				if (drawInverted() ? direction < 0 : direction > 0) {
					if (r.y <= currentMouseY) {
						return false;
					}
				} else if (r.y + r.height >= currentMouseY) {
					return false;
				}
			} else {
				if (drawInverted() ? direction < 0 : direction > 0) {
					if (r.x + r.width >= currentMouseX) {
						return false;
					}
				} else if (r.x <= currentMouseX) {
					return false;
				}
			}

			if (direction > 0
					&& slider.getValue() + slider.getExtent() >= slider
							.getMaximum()) {
				return false;
			} else if (direction < 0
					&& slider.getValue() <= slider.getMinimum()) {
				return false;
			}

			return true;
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
			moveThumb();
		}

		private void moveThumb() {
			Rectangle curThumbRect;
			if (upperThumbSelected) {
				curThumbRect = upperThumbRect;
			} else {
				curThumbRect = thumbRect;
			}
			int newValue = 0;
			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:
				int halfThumbHeight = curThumbRect.height / 2;
				int thumbTop = currentMouseY - offset;
				int trackTop = trackRect.y;
				int trackBottom = trackRect.y + (trackRect.height - 1);

				thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

				setThumbLocation(curThumbRect.x, thumbTop);
				newValue = valueForYPosition(thumbTop + halfThumbHeight);
				break;
			case JSlider.HORIZONTAL:
				int halfThumbWidth = curThumbRect.width / 2;
				int thumbLeft = currentMouseX - offset;
				int trackLeft = trackRect.x;
				int trackRight = trackRect.x + (trackRect.width - 1);

				thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

				setThumbLocation(thumbLeft, curThumbRect.y);
				newValue = valueForXPosition(thumbLeft + halfThumbWidth);
				break;
			}
			if (upperThumbSelected) {
				if (newValue < slider.getValue()) {
					swapThumbRects();
					int newUpperValue = slider.getValue();
					slider.setValue(newValue);
					((RangeSlider) slider).setUpperValue(newUpperValue);
					upperThumbSelected = false;
				} else {
					((RangeSlider) slider).setUpperValue(newValue);
				}
			} else {
				if (newValue > slider.getValue() + slider.getExtent()) {
					swapThumbRects();
					int newLowerValue = ((RangeSlider) slider).getUpperValue();
					((RangeSlider) slider).setUpperValue(newValue);
					slider.setValue(newLowerValue);
					upperThumbSelected = true;
				} else {
					slider.setValue(newValue);
				}
			}
		}
	}

	@SuppressWarnings("serial")
	protected class TabActions extends AbstractAction {
		protected final boolean control;
		protected final boolean shift;

		public TabActions(boolean control, boolean shift) {
			this.control = control;
			this.shift = shift;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!slider.isEnabled())
				return;
			if (isDragging)
				return;
			if (control) {
				if (shift)
					slider.transferFocusBackward();
				else
					slider.transferFocus();
			} else {
				setUpperThumbSelected(!upperThumbSelected);
			}
		}
	}

	@SuppressWarnings("serial")
	protected class MinMaxAction extends AbstractAction {
		public static final String MIN_SCROLL_INCREMENT = "minScroll";
		public static final String MAX_SCROLL_INCREMENT = "maxScroll";

		private boolean isMin;

		protected MinMaxAction(boolean isMin) {
			this.isMin = isMin;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			boolean isMin = this.isMin ^ slider.getInverted();
			if (upperThumbSelected) {
				((RangeSlider) slider).setUpperValue(isMin ? slider.getValue()
						: slider.getMaximum());
			} else {
				slider.setValue(isMin ? slider.getMinimum()
						: ((RangeSlider) slider).getUpperValue());
			}
		}
	}
}
