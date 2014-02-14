package slider;

import javax.swing.plaf.synth.Region;

class SliderRangeTrackRegion extends Region {

	public static final SliderRangeTrackRegion INSTANCE = new SliderRangeTrackRegion();

	private SliderRangeTrackRegion() {
		super("SliderRangeTrack", null, true);
	}
}
