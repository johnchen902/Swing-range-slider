package slider;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.plaf.nimbus.AbstractRegionPainter;

/**
 * @see javax.swing.plaf.nimbus.SliderTrackPainter
 * @author johnchen902
 */
final class SliderRangeTrackPainter extends AbstractRegionPainter {

	private boolean enabled;
	private PaintContext ctx;
	private RoundRectangle2D roundRect = new RoundRectangle2D.Float();

	// color used in "disabled"
	private Color color1 = decodeColor("nimbusSelection", 0.000f,
			-0.410526316f, 0.25490195f, -245);
	private Color color2 = decodeColor("nimbusSelection", 0.0055555105f,
			-0.361265234f, 0.05098039f, 0);
	private Color color3 = decodeColor("nimbusSelection", 0.01010108f,
			-0.359835073f, 0.10588235f, 0);
	private Color color4 = decodeColor("nimbusSelection", -0.01111114f,
			-0.361982628f, 0.062745094f, 0);
	private Color color5 = decodeColor("nimbusSelection", -0.00505054f,
			-0.358639523f, 0.086274505f, 0);

	// color used in "enabled"
	private Color color6 = decodeColor("nimbusSelection", 0.0f, -0.110526316f,
			0.25490195f, -111);
	private Color color7 = decodeColor("nimbusSelection", 0.0f, -0.034093194f,
			-0.12941176f, 0);
	private Color color8 = decodeColor("nimbusSelection", 0.01111114f,
			-0.023821115f, -0.06666666f, 0);
	private Color color9 = decodeColor("nimbusSelection", -0.008547008f,
			-0.03314536f, -0.086274505f, 0);
	private Color color10 = decodeColor("nimbusSelection", 0.004273474f,
			-0.040256046f, -0.019607842f, 0);
	private Color color11 = decodeColor("nimbusSelection", 0.0f, -0.03626889f,
			0.04705882f, 0);

	private static class MyPaintContext extends PaintContext {
		public MyPaintContext(boolean enabled) {
			super(new Insets(6, 5, 6, 5), new Dimension(23, 17), false,
					enabled ? CacheMode.FIXED_SIZES
							: CacheMode.NINE_SQUARE_SCALE, enabled ? 1.0
							: 1.0 / 0.0, enabled ? 1.0 : 0.0);
		}
	}

	public SliderRangeTrackPainter(boolean enabled) {
		super();
		this.enabled = enabled;
		this.ctx = new MyPaintContext(enabled);
	}

	@Override
	protected void doPaint(Graphics2D g, JComponent c, int width, int height,
			Object[] extendedCacheKeys) {
		if (enabled)
			paintBackgroundEnabled(g);
		else
			paintBackgroundDisabled(g);
	}

	@Override
	protected final PaintContext getPaintContext() {
		return ctx;
	}

	private void paintBackgroundDisabled(Graphics2D g) {
		roundRect = decodeRoundRect1();
		g.setPaint(color1);
		g.fill(roundRect);
		roundRect = decodeRoundRect2();
		g.setPaint(decodeGradient1(roundRect));
		g.fill(roundRect);
		roundRect = decodeRoundRect3();
		g.setPaint(decodeGradient2(roundRect));
		g.fill(roundRect);
	}

	private void paintBackgroundEnabled(Graphics2D g) {
		roundRect = decodeRoundRect4();
		g.setPaint(color6);
		g.fill(roundRect);
		roundRect = decodeRoundRect2();
		g.setPaint(decodeGradient3(roundRect));
		g.fill(roundRect);
		roundRect = decodeRoundRect5();
		g.setPaint(decodeGradient4(roundRect));
		g.fill(roundRect);
	}

	private RoundRectangle2D decodeRoundRect1() {
		roundRect.setRoundRect(decodeX(0.2f), // x
				decodeY(1.6f), // y
				decodeX(2.8f) - decodeX(0.2f), // width
				decodeY(2.8333333f) - decodeY(1.6f), // height
				8.705882f, 8.705882f); // rounding
		return roundRect;
	}

	private RoundRectangle2D decodeRoundRect2() {
		roundRect.setRoundRect(decodeX(0.0f), // x
				decodeY(1.0f), // y
				decodeX(3.0f) - decodeX(0.0f), // width
				decodeY(2.0f) - decodeY(1.0f), // height
				4.9411764f, 4.9411764f); // rounding
		return roundRect;
	}

	private RoundRectangle2D decodeRoundRect3() {
		roundRect.setRoundRect(decodeX(0.29411763f), // x
				decodeY(1.2f), // y
				decodeX(2.7058823f) - decodeX(0.29411763f), // width
				decodeY(2.0f) - decodeY(1.2f), // height
				4.0f, 4.0f); // rounding
		return roundRect;
	}

	private RoundRectangle2D decodeRoundRect4() {
		roundRect.setRoundRect(decodeX(0.2f), // x
				decodeY(1.6f), // y
				decodeX(2.8f) - decodeX(0.2f), // width
				decodeY(2.1666667f) - decodeY(1.6f), // height
				8.705882f, 8.705882f); // rounding
		return roundRect;
	}

	private RoundRectangle2D decodeRoundRect5() {
		roundRect.setRoundRect(decodeX(0.28823528f), // x
				decodeY(1.2f), // y
				decodeX(2.7f) - decodeX(0.28823528f), // width
				decodeY(2.0f) - decodeY(1.2f), // height
				4.0f, 4.0f); // rounding
		return roundRect;
	}

	private Paint decodeGradient1(Shape s) {
		Rectangle2D bounds = s.getBounds2D();
		float x = (float) bounds.getX();
		float y = (float) bounds.getY();
		float w = (float) bounds.getWidth();
		float h = (float) bounds.getHeight();
		return decodeGradient(
				(0.25f * w) + x,
				(0.07647059f * h) + y,
				(0.25f * w) + x,
				(0.9117647f * h) + y,
				new float[] { 0.0f, 0.5f, 1.0f },
				new Color[] { color2, decodeColor(color2, color3, 0.5f), color3 });
	}

	private Paint decodeGradient2(Shape s) {
		Rectangle2D bounds = s.getBounds2D();
		float x = (float) bounds.getX();
		float y = (float) bounds.getY();
		float w = (float) bounds.getWidth();
		float h = (float) bounds.getHeight();
		return decodeGradient((0.25f * w) + x, (0.0f * h) + y, (0.25f * w) + x,
				(1.0f * h) + y, new float[] { 0.0f, 0.13770053f, 0.27540106f,
						0.63770056f, 1.0f },
				new Color[] { color4, decodeColor(color4, color5, 0.5f),
						color5, decodeColor(color5, color3, 0.5f), color3 });
	}

	private Paint decodeGradient3(Shape s) {
		Rectangle2D bounds = s.getBounds2D();
		float x = (float) bounds.getX();
		float y = (float) bounds.getY();
		float w = (float) bounds.getWidth();
		float h = (float) bounds.getHeight();
		return decodeGradient(
				(0.25f * w) + x,
				(0.07647059f * h) + y,
				(0.25f * w) + x,
				(0.9117647f * h) + y,
				new float[] { 0.0f, 0.5f, 1.0f },
				new Color[] { color7, decodeColor(color7, color8, 0.5f), color8 });
	}

	private Paint decodeGradient4(Shape s) {
		Rectangle2D bounds = s.getBounds2D();
		float x = (float) bounds.getX();
		float y = (float) bounds.getY();
		float w = (float) bounds.getWidth();
		float h = (float) bounds.getHeight();
		return decodeGradient((0.25f * w) + x, (0.0f * h) + y, (0.25f * w) + x,
				(1.0f * h) + y, new float[] { 0.0f, 0.13770053f, 0.27540106f,
						0.4906417f, 0.7058824f }, new Color[] { color9,
						decodeColor(color9, color10, 0.5f), color10,
						decodeColor(color10, color11, 0.5f), color11 });
	}
}
