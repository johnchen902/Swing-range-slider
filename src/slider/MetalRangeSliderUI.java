package slider;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalSliderUI;
import javax.swing.plaf.metal.OceanTheme;

/**
 * A Java L&F UI delegation of RangeSlider.
 * 
 * @see MetalSliderUI
 * @author johnchen902
 */
public class MetalRangeSliderUI extends BasicRangeSliderUI {

	protected final int TICK_BUFFER = 4;
	protected boolean filledSlider = false;
	private int safeLength;

	/**
	 * A default horizontal thumb <code>Icon</code>. This field might not be
	 * used. To change the <code>Icon</code> used by this delegate directly set
	 * it using the <code>Slider.horizontalThumbIcon</code> UIManager property.
	 */
	protected static Icon horizThumbIcon;

	/**
	 * A default vertical thumb <code>Icon</code>. This field might not be used.
	 * To change the <code>Icon</code> used by this delegate directly set it
	 * using the <code>Slider.verticalThumbIcon</code> UIManager property.
	 */
	protected static Icon vertThumbIcon;

	private static Icon SAFE_HORIZ_THUMB_ICON;
	private static Icon SAFE_VERT_THUMB_ICON;

	protected final String SLIDER_FILL = "JSlider.isFilled";

	public static ComponentUI createUI(JComponent c) {
		return new MetalRangeSliderUI();
	}

	public MetalRangeSliderUI() {
		super(null);
	}

	private static Icon getHorizThumbIcon() {
		if (System.getSecurityManager() != null) {
			return SAFE_HORIZ_THUMB_ICON;
		} else {
			return horizThumbIcon;
		}
	}

	private static Icon getVertThumbIcon() {
		if (System.getSecurityManager() != null) {
			return SAFE_VERT_THUMB_ICON;
		} else {
			return vertThumbIcon;
		}
	}

	@Override
	public void installUI(JComponent c) {
		safeLength = (int) UIManager.get("Slider.majorTickLength");
		horizThumbIcon = SAFE_HORIZ_THUMB_ICON = UIManager
				.getIcon("Slider.horizontalThumbIcon");
		vertThumbIcon = SAFE_VERT_THUMB_ICON = UIManager
				.getIcon("Slider.verticalThumbIcon");

		super.installUI(c);

		scrollListener.setScrollByBlock(false);

		prepareFilledSliderField();
	}

	@Override
	protected PropertyChangeListener createPropertyChangeListener(JSlider slider) {
		return new MetalPropertyListener();
	}

	protected class MetalPropertyListener extends
			BasicRangeSliderUI.PropertyChangeHandler {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			super.propertyChange(e);

			if (e.getPropertyName().equals(SLIDER_FILL)) {
				prepareFilledSliderField();
			}
		}
	}

	private void prepareFilledSliderField() {
		// Use true for Ocean theme
		filledSlider = MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme;

		Object sliderFillProp = slider.getClientProperty(SLIDER_FILL);

		if (sliderFillProp != null) {
			filledSlider = ((Boolean) sliderFillProp).booleanValue();
		}
	}

	@Override
	public void paintThumb(Graphics g) {
		Rectangle knobBounds = thumbRect;

		g.translate(knobBounds.x, knobBounds.y);

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			getHorizThumbIcon().paintIcon(slider, g, 0, 0);
		} else {
			getVertThumbIcon().paintIcon(slider, g, 0, 0);
		}

		g.translate(-knobBounds.x, -knobBounds.y);
	}

	/**
	 * Returns a rectangle enclosing the track that will be painted.
	 */
	private Rectangle getPaintTrackRect() {
		int trackLeft = 0, trackRight, trackTop = 0, trackBottom;
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			trackBottom = (trackRect.height - 1) - getThumbOverhang();
			trackTop = trackBottom - (getTrackWidth() - 1);
			trackRight = trackRect.width - 1;
		} else {
			if (slider.getComponentOrientation().isLeftToRight()) {
				trackLeft = (trackRect.width - getThumbOverhang())
						- getTrackWidth();
				trackRight = (trackRect.width - getThumbOverhang()) - 1;
			} else {
				trackLeft = getThumbOverhang();
				trackRight = getThumbOverhang() + getTrackWidth() - 1;
			}
			trackBottom = trackRect.height - 1;
		}
		return new Rectangle(trackRect.x + trackLeft, trackRect.y + trackTop,
				trackRight - trackLeft, trackBottom - trackTop);
	}

	@Override
	public void paintTrack(Graphics g) {
		if (MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme) {
			oceanPaintTrack(g);
			return;
		}
		boolean leftToRight = slider.getComponentOrientation().isLeftToRight();

		g.translate(trackRect.x, trackRect.y);

		int trackLeft = 0;
		int trackTop = 0;
		int trackRight;
		int trackBottom;

		// Draw the track
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			trackBottom = (trackRect.height - 1) - getThumbOverhang();
			trackTop = trackBottom - (getTrackWidth() - 1);
			trackRight = trackRect.width - 1;
		} else {
			if (leftToRight) {
				trackLeft = (trackRect.width - getThumbOverhang())
						- getTrackWidth();
				trackRight = (trackRect.width - getThumbOverhang()) - 1;
			} else {
				trackLeft = getThumbOverhang();
				trackRight = getThumbOverhang() + getTrackWidth() - 1;
			}
			trackBottom = trackRect.height - 1;
		}

		if (slider.isEnabled()) {
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawRect(trackLeft, trackTop, (trackRight - trackLeft) - 1,
					(trackBottom - trackTop) - 1);

			g.setColor(MetalLookAndFeel.getControlHighlight());
			g.drawLine(trackLeft + 1, trackBottom, trackRight, trackBottom);
			g.drawLine(trackRight, trackTop + 1, trackRight, trackBottom);

			g.setColor(MetalLookAndFeel.getControlShadow());
			g.drawLine(trackLeft + 1, trackTop + 1, trackRight - 2,
					trackTop + 1);
			g.drawLine(trackLeft + 1, trackTop + 1, trackLeft + 1,
					trackBottom - 2);
		} else {
			g.setColor(MetalLookAndFeel.getControlShadow());
			g.drawRect(trackLeft, trackTop, (trackRight - trackLeft) - 1,
					(trackBottom - trackTop) - 1);
		}

		// Draw the fill
		if (filledSlider) {
			int middleOfThumb;
			int middleOfUpperThumb;
			int fillTop;
			int fillLeft;
			int fillBottom;
			int fillRight;

			if (slider.getOrientation() == JSlider.HORIZONTAL) {
				middleOfThumb = thumbRect.x + (thumbRect.width / 2);
				middleOfThumb -= trackRect.x;

				middleOfUpperThumb = upperThumbRect.x
						+ (upperThumbRect.width / 2);
				middleOfUpperThumb -= trackRect.x;

				fillTop = !slider.isEnabled() ? trackTop : trackTop + 1;
				fillBottom = !slider.isEnabled() ? trackBottom - 1
						: trackBottom - 2;

				fillLeft = Math.min(middleOfThumb, middleOfUpperThumb);
				fillRight = Math.max(middleOfThumb, middleOfUpperThumb);
			} else {
				middleOfThumb = thumbRect.y + (thumbRect.height / 2);
				middleOfThumb -= trackRect.y;

				middleOfUpperThumb = upperThumbRect.y
						+ (upperThumbRect.height / 2);
				middleOfUpperThumb -= trackRect.y;

				fillLeft = !slider.isEnabled() ? trackLeft : trackLeft + 1;
				fillRight = !slider.isEnabled() ? trackRight - 1
						: trackRight - 2;

				fillTop = Math.min(middleOfThumb, middleOfUpperThumb);
				fillBottom = Math.max(middleOfThumb, middleOfUpperThumb);
			}

			if (slider.isEnabled()) {
				g.setColor(slider.getBackground());
				g.drawLine(fillLeft, fillTop, fillRight, fillTop);
				g.drawLine(fillLeft, fillTop, fillLeft, fillBottom);

				g.setColor(MetalLookAndFeel.getControlShadow());
				g.fillRect(fillLeft + 1, fillTop + 1, fillRight - fillLeft,
						fillBottom - fillTop);
			} else {
				g.setColor(MetalLookAndFeel.getControlShadow());
				g.fillRect(fillLeft, fillTop, fillRight - fillLeft, fillBottom
						- fillTop);
			}
		}

		g.translate(-trackRect.x, -trackRect.y);
	}

	private void oceanPaintTrack(Graphics g) {
		boolean leftToRight = slider.getComponentOrientation().isLeftToRight();
		Color sliderAltTrackColor = (Color) UIManager
				.get("Slider.altTrackColor");

		// Translate to the origin of the painting rectangle
		Rectangle paintRect = getPaintTrackRect();
		g.translate(paintRect.x, paintRect.y);

		// Width and height of the painting rectangle.
		int w = paintRect.width;
		int h = paintRect.height;

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int middleOfThumb = thumbRect.x + thumbRect.width / 2 - paintRect.x;
			int middleOfUpperThumb = upperThumbRect.x + upperThumbRect.width
					/ 2 - paintRect.x;

			if (slider.isEnabled()) {
				int fillMinX;
				int fillMaxX;

				g.setColor(MetalLookAndFeel.getControlDarkShadow());
				g.drawRect(0, 0, w - 1, h - 1);

				g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
				g.drawRect(Math.min(middleOfThumb, middleOfUpperThumb), 0,
						Math.abs(middleOfThumb - middleOfUpperThumb) - 1, h - 1);

				if (filledSlider) {
					g.setColor(MetalLookAndFeel.getPrimaryControlShadow());
					fillMinX = Math.min(middleOfThumb, middleOfUpperThumb);
					fillMaxX = Math.max(middleOfThumb, middleOfUpperThumb);
					g.drawLine(0, 1, w, 1);
					if (h == 6) {
						g.setColor(MetalLookAndFeel.getWhite());
						g.drawLine(fillMinX, 1, fillMaxX, 1);
						g.setColor(sliderAltTrackColor);
						g.drawLine(fillMinX, 2, fillMaxX, 2);
						g.setColor(MetalLookAndFeel.getControlShadow());
						g.drawLine(fillMinX, 3, fillMaxX, 3);
						g.setColor(MetalLookAndFeel.getPrimaryControlShadow());
						g.drawLine(fillMinX, 4, fillMaxX, 4);
					}
				}
			} else {
				g.setColor(MetalLookAndFeel.getControlShadow());

				g.drawRect(0, 0, w - 1, h - 1);

				if (filledSlider) {
					g.fillRect(Math.min(middleOfThumb, middleOfUpperThumb), 0,
							Math.abs(middleOfThumb - middleOfUpperThumb) - 1,
							h - 1);
				}
			}
		} else {
			int middleOfThumb = thumbRect.y + (thumbRect.height / 2)
					- paintRect.y;
			int middleOfUpperThumb = upperThumbRect.y
					+ (upperThumbRect.height / 2) - paintRect.y;

			if (slider.isEnabled()) {
				int fillMinY;
				int fillMaxY;

				g.setColor(MetalLookAndFeel.getControlDarkShadow());
				g.drawRect(0, 0, w - 1, h - 1);

				g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
				g.drawRect(0, Math.min(middleOfThumb, middleOfUpperThumb),
						w - 1, Math.abs(middleOfThumb - middleOfUpperThumb) - 1);

				if (filledSlider) {
					g.setColor(MetalLookAndFeel.getPrimaryControlShadow());
					fillMinY = Math.min(middleOfThumb, middleOfUpperThumb);
					fillMaxY = Math.max(middleOfThumb, middleOfUpperThumb);
					if (leftToRight) {
						g.drawLine(1, 0, 1, h);
					} else {
						g.drawLine(w - 2, 0, w - 2, h);
					}
					if (w == 6) {
						g.setColor(leftToRight ? MetalLookAndFeel.getWhite()
								: MetalLookAndFeel.getPrimaryControlShadow());
						g.drawLine(1, fillMinY, 1, fillMaxY);
						g.setColor(leftToRight ? sliderAltTrackColor
								: MetalLookAndFeel.getControlShadow());
						g.drawLine(2, fillMinY, 2, fillMaxY);
						g.setColor(leftToRight ? MetalLookAndFeel
								.getControlShadow() : sliderAltTrackColor);
						g.drawLine(3, fillMinY, 3, fillMaxY);
						g.setColor(leftToRight ? MetalLookAndFeel
								.getPrimaryControlShadow() : MetalLookAndFeel
								.getWhite());
						g.drawLine(4, fillMinY, 4, fillMaxY);
					}
				}
			} else {
				g.setColor(MetalLookAndFeel.getControlShadow());

				g.drawRect(0, 0, w - 1, h - 1);

				if (filledSlider) {
					g.fillRect(0, Math.min(middleOfThumb, middleOfUpperThumb),
							w - 1,
							Math.abs(middleOfThumb - middleOfUpperThumb) - 1);
				}
			}
		}

		g.translate(-paintRect.x, -paintRect.y);
	}

	@Override
	public void paintFocus(Graphics g) {
	}

	@Override
	protected Dimension getThumbSize() {
		Dimension size = new Dimension();

		if (slider.getOrientation() == JSlider.VERTICAL) {
			size.width = getVertThumbIcon().getIconWidth();
			size.height = getVertThumbIcon().getIconHeight();
		} else {
			size.width = getHorizThumbIcon().getIconWidth();
			size.height = getHorizThumbIcon().getIconHeight();
		}

		return size;
	}

	/**
	 * Gets the height of the tick area for horizontal sliders and the width of
	 * the tick area for vertical sliders. BasicSliderUI uses the returned value
	 * to determine the tick area rectangle.
	 */
	@Override
	public int getTickLength() {
		return slider.getOrientation() == JSlider.HORIZONTAL ? safeLength
				+ TICK_BUFFER + 1 : safeLength + TICK_BUFFER + 3;
	}

	/**
	 * Returns the shorter dimension of the track.
	 */
	protected int getTrackWidth() {
		// This strange calculation is here to keep the
		// track in proportion to the thumb.
		final double kIdealTrackWidth = 7.0;
		final double kIdealThumbHeight = 16.0;
		final double kWidthScalar = kIdealTrackWidth / kIdealThumbHeight;

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			return (int) (kWidthScalar * thumbRect.height);
		} else {
			return (int) (kWidthScalar * thumbRect.width);
		}
	}

	/**
	 * Returns the longer dimension of the slide bar. (The slide bar is only the
	 * part that runs directly under the thumb)
	 */
	protected int getTrackLength() {
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			return trackRect.width;
		}
		return trackRect.height;
	}

	/**
	 * Returns the amount that the thumb goes past the slide bar.
	 */
	protected int getThumbOverhang() {
		return (int) (getThumbSize().getHeight() - getTrackWidth()) / 2;
	}

	@Override
	protected void scrollDueToClickInTrack(int dir) {
		scrollByUnit(dir);
	}

	@Override
	protected void paintMinorTickForHorizSlider(Graphics g,
			Rectangle tickBounds, int x) {
		g.setColor(slider.isEnabled() ? slider.getForeground()
				: MetalLookAndFeel.getControlShadow());
		g.drawLine(x, TICK_BUFFER, x, TICK_BUFFER + (safeLength / 2));
	}

	@Override
	protected void paintMajorTickForHorizSlider(Graphics g,
			Rectangle tickBounds, int x) {
		g.setColor(slider.isEnabled() ? slider.getForeground()
				: MetalLookAndFeel.getControlShadow());
		g.drawLine(x, TICK_BUFFER, x, TICK_BUFFER + (safeLength - 1));
	}

	@Override
	protected void paintMinorTickForVertSlider(Graphics g,
			Rectangle tickBounds, int y) {
		g.setColor(slider.isEnabled() ? slider.getForeground()
				: MetalLookAndFeel.getControlShadow());

		if (slider.getComponentOrientation().isLeftToRight()) {
			g.drawLine(TICK_BUFFER, y, TICK_BUFFER + (safeLength / 2), y);
		} else {
			g.drawLine(0, y, safeLength / 2, y);
		}
	}

	@Override
	protected void paintMajorTickForVertSlider(Graphics g,
			Rectangle tickBounds, int y) {
		g.setColor(slider.isEnabled() ? slider.getForeground()
				: MetalLookAndFeel.getControlShadow());

		if (slider.getComponentOrientation().isLeftToRight()) {
			g.drawLine(TICK_BUFFER, y, TICK_BUFFER + safeLength, y);
		} else {
			g.drawLine(0, y, safeLength, y);
		}
	}
}
