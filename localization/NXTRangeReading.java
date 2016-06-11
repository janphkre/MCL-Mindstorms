package localization;

import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.RangeReading;
import aima.core.util.Util;

public final class NXTRangeReading extends RangeReading {

	private static final double RANGE_SENSOR_NOISE = 2.0d;//cm
	
	public NXTRangeReading(double value) {
		super(value);
	}

	public NXTRangeReading(double value,Angle angle) {
		super(value, angle);
	}
	
	@Override
	public RangeReading addRangeNoise() {
		final double adaptedRangeReading = Util.generateRandomDoubleBetween(getValue() - RANGE_SENSOR_NOISE, getValue() + RANGE_SENSOR_NOISE);
		return new NXTRangeReading(adaptedRangeReading,getAngle());
	}

}
