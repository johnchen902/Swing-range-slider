package slider;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import com.sun.java.swing.plaf.motif.MotifBorders;
import com.sun.java.swing.plaf.motif.MotifSliderUI;

/**
 * Motif Range Slider
 * <p>
 * <strong>Warning:</strong> Serialized objects of this class will not be
 * compatible with future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Swing.
 * 
 * @see MotifSliderUI
 * @author johnchen902
 */
public class MotifRangeSliderUI extends BasicRangeSliderUI {
	static final Dimension PREFERRED_HORIZONTAL_SIZE = new Dimension(164, 15);
	static final Dimension PREFERRED_VERTICAL_SIZE = new Dimension(15, 164);

	static final Dimension MINIMUM_HORIZONTAL_SIZE = new Dimension(43, 15);
	static final Dimension MINIMUM_VERTICAL_SIZE = new Dimension(15, 43);

	/**
	 * MotifRangeSliderUI Constructor
	 */
	public MotifRangeSliderUI() {
	}

	/**
	 * create a MotifRangeSliderUI object
	 */
	public static ComponentUI createUI(JComponent b) {
		return new MotifRangeSliderUI();
	}

	@Override
	protected Dimension getPreferredHorizontalSize() {
		return PREFERRED_HORIZONTAL_SIZE;
	}

	@Override
	protected Dimension getPreferredVerticalSize() {
		return PREFERRED_VERTICAL_SIZE;
	}

	@Override
	protected Dimension getMinimumHorizontalSize() {
		return MINIMUM_HORIZONTAL_SIZE;
	}

	@Override
	protected Dimension getMinimumVerticalSize() {
		return MINIMUM_VERTICAL_SIZE;
	}

	@Override
	protected Dimension getThumbSize() {
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			return new Dimension(30 + 2, 15 + 2);
		} else {
			return new Dimension(15 + 2, 30 + 2);
		}
	}

	@Override
	protected void paintFocus(Graphics g) {
	}

	@Override
	protected void paintTrack(Graphics g) {
	}

	@Override
	protected void paintThumb(Graphics g, boolean isLower) {
		Rectangle knobBounds = isLower ? lowerThumbRect : upperThumbRect;

		int x = knobBounds.x + 1;
		int y = knobBounds.y + 1;
		int w = knobBounds.width - 2;
		int h = knobBounds.height - 2;

		if (slider.isEnabled()) {
			g.setColor(slider.getForeground());
		} else {
			g.setColor(slider.getForeground().darker());
		}

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			g.translate(x, y - 1);

			// fill
			g.fillRect(0, 1, w, h - 1);

			// highlight
			g.setColor(getHighlightColor());
			g.drawLine(0, 1, w - 1, 1); // top
			g.drawLine(0, 1, 0, h); // left
			g.drawLine(w / 2, 2, w / 2, h - 1); // center

			// shadow
			g.setColor(getShadowColor());
			g.drawLine(0, h, w - 1, h); // bottom
			g.drawLine(w - 1, 1, w - 1, h); // right
			g.drawLine(w / 2 - 1, 2, w / 2 - 1, h); // center

			g.translate(-x, -(y - 1));
		} else {
			g.translate(x - 1, 0);

			// fill
			g.fillRect(1, y, w - 1, h);

			// highlight
			g.setColor(getHighlightColor());
			g.drawLine(1, y, w, y); // top
			g.drawLine(1, y + 1, 1, y + h - 1); // left
			g.drawLine(2, y + h / 2, w - 1, y + h / 2); // center

			// shadow
			g.setColor(getShadowColor());
			g.drawLine(2, y + h - 1, w, y + h - 1); // bottom
			g.drawLine(w, y + h - 1, w, y); // right
			g.drawLine(2, y + h / 2 - 1, w - 1, y + h / 2 - 1); // center

			g.translate(-(x - 1), 0);
		}

		if (slider.hasFocus() && (isLower == slider.isLowerThumbFocused())) {
			MotifBorders.drawBezel(g, knobBounds.x, knobBounds.y,
					knobBounds.width, knobBounds.height, false, true,
					new Color(0, true), getHighlightColor(), getShadowColor(),
					UIManager.getColor("activeCaptionBorder"));
		}
	}
}
