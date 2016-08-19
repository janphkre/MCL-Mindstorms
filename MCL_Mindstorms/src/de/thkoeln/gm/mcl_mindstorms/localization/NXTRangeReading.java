package de.thkoeln.gm.mcl_mindstorms.localization;

import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.AbstractRangeReading;
import aima.core.util.Util;

/**
 * A range reading that extends the abstract class {@link AbstractRangeReading} by adding the range noise model for the NXT environment.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public final class NXTRangeReading extends AbstractRangeReading {
	/**
	 * The distance that the ultrasonic sensor of the NXT can reliable measure in cm. Any range reading above this value should be treated as infinity. 
	 */
	public static final double MAX_RELIABLE_RANGE_READING = 180.0d;
	/**
	 * The distance that the ultrasonic sensor will return as a maximum in cm.
	 */
	public static final double MAX_RANGE_READING = 255.0d;
	
	private static double RANGE_NOISE = 2.0d;
	private static double BAD_DELTA;
	/**
	 * Sets the noise model for all range readings.
	 * @param value the noise of the model in cm.
	 */
	public static void setRangeNoise(double value) {
		RANGE_NOISE = value;
	}
	
	/**
	 * Sets the bad delta for the calculation of the weight.
	 * @param delta the delta to be set.
	 */
	public static void setBadDelta(double delta) {
		BAD_DELTA = delta;
	}
	
	/**
	 * Constructor for a range reading that has a zero angle.
	 * @param value the range reading.
	 */
	public NXTRangeReading(double value) {
		super(value);
	}

	/**
	 * Constructor for a range reading at a given angle.
	 * @param value the range reading.
	 * @param angle the angle of the range reading.
	 */
	public NXTRangeReading(double value,Angle angle) {
		super(value, angle);
	}

	@Override
	public double calculateWeight(AbstractRangeReading secondRange) {
		final double adaptedRangeReading = Util.generateRandomDoubleBetween(getValue() - RANGE_NOISE, getValue() + RANGE_NOISE);
		AbstractRangeReading firstRange = new NXTRangeReading(adaptedRangeReading,getAngle());
		if((firstRange.getValue() < 0 || Double.isInfinite(firstRange.getValue())) && (secondRange.getValue() < 0  || Double.isInfinite(secondRange.getValue()))) return 1;
		final double robotValue;
		if(Double.isInfinite(firstRange.getValue()) || firstRange.getValue() > MAX_RELIABLE_RANGE_READING) robotValue = MAX_RELIABLE_RANGE_READING;
		else robotValue = firstRange.getValue();
		final double secondRangeValue;
		if(Double.isInfinite(secondRange.getValue()) || secondRange.getValue() > MAX_RELIABLE_RANGE_READING) secondRangeValue = MAX_RELIABLE_RANGE_READING;
		else secondRangeValue = secondRange.getValue();
		final double delta = Math.abs(robotValue - secondRangeValue);
		if(delta > BAD_DELTA) return 0.0f;
		return (float) (1.0d - delta / BAD_DELTA);
	}
}
