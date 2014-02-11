package slider;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

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
		UIManager.put(uiClassID, "slider.BasicRangeSliderUI");
	}

	/**
	 * Constructs a RangeSlider with default minimum and maximum values of 0 and
	 * 100.
	 */
	public RangeSlider() {
		initSlider();
	}

	/**
	 * Constructs a RangeSlider with the specified default minimum and maximum
	 * values.
	 */
	public RangeSlider(int min, int max) {
		super(min, max);
		initSlider();
	}

	/**
	 * Initializes the slider by setting default properties.
	 */
	private void initSlider() {
		setOrientation(HORIZONTAL);
	}

	/**
	 * Returns the name of the L&F class that renders this component.
	 * 
	 * @return "RangeSliderUI"
	 * @see JComponent#getUIClassID
	 * @see UIDefaults#getUI
	 */
	public String getUIClassID() {
		return uiClassID;
	}

	/**
	 * Returns the lower value in the range.
	 */
	@Override
	public int getValue() {
		return super.getValue();
	}

	/**
	 * Sets the lower value in the range.
	 */
	@Override
	public void setValue(int value) {
		int oldValue = getValue();
		if (oldValue == value) {
			return;
		}

		// Compute new value and extent to maintain upper value.
		int oldExtent = getExtent();
		int newValue = Math.min(Math.max(getMinimum(), value), oldValue
				+ oldExtent);
		int newExtent = oldExtent + oldValue - newValue;

		// Set new value and extent, and fire a single change event.
		getModel().setRangeProperties(newValue, newExtent, getMinimum(),
				getMaximum(), getValueIsAdjusting());
	}

	/**
	 * Returns the upper value in the range.
	 */
	public int getUpperValue() {
		return getValue() + getExtent();
	}

	/**
	 * Sets the upper value in the range.
	 */
	public void setUpperValue(int value) {
		// Compute new extent.
		int lowerValue = getValue();
		int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum()
				- lowerValue);

		// Set extent to set upper value.
		setExtent(newExtent);
	}
}
