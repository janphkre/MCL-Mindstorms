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

	private static double RANGE_NOISE = 2.0d;
	
	/**
	 * Sets the noise model for all range readings.
	 * @param value the noise of the model in cm.
	 */
	public static void setRangeNoise(double value) {
		RANGE_NOISE = value;
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
	public AbstractRangeReading addRangeNoise() {
		final double adaptedRangeReading = Util.generateRandomDoubleBetween(getValue() - RANGE_NOISE, getValue() + RANGE_NOISE);
		return new NXTRangeReading(adaptedRangeReading,getAngle());
	}
}
