package localization;

import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import aima.core.util.Util;

public final class NXTRangeReading extends RangeReading {

	private static double RANGE_NOISE = 2.0d;//cm
	
	public static void setRangeNoise(double value) {
		RANGE_NOISE = value;
	}
	
	public NXTRangeReading(double value) {
		super(value);
	}

	public NXTRangeReading(double value,Angle angle) {
		super(value, angle);
	}
	
	@Override
	public RangeReading addRangeNoise() {
		final double adaptedRangeReading = Util.generateRandomDoubleBetween(getValue() - RANGE_NOISE, getValue() + RANGE_NOISE);
		return new NXTRangeReading(adaptedRangeReading,getAngle());
	}

}
