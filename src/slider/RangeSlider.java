package slider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * 
 * <p>
 * Note that RangeSlider makes use of the default BoundedRangeModel, which
 * supports an inner range defined by a value and an extent. The upper value
 * returned by RangeSlider is simply the lower value plus the extent.
 * </p>
 */
@SuppressWarnings("serial")
public class RangeSlider extends JSlider {

	/**
	 * @see #getUIClassID
	 * @see #readObject
	 */
	private static final String uiClassID = "RangeSliderUI";

	static {
		UIManager.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("lookAndFeel"))
					guessAndSetDefaultUI();
			}
		});
		if (UIManager.get(uiClassID) == null)
			guessAndSetDefaultUI();
	}

	/**
	 * Guess and set the default UI delegation of this RangeSlider from the
	 * current L&F.
	 */
	public static void guessAndSetDefaultUI() {
		String lafName = UIManager.getLookAndFeel().getID();
		if (lafName.equals("Metal"))
			UIManager.put(uiClassID, "slider.MetalRangeSliderUI");
		else if (lafName.equals("Windows"))
			UIManager.put(uiClassID, "slider.WindowsRangeSliderUI");
		else if (lafName.equals("Nimbus")) {
			((NimbusLookAndFeel) UIManager.getLookAndFeel()).register(
					SliderRangeTrackRegion.INSTANCE, "Slider:SliderRangeTrack");
			UIManager.put("Slider:SliderRangeTrack.States",
					"Enabled,MouseOver,Pressed,Disabled,Focused,Selected");
			UIManager.put(
					"Slider:SliderRangeTrack[Disabled].backgroundPainter",
					new SliderRangeTrackPainter(false));
			UIManager.put("Slider:SliderRangeTrack[Enabled].backgroundPainter",
					new SliderRangeTrackPainter(true));
			UIManager.put(uiClassID, "slider.SynthRangeSliderUI");
		} else
			UIManager.put(uiClassID, "slider.BasicRangeSliderUI");
	}

	/**
	 * Creates a range slider with the range 0 to 100 and an initial value of 33
	 * and 66.
	 */
	public RangeSlider() {
		this(HORIZONTAL, 0, 100, 33, 66);
	}

	/**
	 * Creates a range slider using the specified orientation with the range
	 * {@code 0} to {@code 100} and an initial value of {@code 33} and
	 * {@code 66}. The orientation can be either
	 * <code>SwingConstants.VERTICAL</code> or
	 * <code>SwingConstants.HORIZONTAL</code>.
	 * 
	 * @param orientation
	 *            the orientation of the slider
	 * @throws IllegalArgumentException
	 *             if orientation is not one of {@code VERTICAL},
	 *             {@code HORIZONTAL}
	 * @see #setOrientation
	 */
	public RangeSlider(int orientation) {
		this(orientation, 0, 100, 33, 66);
	}

	/**
	 * Creates a horizontal range slider using the specified min and max with an
	 * initial value equal to the {@code (min * 2 + max) / 3} and
	 * {@code (min + max * 2) / 3}.
	 * <p>
	 * The <code>BoundedRangeModel</code> that holds the slider's data handles
	 * any issues that may arise from improperly setting the minimum and maximum
	 * values on the slider. See the {@code BoundedRangeModel} documentation for
	 * details.
	 * 
	 * @param min
	 *            the minimum value of the slider
	 * @param max
	 *            the maximum value of the slider
	 * @see BoundedRangeModel
	 * @see #setMinimum
	 * @see #setMaximum
	 */
	public RangeSlider(int min, int max) {
		this(HORIZONTAL, min, max, (min * 2 + max) / 3, (min + max * 2) / 3);
	}

	/**
	 * Creates a horizontal range slider using the specified min, max, lower
	 * value and upper value.
	 * <p>
	 * The <code>BoundedRangeModel</code> that holds the slider's data handles
	 * any issues that may arise from improperly setting the minimum, initial,
	 * and maximum values on the slider. See the {@code BoundedRangeModel}
	 * documentation for details.
	 * 
	 * @param min
	 *            the minimum value of the slider
	 * @param max
	 *            the maximum value of the slider
	 * @param lowerValue
	 *            the initial lower value of the slider
	 * @param upperValue
	 *            the initial upper value of the slider
	 * @see BoundedRangeModel
	 * @see #setMinimum
	 * @see #setMaximum
	 * @see #setValue
	 * @see #setUpperValue
	 */
	public RangeSlider(int min, int max, int lowerValue, int upperValue) {
		this(HORIZONTAL, min, max, lowerValue, upperValue);
	}

	/**
	 * Creates a range slider with the specified orientation and the specified
	 * minimum, maximum, and initial values. The orientation can be either
	 * <code>SwingConstants.VERTICAL</code> or
	 * <code>SwingConstants.HORIZONTAL</code>.
	 * <p>
	 * The <code>BoundedRangeModel</code> that holds the slider's data handles
	 * any issues that may arise from improperly setting the minimum, initial,
	 * and maximum values on the slider. See the {@code BoundedRangeModel}
	 * documentation for details.
	 * 
	 * @param orientation
	 *            the orientation of the slider
	 * @param min
	 *            the minimum value of the slider
	 * @param max
	 *            the maximum value of the slider
	 * @param lowerValue
	 *            the initial lower value of the slider
	 * @param upperValue
	 *            the initial upper value of the slider
	 * @throws IllegalArgumentException
	 *             if orientation is not one of {@code VERTICAL},
	 *             {@code HORIZONTAL}
	 * @see BoundedRangeModel
	 * @see #setOrientation
	 * @see #setMinimum
	 * @see #setMaximum
	 * @see #setValue
	 * @see #setUpperValue
	 */
	public RangeSlider(int orientation, int min, int max, int lowerValue,
			int upperValue) {
		super(orientation, min, max, lowerValue);
		setUpperValue(upperValue);
	}

	/**
	 * Creates a horizontal slider using the specified BoundedRangeModel.
	 */
	public RangeSlider(BoundedRangeModel brm) {
		super(brm);
	}

	/**
	 * Returns the name of the L&F class that renders this component.
	 * 
	 * @return "RangeSliderUI"
	 * @see JComponent#getUIClassID
	 * @see UIDefaults#getUI
	 */
	@Override
	public String getUIClassID() {
		return uiClassID;
	}

	/**
	 * Sets the UI object which implements the L&F for this component.
	 * 
	 * @param ui
	 *            the SliderUI L&F object
	 * @throws IllegalArgumentException
	 *             if {@code ui} is not a RangeSliderUI
	 * @see UIDefaults#getUI
	 */
	@Override
	public void setUI(SliderUI ui) {
		if (!(ui instanceof RangeSliderUI))
			throw new IllegalArgumentException("ui is not a RangeSliderUI");
		super.setUI(ui);
	}

	/**
	 * Sets the UI object which implements the L&F for this component.
	 * 
	 * @param ui
	 *            the SliderUI L&F object
	 * @see UIDefaults#getUI
	 */
	public void setUI(RangeSliderUI ui) {
		super.setUI(ui);
	}

	@Override
	public RangeSliderUI getUI() {
		return (RangeSliderUI) super.getUI();
	}

	/**
	 * Returns the slider's current lower value from the
	 * {@code BoundedRangeModel}.
	 * 
	 * @return the current lower value of the slider
	 * @see #setValue
	 * @see #getLowerValue
	 * @see #getUpperValue
	 * @see BoundedRangeModel#getValue
	 */
	@Override
	public int getValue() {
		return super.getValue();
	}

	/**
	 * Sets the slider's current lower value to {@code value}, <i>without
	 * changing the extent</i>. This method forwards the new value to the model.
	 * <p>
	 * The data model (an instance of {@code BoundedRangeModel}) handles any
	 * mathematical issues arising from assigning faulty values. See the
	 * {@code BoundedRangeModel} documentation for details.
	 * <p>
	 * If the new value is different from the previous value, all change
	 * listeners are notified.
	 * 
	 * @param value
	 *            the new lower value
	 * @see #getValue
	 * @see #setLowerValue
	 * @see #setUpperValue
	 * @see #addChangeListener
	 * @see BoundedRangeModel#setValue
	 */
	@Override
	public void setValue(int value) {
		super.setValue(value);
	}

	/**
	 * Returns the slider's current lower value from the
	 * {@code BoundedRangeModel}. Equivalent to {@code getValue}.
	 * 
	 * @return the current lower value of the slider
	 * @see #setLowerValue
	 * @see #getValue
	 * @see #getUpperValue
	 */
	public int getLowerValue() {
		return getValue();
	}

	/**
	 * Sets the slider's current lower value to {@code value}, <i>without
	 * changing the upper value</i>.
	 * <p>
	 * If the new value is different from the previous value, all change
	 * listeners are notified.
	 * 
	 * @param value
	 *            the new lower value
	 * @see #getLowerValue
	 * @see #setValue
	 * @see #setUpperValue
	 * @see #addChangeListener
	 */
	public void setLowerValue(int lowerValue) {
		int oldValue = getValue();
		if (oldValue == lowerValue) {
			return;
		}

		// Compute new value and extent to maintain upper value.
		int oldExtent = getExtent();
		int newValue = Math.min(Math.max(getMinimum(), lowerValue), oldValue
				+ oldExtent);
		int newExtent = oldExtent + oldValue - newValue;

		// Set new value and extent, and fire a single change event.
		getModel().setRangeProperties(newValue, newExtent, getMinimum(),
				getMaximum(), getValueIsAdjusting());
	}

	/**
	 * Returns the slider's current upper value from the
	 * {@code BoundedRangeModel}.
	 * 
	 * @return the current upper value of the slider
	 * @see #setUpperValue
	 * @see #getValue
	 * @see #getLowerValue
	 */
	public int getUpperValue() {
		return getValue() + getExtent();
	}

	/**
	 * Sets the slider's current upper value to {@code value}, <i>without
	 * changing the lower value if possible</i>.
	 * <p>
	 * If the new value is different from the previous value, all change
	 * listeners are notified.
	 * 
	 * @param value
	 *            the new upper value
	 * @see #getUpperValue
	 * @see #setValue
	 * @see #setLowerValue
	 * @see #addChangeListener
	 * @beaninfo preferred
	 */
	public void setUpperValue(int upperValue) {
		// Compute new extent.
		int lowerValue = getValue();
		int newExtent = Math.min(Math.max(0, upperValue - lowerValue),
				getMaximum() - lowerValue);

		// Set extent to set upper value.
		setExtent(newExtent);
	}
}
