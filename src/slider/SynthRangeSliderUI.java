package slider;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Dictionary;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthGraphicsUtils;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthUI;

import sun.swing.SwingUtilities2;

public class SynthRangeSliderUI extends BasicRangeSliderUI implements
		PropertyChangeListener, SynthUI {
	private Rectangle valueRect = new Rectangle();
	private boolean paintValue;

	/**
	 * When a JSlider is used as a renderer in a JTable, its layout is not being
	 * recomputed even though the size is changing. Even though there is a
	 * ComponentListener installed, it is not being notified. As such, at times
	 * when being asked to paint the layout should first be redone. At the end
	 * of the layout method we set this lastSize variable, which represents the
	 * size of the slider the last time it was layed out.
	 * 
	 * In the paint method we then check to see that this is accurate, that the
	 * slider has not changed sizes since being last layed out. If necessary we
	 * recompute the layout.
	 */
	private Dimension lastSize;

	private int trackHeight;
	private int trackBorder;
	private int thumbWidth;
	private int thumbHeight;

	private SynthStyle style;
	private SynthStyle sliderTrackStyle;
	private SynthStyle sliderRangeTrackStyle;
	private SynthStyle sliderThumbStyle;

	/** Used to determine the color to paint the thumb. */
	// happens on rollover, and when pressed
	private transient boolean thumbActiveLower;
	private transient boolean thumbActiveUpper;
	// happens when mouse was depressed while over thumb
	private transient boolean thumbPressed;

	// /////////////////////////////////////////////////
	// ComponentUI Interface Implementation methods
	// /////////////////////////////////////////////////
	/**
	 * Creates a new UI object for the given component.
	 * 
	 * @param c
	 *            component to create UI object for
	 * @return the UI object
	 */
	public static ComponentUI createUI(JComponent c) {
		return new SynthRangeSliderUI();
	}

	protected SynthRangeSliderUI() {
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void installDefaults(JSlider slider) {
		updateStyle(slider);
	}

	/**
	 * Uninstalls default setting. This method is called when a
	 * {@code LookAndFeel} is uninstalled.
	 */
	@Override
	protected void uninstallDefaults(JSlider slider) {
		SynthContext context = getContextByState(slider, ENABLED);
		style.uninstallDefaults(context);
		style = null;

		context = getContext(slider, Region.SLIDER_TRACK, ENABLED);
		sliderTrackStyle.uninstallDefaults(context);
		sliderTrackStyle = null;

		context = getContext(slider, SliderRangeTrackRegion.INSTANCE, ENABLED);
		sliderRangeTrackStyle.uninstallDefaults(context);
		sliderRangeTrackStyle = null;

		context = getContext(slider, Region.SLIDER_THUMB, ENABLED);
		sliderThumbStyle.uninstallDefaults(context);
		sliderThumbStyle = null;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void installListeners(JSlider slider) {
		super.installListeners(slider);
		slider.addPropertyChangeListener(this);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void uninstallListeners(JSlider slider) {
		slider.removePropertyChangeListener(this);
		super.uninstallListeners(slider);
	}

	private void updateStyle(JSlider c) {
		SynthContext context = getContextByState(c, ENABLED);
		SynthStyle oldStyle = style;
		style = (context = updateStyle(context, this)).getStyle();

		if (style != oldStyle) {
			thumbWidth = style.getInt(context, "Slider.thumbWidth", 30);

			thumbHeight = style.getInt(context, "Slider.thumbHeight", 14);

			// handle scaling for sizeVarients for special case components. The
			// key "JComponent.sizeVariant" scales for large/small/mini
			// components are based on Apples LAF
			String scaleKey = (String) slider
					.getClientProperty("JComponent.sizeVariant");
			if (scaleKey != null) {
				if ("large".equals(scaleKey)) {
					thumbWidth *= 1.15;
					thumbHeight *= 1.15;
				} else if ("small".equals(scaleKey)) {
					thumbWidth *= 0.857;
					thumbHeight *= 0.857;
				} else if ("mini".equals(scaleKey)) {
					thumbWidth *= 0.784;
					thumbHeight *= 0.784;
				}
			}

			trackBorder = style.getInt(context, "Slider.trackBorder", 1);

			trackHeight = thumbHeight + trackBorder * 2;

			paintValue = style.getBoolean(context, "Slider.paintValue", true);
			if (oldStyle != null) {
				uninstallKeyboardActions(c);
				installKeyboardActions(c);
			}
		}

		context = getInitialContext(c, Region.SLIDER_TRACK, ENABLED, this);
		sliderTrackStyle = context.getStyle();

		context = getInitialContext(c, SliderRangeTrackRegion.INSTANCE,
				ENABLED, this);
		sliderRangeTrackStyle = context.getStyle();

		context = getInitialContext(c, Region.SLIDER_THUMB, ENABLED, this);
		sliderThumbStyle = context.getStyle();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected RangeTrackListener createTrackListener(JSlider s) {
		return new SynthTrackListener();
	}

	private void updateThumbState(int x, int y) {
		setThumbActiveLower(lowerThumbRect.contains(x, y));
		setThumbActiveUpper(upperThumbRect.contains(x, y));
	}

	private void updateThumbState(int x, int y, boolean pressed) {
		updateThumbState(x, y);
		setThumbPressed(pressed);
	}

	private void setThumbActiveLower(boolean active) {
		if (thumbActiveLower != active) {
			thumbActiveLower = active;
			slider.repaint(lowerThumbRect);
		}
	}

	private void setThumbActiveUpper(boolean active) {
		if (thumbActiveUpper != active) {
			thumbActiveUpper = active;
			slider.repaint(upperThumbRect);
		}
	}

	private void setThumbPressed(boolean pressed) {
		if (thumbPressed != pressed) {
			thumbPressed = pressed;
			slider.repaint(lowerThumbRect);
			slider.repaint(upperThumbRect);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int getBaseline(JComponent c, int width, int height) {
		if (c == null) {
			throw new NullPointerException("Component must be non-null");
		}
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("Width and height must be >= 0");
		}
		if (slider.getPaintLabels() && labelsHaveSameBaselines()) {
			// Get the insets for the track.
			Insets trackInsets = new Insets(0, 0, 0, 0);
			SynthContext trackContext = getContextByRegion(slider,
					Region.SLIDER_TRACK);
			style.getInsets(trackContext, trackInsets);
			if (slider.getOrientation() == JSlider.HORIZONTAL) {
				int valueHeight = 0;
				if (paintValue) {
					SynthContext context = getContext(slider);
					valueHeight = context.getStyle().getGraphicsUtils(context)
							.getMaximumCharHeight(context);
				}
				int tickHeight = 0;
				if (slider.getPaintTicks()) {
					tickHeight = getTickLength();
				}
				int labelHeight = getHeightOfTallestLabel();
				int contentHeight = valueHeight + trackHeight + trackInsets.top
						+ trackInsets.bottom + tickHeight + labelHeight + 4;
				int centerY = height / 2 - contentHeight / 2;
				centerY += valueHeight + 2;
				centerY += trackHeight + trackInsets.top + trackInsets.bottom;
				centerY += tickHeight + 2;
				JComponent label = (JComponent) slider.getLabelTable()
						.elements().nextElement();
				Dimension pref = label.getPreferredSize();
				return centerY + label.getBaseline(pref.width, pref.height);
			} else { // VERTICAL
				Integer value = slider.getInverted() ? getLowestValue()
						: getHighestValue();
				if (value != null) {
					int valueY = insetCache.top;
					int valueHeight = 0;
					if (paintValue) {
						SynthContext context = getContext(slider);
						valueHeight = context.getStyle()
								.getGraphicsUtils(context)
								.getMaximumCharHeight(context);
					}
					int contentHeight = height - insetCache.top
							- insetCache.bottom;
					int trackY = valueY + valueHeight;
					int trackHeight = contentHeight - valueHeight;
					int yPosition = yPositionForValue(value.intValue(), trackY,
							trackHeight);
					JComponent label = (JComponent) slider.getLabelTable().get(
							value);
					Dimension pref = label.getPreferredSize();
					return yPosition - pref.height / 2
							+ label.getBaseline(pref.width, pref.height);
				}
			}
		}
		return -1;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Dimension getPreferredSize(JComponent c) {
		recalculateIfInsetsChanged();
		Dimension d = new Dimension(contentRect.width, contentRect.height);
		if (slider.getOrientation() == JSlider.VERTICAL) {
			d.height = 200;
		} else {
			d.width = 200;
		}
		Insets i = slider.getInsets();
		d.width += i.left + i.right;
		d.height += i.top + i.bottom;
		return d;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Dimension getMinimumSize(JComponent c) {
		recalculateIfInsetsChanged();
		Dimension d = new Dimension(contentRect.width, contentRect.height);
		if (slider.getOrientation() == JSlider.VERTICAL) {
			d.height = lowerThumbRect.height + insetCache.top
					+ insetCache.bottom;
		} else {
			d.width = lowerThumbRect.width + insetCache.left + insetCache.right;
		}
		return d;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void calculateGeometry() {
		calculateThumbSize();
		layout();
		calculateThumbLocation();
		calculateRangeTrackRect();
	}

	/**
	 * Lays out the slider.
	 */
	protected void layout() {
		SynthContext context = getContext(slider);
		SynthGraphicsUtils synthGraphics = style.getGraphicsUtils(context);

		// Get the insets for the track.
		Insets trackInsets = new Insets(0, 0, 0, 0);
		SynthContext trackContext = getContextByRegion(slider,
				Region.SLIDER_TRACK);
		style.getInsets(trackContext, trackInsets);

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			// Calculate the height of all the subcomponents so we can center
			// them.
			valueRect.height = 0;
			if (paintValue) {
				valueRect.height = synthGraphics.getMaximumCharHeight(context);
			}

			trackRect.height = trackHeight;

			tickRect.height = 0;
			if (slider.getPaintTicks()) {
				tickRect.height = getTickLength();
			}

			labelRect.height = 0;
			if (slider.getPaintLabels()) {
				labelRect.height = getHeightOfTallestLabel();
			}

			contentRect.height = valueRect.height + trackRect.height
					+ trackInsets.top + trackInsets.bottom + tickRect.height
					+ labelRect.height + 4;
			contentRect.width = slider.getWidth() - insetCache.left
					- insetCache.right;

			// Check if any of the labels will paint out of bounds.
			int pad = 0;
			if (slider.getPaintLabels()) {
				// Calculate the track rectangle. It is necessary for
				// xPositionForValue to return correct values.
				trackRect.x = insetCache.left;
				trackRect.width = contentRect.width;

				Dictionary<?, ?> dictionary = slider.getLabelTable();
				if (dictionary != null) {
					int minValue = slider.getMinimum();
					int maxValue = slider.getMaximum();

					// Iterate through the keys in the dictionary and find the
					// first and last labels indices that fall within the
					// slider range.
					int firstLblIdx = Integer.MAX_VALUE;
					int lastLblIdx = Integer.MIN_VALUE;
					for (Enumeration<?> keys = dictionary.keys(); keys
							.hasMoreElements();) {
						int keyInt = (Integer) keys.nextElement();
						if (keyInt >= minValue && keyInt < firstLblIdx) {
							firstLblIdx = keyInt;
						}
						if (keyInt <= maxValue && keyInt > lastLblIdx) {
							lastLblIdx = keyInt;
						}
					}
					// Calculate the pad necessary for the labels at the first
					// and last visible indices.
					pad = getPadForLabel(firstLblIdx);
					pad = Math.max(pad, getPadForLabel(lastLblIdx));
				}
			}
			// Calculate the painting rectangles for each of the different
			// slider areas.
			valueRect.x = trackRect.x = tickRect.x = labelRect.x = (insetCache.left + pad);
			valueRect.width = trackRect.width = tickRect.width = labelRect.width = (contentRect.width - (pad * 2));

			int centerY = slider.getHeight() / 2 - contentRect.height / 2;

			valueRect.y = centerY;
			centerY += valueRect.height + 2;

			trackRect.y = centerY + trackInsets.top;
			centerY += trackRect.height + trackInsets.top + trackInsets.bottom;

			tickRect.y = centerY;
			centerY += tickRect.height + 2;

			labelRect.y = centerY;
			centerY += labelRect.height;
		} else {
			// Calculate the width of all the subcomponents so we can center
			// them.
			trackRect.width = trackHeight;

			tickRect.width = 0;
			if (slider.getPaintTicks()) {
				tickRect.width = getTickLength();
			}

			labelRect.width = 0;
			if (slider.getPaintLabels()) {
				labelRect.width = getWidthOfWidestLabel();
			}

			valueRect.y = insetCache.top;
			valueRect.height = 0;
			if (paintValue) {
				valueRect.height = synthGraphics.getMaximumCharHeight(context);
			}

			// Get the max width of the min or max value of the slider.
			FontMetrics fm = slider.getFontMetrics(slider.getFont());
			valueRect.width = Math.max(synthGraphics.computeStringWidth(
					context, slider.getFont(), fm, "" + slider.getMaximum()),
					synthGraphics.computeStringWidth(context, slider.getFont(),
							fm, "" + slider.getMinimum()));

			int l = valueRect.width / 2;
			int w1 = trackInsets.left + trackRect.width / 2;
			int w2 = trackRect.width / 2 + trackInsets.right + tickRect.width
					+ labelRect.width;
			contentRect.width = Math.max(w1, l) + Math.max(w2, l) + 2
					+ insetCache.left + insetCache.right;
			contentRect.height = slider.getHeight() - insetCache.top
					- insetCache.bottom;

			// Layout the components.
			trackRect.y = tickRect.y = labelRect.y = valueRect.y
					+ valueRect.height;
			trackRect.height = tickRect.height = labelRect.height = contentRect.height
					- valueRect.height;

			int startX = slider.getWidth() / 2 - contentRect.width / 2;
			if (slider.getComponentOrientation().isLeftToRight()) {
				if (l > w1) {
					startX += (l - w1);
				}
				trackRect.x = startX + trackInsets.left;

				startX += trackInsets.left + trackRect.width
						+ trackInsets.right;
				tickRect.x = startX;
				labelRect.x = startX + tickRect.width + 2;
			} else {
				if (l > w2) {
					startX += (l - w2);
				}
				labelRect.x = startX;

				startX += labelRect.width + 2;
				tickRect.x = startX;
				trackRect.x = startX + tickRect.width + trackInsets.left;
			}
		}
		lastSize = slider.getSize();
	}

	/**
	 * Calculates the pad for the label at the specified index.
	 * 
	 * @param i
	 *            index of the label to calculate pad for.
	 * @return padding required to keep label visible.
	 */
	private int getPadForLabel(int i) {
		int pad = 0;

		JComponent c = (JComponent) slider.getLabelTable().get(i);
		if (c != null) {
			int centerX = xPositionForValue(i);
			int cHalfWidth = c.getPreferredSize().width / 2;
			if (centerX - cHalfWidth < insetCache.left) {
				pad = Math.max(pad, insetCache.left - (centerX - cHalfWidth));
			}

			if (centerX + cHalfWidth > slider.getWidth() - insetCache.right) {
				pad = Math.max(pad, (centerX + cHalfWidth)
						- (slider.getWidth() - insetCache.right));
			}
		}
		return pad;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void calculateThumbLocation() {
		super.calculateThumbLocation();
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			lowerThumbRect.y += trackBorder;
		} else {
			lowerThumbRect.x += trackBorder;
		}
		Point mousePosition = slider.getMousePosition();
		if (mousePosition != null) {
			updateThumbState(mousePosition.x, mousePosition.y);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void setLowerThumbLocation(int x, int y) {
		super.setLowerThumbLocation(x, y);
		// Value rect is tied to the lower thumb location. We need to repaint
		// when the thumb repaints.
		slider.repaint(valueRect.x, valueRect.y, valueRect.width,
				valueRect.height);
		setThumbActiveLower(false);
		setThumbActiveUpper(false);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected int xPositionForValue(int value) {
		int min = slider.getMinimum();
		int max = slider.getMaximum();
		int trackLeft = trackRect.x + lowerThumbRect.width / 2 + trackBorder;
		int trackRight = trackRect.x + trackRect.width - lowerThumbRect.width
				/ 2 - trackBorder;
		int trackLength = trackRight - trackLeft;
		double valueRange = (double) max - (double) min;
		double pixelsPerValue = trackLength / valueRange;
		int xPosition;

		if (!drawInverted()) {
			xPosition = trackLeft;
			xPosition += Math.round(pixelsPerValue * ((double) value - min));
		} else {
			xPosition = trackRight;
			xPosition -= Math.round(pixelsPerValue * ((double) value - min));
		}

		xPosition = Math.max(trackLeft, xPosition);
		xPosition = Math.min(trackRight, xPosition);

		return xPosition;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected int yPositionForValue(int value, int trackY, int trackHeight) {
		int min = slider.getMinimum();
		int max = slider.getMaximum();
		int trackTop = trackY + lowerThumbRect.height / 2 + trackBorder;
		int trackBottom = trackY + trackHeight - lowerThumbRect.height / 2
				- trackBorder;
		int trackLength = trackBottom - trackTop;
		double valueRange = (double) max - (double) min;
		double pixelsPerValue = trackLength / valueRange;
		int yPosition;

		if (!drawInverted()) {
			yPosition = trackTop;
			yPosition += Math.round(pixelsPerValue * ((double) max - value));
		} else {
			yPosition = trackTop;
			yPosition += Math.round(pixelsPerValue * ((double) value - min));
		}

		yPosition = Math.max(trackTop, yPosition);
		yPosition = Math.min(trackBottom, yPosition);

		return yPosition;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected int valueForYPosition(int yPos) {
		int value;
		int minValue = slider.getMinimum();
		int maxValue = slider.getMaximum();
		int trackTop = trackRect.y + lowerThumbRect.height / 2 + trackBorder;
		int trackBottom = trackRect.y + trackRect.height
				- lowerThumbRect.height / 2 - trackBorder;
		int trackLength = trackBottom - trackTop;

		if (yPos <= trackTop) {
			value = drawInverted() ? minValue : maxValue;
		} else if (yPos >= trackBottom) {
			value = drawInverted() ? maxValue : minValue;
		} else {
			int distanceFromTrackTop = yPos - trackTop;
			double valueRange = (double) maxValue - (double) minValue;
			double valuePerPixel = valueRange / trackLength;
			int valueFromTrackTop = (int) Math.round(distanceFromTrackTop
					* valuePerPixel);
			value = drawInverted() ? minValue + valueFromTrackTop : maxValue
					- valueFromTrackTop;
		}
		return value;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected int valueForXPosition(int xPos) {
		int value;
		int minValue = slider.getMinimum();
		int maxValue = slider.getMaximum();
		int trackLeft = trackRect.x + lowerThumbRect.width / 2 + trackBorder;
		int trackRight = trackRect.x + trackRect.width - lowerThumbRect.width
				/ 2 - trackBorder;
		int trackLength = trackRight - trackLeft;

		if (xPos <= trackLeft) {
			value = drawInverted() ? maxValue : minValue;
		} else if (xPos >= trackRight) {
			value = drawInverted() ? minValue : maxValue;
		} else {
			int distanceFromTrackLeft = xPos - trackLeft;
			double valueRange = (double) maxValue - (double) minValue;
			double valuePerPixel = valueRange / trackLength;
			int valueFromTrackLeft = (int) Math.round(distanceFromTrackLeft
					* valuePerPixel);
			value = drawInverted() ? maxValue - valueFromTrackLeft : minValue
					+ valueFromTrackLeft;
		}
		return value;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected Dimension getThumbSize() {
		Dimension size = new Dimension();

		if (slider.getOrientation() == JSlider.VERTICAL) {
			size.width = thumbHeight;
			size.height = thumbWidth;
		} else {
			size.width = thumbWidth;
			size.height = thumbHeight;
		}
		return size;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	protected void recalculateIfInsetsChanged() {
		SynthContext context = getContext(slider);
		Insets newInsets = style.getInsets(context, null);
		Insets compInsets = slider.getInsets();
		newInsets.left += compInsets.left;
		newInsets.right += compInsets.right;
		newInsets.top += compInsets.top;
		newInsets.bottom += compInsets.bottom;
		if (!newInsets.equals(insetCache)) {
			insetCache = newInsets;
			calculateGeometry();
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public SynthContext getContext(JComponent c) {
		return getContextByState(c, getComponentState(c));
	}

	private SynthContext getContextByState(JComponent c, int state) {
		Region region = SynthLookAndFeel.getRegion(c);
		if (region == null)
			region = Region.SLIDER;
		return getContext(c, region, state);
	}

	private SynthContext getContextByRegion(JComponent c, Region subregion) {
		return getContext(c, subregion, getComponentState(c));
	}

	private SynthContext getThumbContext(JComponent c, boolean isLowerThumb) {
		return getContext(c, Region.SLIDER_THUMB,
				getThumbState(c, isLowerThumb));
	}

	private SynthContext getContext(JComponent c, Region subregion, int state) {
		SynthStyle style = null;

		if (subregion == Region.SLIDER_TRACK) {
			style = sliderTrackStyle;
		} else if (subregion == SliderRangeTrackRegion.INSTANCE) {
			style = sliderRangeTrackStyle;
		} else if (subregion == Region.SLIDER_THUMB) {
			style = sliderThumbStyle;
		} else {
			style = this.style;
			if (style == null) {
				style = SynthLookAndFeel.getStyle(c, subregion);
			}
		}

		assert c != null : "c != null";
		assert subregion != null : "subregion != null";
		assert style != null : "style != null";

		return new SynthContext(c, subregion, style, state);
	}

	private int getThumbState(JComponent c, boolean isLower) {
		if (c.isEnabled()) {
			if (slider.isLowerThumbFocused() == isLower) {
				int state = 0;
				if (isLower ? thumbActiveLower : thumbActiveUpper)
					state = MOUSE_OVER;
				if (thumbPressed)
					state = PRESSED;
				if (state == 0)
					return getComponentState(c);
				if (c.isFocusOwner())
					state |= FOCUSED;
				return state;
			} else {
				if (!thumbPressed)
					if (isLower ? thumbActiveLower : thumbActiveUpper)
						return MOUSE_OVER;
				return getComponentState(c) & ~FOCUSED;
			}
		}
		return getComponentState(c);
	}

	/**
	 * Notifies this UI delegate to repaint the specified component. This method
	 * paints the component background, then calls the
	 * {@link #paint(SynthContext,Graphics)} method.
	 * 
	 * <p>
	 * In general, this method does not need to be overridden by subclasses. All
	 * Look and Feel rendering code should reside in the {@code paint} method.
	 * 
	 * @param g
	 *            the {@code Graphics} object used for painting
	 * @param c
	 *            the component being painted
	 * @see #paint(SynthContext,Graphics)
	 */
	@Override
	public void update(Graphics g, JComponent c) {
		SynthContext context = getContext(c);
		update(context, g);
		getPainter(context).paintSliderBackground(context, g, 0, 0,
				c.getWidth(), c.getHeight(), slider.getOrientation());
		paint(context, g);
	}

	/**
	 * Paints the specified component according to the Look and Feel.
	 * <p>
	 * This method is not used by Synth Look and Feel. Painting is handled by
	 * the {@link #paint(SynthContext,Graphics)} method.
	 * 
	 * @param g
	 *            the {@code Graphics} object used for painting
	 * @param c
	 *            the component being painted
	 * @see #paint(SynthContext,Graphics)
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		SynthContext context = getContext(c);
		paint(context, g);
	}

	/**
	 * Paints the specified component.
	 * 
	 * @param context
	 *            context for the component being painted
	 * @param g
	 *            the {@code Graphics} object used for painting
	 * @see #update(Graphics,JComponent)
	 */
	protected void paint(SynthContext context, Graphics g) {
		recalculateIfInsetsChanged();
		recalculateIfOrientationChanged();
		Rectangle clip = g.getClipBounds();

		if (lastSize == null || !lastSize.equals(slider.getSize())) {
			calculateGeometry();
		}

		if (paintValue) {
			FontMetrics fm = SwingUtilities2.getFontMetrics(slider, g);
			int labelWidth = context
					.getStyle()
					.getGraphicsUtils(context)
					.computeStringWidth(context, g.getFont(), fm,
							"" + slider.getValue());
			valueRect.x = lowerThumbRect.x
					+ (lowerThumbRect.width - labelWidth) / 2;

			// For horizontal sliders, make sure value is not painted
			// outside slider bounds.
			if (slider.getOrientation() == JSlider.HORIZONTAL) {
				if (valueRect.x + labelWidth > insetCache.left
						+ contentRect.width) {
					valueRect.x = (insetCache.left + contentRect.width)
							- labelWidth;
				}
				valueRect.x = Math.max(valueRect.x, 0);
			}

			g.setColor(context.getStyle().getColor(context,
					ColorType.TEXT_FOREGROUND));
			context.getStyle()
					.getGraphicsUtils(context)
					.paintText(context, g, "" + slider.getValue(), valueRect.x,
							valueRect.y, -1);
		}

		if (slider.getPaintTrack() && clip.intersects(trackRect)) {
			SynthContext subcontext = getContextByRegion(slider,
					Region.SLIDER_TRACK);
			paintTrack(subcontext, g, trackRect);
		}

		if (slider.getPaintTrack() && clip.intersects(rangeTrackRect)) {
			SynthContext subcontext = getContextByRegion(slider,
					SliderRangeTrackRegion.INSTANCE);
			paintTrack(subcontext, g, rangeTrackRect);
		}

		if (clip.intersects(lowerThumbRect)) {
			SynthContext subcontext = getThumbContext(slider, true);
			paintThumb(subcontext, g, lowerThumbRect);
		}

		if (clip.intersects(upperThumbRect)) {
			SynthContext subcontext = getThumbContext(slider, false);
			paintThumb(subcontext, g, upperThumbRect);
		}

		if (slider.getPaintTicks() && clip.intersects(tickRect)) {
			paintTicks(g);
		}

		if (slider.getPaintLabels() && clip.intersects(labelRect)) {
			paintLabels(g);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void paintBorder(SynthContext context, Graphics g, int x, int y,
			int w, int h) {
		getPainter(context).paintSliderBorder(context, g, x, y, w, h,
				slider.getOrientation());
	}

	/**
	 * Paints the slider thumb.
	 * 
	 * @param context
	 *            context for the component being painted
	 * @param g
	 *            {@code Graphics} object used for painting
	 * @param thumbBounds
	 *            bounding box for the thumb
	 */
	protected void paintThumb(SynthContext context, Graphics g,
			Rectangle thumbBounds) {
		int orientation = slider.getOrientation();
		updateSubregion(context, g, thumbBounds);
		getPainter(context).paintSliderThumbBackground(context, g,
				thumbBounds.x, thumbBounds.y, thumbBounds.width,
				thumbBounds.height, orientation);
		getPainter(context).paintSliderThumbBorder(context, g, thumbBounds.x,
				thumbBounds.y, thumbBounds.width, thumbBounds.height,
				orientation);
	}

	/**
	 * Paints the slider track.
	 * 
	 * @param context
	 *            context for the component being painted
	 * @param g
	 *            {@code Graphics} object used for painting
	 * @param trackBounds
	 *            bounding box for the track
	 */
	protected void paintTrack(SynthContext context, Graphics g,
			Rectangle trackBounds) {
		int orientation = slider.getOrientation();
		updateSubregion(context, g, trackBounds);
		getPainter(context).paintSliderTrackBackground(context, g,
				trackBounds.x, trackBounds.y, trackBounds.width,
				trackBounds.height, orientation);
		getPainter(context).paintSliderTrackBorder(context, g, trackBounds.x,
				trackBounds.y, trackBounds.width, trackBounds.height,
				orientation);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (shouldUpdateStyle(e)) {
			updateStyle((JSlider) e.getSource());
		}
	}

	// ////////////////////////////////////////////////
	// / Track Listener Class
	// ////////////////////////////////////////////////
	/**
	 * Track mouse movements.
	 */
	private class SynthTrackListener extends RangeTrackListener {

		@Override
		public void mouseExited(MouseEvent e) {
			super.mouseExited(e);
			setThumbActiveLower(false);
			setThumbActiveUpper(false);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			setThumbPressed(lowerThumbRect.contains(e.getX(), e.getY())
					|| upperThumbRect.contains(e.getX(), e.getY()));
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e);
			updateThumbState(e.getX(), e.getY(), false);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			super.mouseDragged(e);
			if (slider.isEnabled() && isDragging()
					&& slider.getValueIsAdjusting()) {
				updateThumbState(e.getX(), e.getY());
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			updateThumbState(e.getX(), e.getY());
		}
	}

	// synth library method

	/**
	 * Returns the component state for the specified component. This should only
	 * be used for Components that don't have any special state beyond that of
	 * ENABLED, DISABLED or FOCUSED. For example, buttons shouldn't call into
	 * this method.
	 */
	private static int getComponentState(Component c) {
		if (c.isEnabled()) {
			if (c.isFocusOwner()) {
				return SynthUI.ENABLED | SynthUI.FOCUSED;
			}
			return SynthUI.ENABLED;
		}
		return SynthUI.DISABLED;
	}

	private SynthContext getInitialContext(JComponent c, Region subregion,
			int state, SynthUI ui) {
		SynthStyle style = SynthLookAndFeel.getStyle(c, subregion);

		assert c != null : "c != null";
		assert subregion != null : "subregion != null";
		assert style != null : "style != null";

		return new SynthContext(c, subregion, style, state);
	}

	/**
	 * A convience method that will reset the Style of StyleContext if
	 * necessary.
	 * 
	 * @return new context containing new style
	 */
	private static SynthContext updateStyle(SynthContext context, SynthUI ui) {
		SynthStyle newStyle = SynthLookAndFeel.getStyle(context.getComponent(),
				context.getRegion());
		SynthStyle oldStyle = context.getStyle();

		if (newStyle != oldStyle) {
			if (oldStyle != null) {
				oldStyle.uninstallDefaults(context);
			}
			context = new SynthContext(context.getComponent(),
					context.getRegion(), newStyle, context.getComponentState());
			newStyle.installDefaults(context);
		}
		return context;
	}

	private static SynthPainter NULL_PAINTER = new SynthPainter() {
	};

	/**
	 * Convenience method to get the Painter from the current SynthStyle. This
	 * will NEVER return null.
	 */
	private static SynthPainter getPainter(SynthContext context) {
		SynthPainter painter = context.getStyle().getPainter(context);

		if (painter != null) {
			return painter;
		}
		return NULL_PAINTER;
	}

	/**
	 * Returns true if the Style should be updated in response to the specified
	 * PropertyChangeEvent. This forwards to
	 * <code>shouldUpdateStyleOnAncestorChanged</code> as necessary.
	 */
	private static boolean shouldUpdateStyle(PropertyChangeEvent event) {
		LookAndFeel laf = UIManager.getLookAndFeel();
		String eName = event.getPropertyName();
		if ("name".equals(eName) || "componentOrientation".equals(eName)) {
			return true;
		}
		if ("ancestor".equals(eName) && event.getNewValue() != null) {
			if (laf instanceof SynthLookAndFeel)
				return ((SynthLookAndFeel) laf)
						.shouldUpdateStyleOnAncestorChanged();
			else
				return false;
		}
		return false;
	}

	/**
	 * A convenience method that handles painting of the background. All SynthUI
	 * implementations should override update and invoke this method.
	 */
	private static void update(SynthContext state, Graphics g) {
		paintRegion(state, g, null);
	}

	/**
	 * A convenience method that handles painting of the background for
	 * subregions. All SynthUI's that have subregions should invoke this method,
	 * than paint the foreground.
	 */
	static void updateSubregion(SynthContext state, Graphics g, Rectangle bounds) {
		paintRegion(state, g, bounds);
	}

	private static void paintRegion(SynthContext state, Graphics g,
			Rectangle bounds) {
		JComponent c = state.getComponent();
		SynthStyle style = state.getStyle();
		int x, y, width, height;

		if (bounds == null) {
			x = 0;
			y = 0;
			width = c.getWidth();
			height = c.getHeight();
		} else {
			x = bounds.x;
			y = bounds.y;
			width = bounds.width;
			height = bounds.height;
		}

		// Fill in the background, if necessary.
		boolean subregion = state.getRegion().isSubregion();
		if ((subregion && style.isOpaque(state))
				|| (!subregion && c.isOpaque())) {
			g.setColor(style.getColor(state, ColorType.BACKGROUND));
			g.fillRect(x, y, width, height);
		}
	}
}
