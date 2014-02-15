package slider;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.ComponentUI;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;

/**
 * Windows rendition of the component.
 * <p>
 * <strong>Warning:</strong> Serialized objects of this class will not be
 * compatible with future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Swing. A future release of Swing will provide support for
 * long term persistence.
 * 
 * @see WindowsSliderUI
 * @author johnchen902
 */
public class WindowsRangeSliderUI extends BasicRangeSliderUI {
	private boolean rolloverLower = false;
	private boolean rolloverUpper = false;
	private boolean pressed = false;

	public static ComponentUI createUI(JComponent b) {
		return new WindowsRangeSliderUI();
	}

	/**
	 * Overrides to return a private track listener subclass which handles the
	 * HOT, PRESSED, and FOCUSED states.
	 * 
	 * @since 1.6
	 */
	@Override
	protected RangeTrackListener createTrackListener(JSlider slider) {
		return new WindowsTrackListener();
	}

	private class WindowsTrackListener extends RangeTrackListener {

		@Override
		public void mouseMoved(MouseEvent e) {
			updateRollover(lowerThumbRect.contains(e.getX(), e.getY()),
					upperThumbRect.contains(e.getX(), e.getY()));
			super.mouseMoved(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			updateRollover(lowerThumbRect.contains(e.getX(), e.getY()),
					upperThumbRect.contains(e.getX(), e.getY()));
			super.mouseEntered(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			updateRollover(false, false);
			super.mouseExited(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			updatePressed(lowerThumbRect.contains(e.getX(), e.getY())
					|| upperThumbRect.contains(e.getX(), e.getY()));
			super.mousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			updatePressed(false);
			super.mouseReleased(e);
		}

		public void updatePressed(boolean newPressed) {
			// You can't press a disabled slider
			if (!slider.isEnabled()) {
				return;
			}
			if (pressed != newPressed) {
				pressed = newPressed;
				slider.repaint(lowerThumbRect);
				slider.repaint(upperThumbRect);
			}
		}

		public void updateRollover(boolean newRolloverLower,
				boolean newRolloverUpper) {
			// You can't have a rollover on a disabled slider
			if (!slider.isEnabled()) {
				return;
			}
			if (rolloverLower != newRolloverLower) {
				rolloverLower = newRolloverLower;
				slider.repaint(lowerThumbRect);
			}
			if (rolloverUpper != newRolloverUpper) {
				rolloverUpper = newRolloverUpper;
				slider.repaint(upperThumbRect);
			}
		}
	}

	@Override
	protected void paintTrack(Graphics g) {
		XPStyle xp = XPStyle.getXP();
		if (xp != null) {
			boolean vertical = (slider.getOrientation() == JSlider.VERTICAL);
			Object part = accessTMSchema("Part", vertical ? "TKP_TRACKVERT"
					: "TKP_TRACK");
			Skin skin = xp.getSkin(slider, part);

			if (vertical) {
				int x = (trackRect.width - skin.getWidth()) / 2;
				skin.paintSkin(g, trackRect.x + x, trackRect.y,
						skin.getWidth(), trackRect.height, null);
			} else {
				int y = (trackRect.height - skin.getHeight()) / 2;
				skin.paintSkin(g, trackRect.x, trackRect.y + y,
						trackRect.width, skin.getHeight(), null);
			}
		} else {
			super.paintTrack(g);
		}
	}

	@Override
	protected void paintMinorTickForHorizSlider(Graphics g,
			Rectangle tickBounds, int x) {
		XPStyle xp = XPStyle.getXP();
		if (xp != null) {
			g.setColor(xp.getColor(slider, accessTMSchema("Part", "TKP_TICS"),
					null, accessTMSchema("Prop", "COLOR"), Color.BLACK));
		}
		super.paintMinorTickForHorizSlider(g, tickBounds, x);
	}

	@Override
	protected void paintMajorTickForHorizSlider(Graphics g,
			Rectangle tickBounds, int x) {
		XPStyle xp = XPStyle.getXP();
		if (xp != null) {
			g.setColor(xp.getColor(slider, accessTMSchema("Part", "TKP_TICS"),
					null, accessTMSchema("Prop", "COLOR"), Color.BLACK));
		}
		super.paintMajorTickForHorizSlider(g, tickBounds, x);
	}

	@Override
	protected void paintMinorTickForVertSlider(Graphics g,
			Rectangle tickBounds, int y) {
		XPStyle xp = XPStyle.getXP();
		if (xp != null) {
			g.setColor(xp.getColor(slider,
					accessTMSchema("Part", "TKP_TICSVERT"), null,
					accessTMSchema("Prop", "COLOR"), Color.black));
		}
		super.paintMinorTickForVertSlider(g, tickBounds, y);
	}

	@Override
	protected void paintMajorTickForVertSlider(Graphics g,
			Rectangle tickBounds, int y) {
		XPStyle xp = XPStyle.getXP();
		if (xp != null) {
			g.setColor(xp.getColor(slider,
					accessTMSchema("Part", "TKP_TICSVERT"), null,
					accessTMSchema("Prop", "COLOR"), Color.black));
		}
		super.paintMajorTickForVertSlider(g, tickBounds, y);
	}

	@Override
	protected void paintThumb(Graphics g, boolean isLower) {
		XPStyle xp = XPStyle.getXP();
		if (xp != null) {
			String stateName = "NORMAL";

			if (isLower == lowerThumbSelected) {
				if (slider.hasFocus()) {
					stateName = "FOCUSED";
				}
				if (isLower ? rolloverLower : rolloverUpper) {
					stateName = "HOT";
				}
				if (pressed) {
					stateName = "PRESSED";
				}
			} else {
				if (!pressed) {
					if (isLower ? rolloverLower : rolloverUpper) {
						stateName = "HOT";
					}
				}
			}
			if (!slider.isEnabled()) {
				stateName = "DISABLED";
			}
			Object part = getXPThumbPart();
			Rectangle rect = isLower ? lowerThumbRect : upperThumbRect;
			xp.getSkin(slider, part).paintSkin(g, rect.x, rect.y,
					accessTMSchema("State", stateName));
		} else {
			super.paintThumb(g, isLower);
		}
	}

	@Override
	protected Dimension getThumbSize() {
		XPStyle xp = XPStyle.getXP();
		if (xp != null) {
			Dimension size = new Dimension();
			Skin s = xp.getSkin(slider, getXPThumbPart());
			size.width = s.getWidth();
			size.height = s.getHeight();
			return size;
		} else {
			return super.getThumbSize();
		}
	}

	private Object getXPThumbPart() {
		boolean vertical = (slider.getOrientation() == JSlider.VERTICAL);
		boolean leftToRight = slider.getComponentOrientation().isLeftToRight();
		Boolean paintThumbArrowShape = (Boolean) slider
				.getClientProperty("Slider.paintThumbArrowShape");
		if ((!slider.getPaintTicks() && paintThumbArrowShape == null)
				|| paintThumbArrowShape == Boolean.FALSE) {
			return accessTMSchema("Part", vertical ? "TKP_THUMBVERT"
					: "TKP_THUMB");
		} else {
			return accessTMSchema("Part",
					vertical ? (leftToRight ? "TKP_THUMBRIGHT"
							: "TKP_THUMBLEFT") : "TKP_THUMBBOTTOM");
		}
	}

	private static Object accessTMSchema(String a, String b) {
		try {
			Class<?> aClass = Class
					.forName("com.sun.java.swing.plaf.windows.TMSchema$" + a);
			Field bField = aClass.getDeclaredField(b);
			bField.setAccessible(true);
			return bField.get(null);
		} catch (ClassNotFoundException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| NoSuchFieldException e) {
			throw new RuntimeException("Unexpected.", e);
		}
	}

	private static class XPStyle {

		private Object delegate;

		private XPStyle(Object delegate) {
			this.delegate = delegate;
		}

		static XPStyle getXP() {
			try {
				Method method = Class.forName(
						"com.sun.java.swing.plaf.windows.XPStyle")
						.getDeclaredMethod("getXP");
				method.setAccessible(true);
				Object object = method.invoke(null);
				return object == null ? null : new XPStyle(object);
			} catch (NoSuchMethodException | ClassNotFoundException
					| InvocationTargetException | IllegalAccessException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				// suppress
			}
			return null;
		}

		Skin getSkin(Component c, Object part) {
			try {
				Class<?> partClass = Class
						.forName("com.sun.java.swing.plaf.windows.TMSchema$Part");
				Method method = delegate.getClass().getDeclaredMethod(
						"getSkin", Component.class, partClass);
				method.setAccessible(true);
				return new Skin(method.invoke(delegate, c, part));
			} catch (ClassNotFoundException | NoSuchMethodException
					| SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Unexpected.", e);
			}
		}

		Color getColor(Component c, Object part, Object state, Object prop,
				Color fallback) {
			try {
				Class<?> partClass = Class
						.forName("com.sun.java.swing.plaf.windows.TMSchema$Part");
				Class<?> stateClass = Class
						.forName("com.sun.java.swing.plaf.windows.TMSchema$State");
				Class<?> propClass = Class
						.forName("com.sun.java.swing.plaf.windows.TMSchema$Prop");
				Method method = delegate.getClass().getDeclaredMethod(
						"getColor", Component.class, partClass, stateClass,
						propClass, Color.class);
				method.setAccessible(true);
				return (Color) method.invoke(delegate, c, part, state, prop,
						fallback);
			} catch (ClassNotFoundException | NoSuchMethodException
					| SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Unexpected.", e);
			}
		}
	}

	private static class Skin {
		private Object delegate;

		Skin(Object delegate) {
			this.delegate = delegate;
		}

		void paintSkin(Graphics g, int dx, int dy, Object state) {
			try {
				Class<?> stateClass = Class
						.forName("com.sun.java.swing.plaf.windows.TMSchema$State");
				Method method = delegate.getClass().getDeclaredMethod(
						"paintSkin", Graphics.class, Integer.TYPE,
						Integer.TYPE, stateClass);
				method.setAccessible(true);
				method.invoke(delegate, g, dx, dy, state);
			} catch (ClassNotFoundException | NoSuchMethodException
					| SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Unexpected.", e);
			}
		}

		void paintSkin(Graphics g, int dx, int dy, int dw, int dh, Object state) {
			try {
				Class<?> stateClass = Class
						.forName("com.sun.java.swing.plaf.windows.TMSchema$State");
				Method method = delegate.getClass().getDeclaredMethod(
						"paintSkin", Graphics.class, Integer.TYPE,
						Integer.TYPE, Integer.TYPE, Integer.TYPE, stateClass);
				method.setAccessible(true);
				method.invoke(delegate, g, dx, dy, dw, dh, state);
			} catch (ClassNotFoundException | NoSuchMethodException
					| SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Unexpected.", e);
			}
		}

		int getWidth() {
			try {
				Method method = delegate.getClass().getDeclaredMethod(
						"getWidth");
				method.setAccessible(true);
				return (int) method.invoke(delegate);
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("Unexpected.", e);
			}
		}

		int getHeight() {
			try {
				Method method = delegate.getClass().getDeclaredMethod(
						"getHeight");
				method.setAccessible(true);
				return (int) method.invoke(delegate);
			} catch (NoSuchMethodException | SecurityException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new RuntimeException("Unexpected.", e);
			}
		}
	}
}
