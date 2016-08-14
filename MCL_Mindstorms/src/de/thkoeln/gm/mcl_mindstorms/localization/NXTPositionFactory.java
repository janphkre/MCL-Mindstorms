package de.thkoeln.gm.mcl_mindstorms.localization;

import aima.core.robotics.impl.map.IPoseFactory;
import aima.core.util.Util;
import aima.core.util.datastructure.Pair;
import aima.core.util.math.geom.shapes.Point2D;

/**
 * Implements {@link IPoseFactory} for the {@link NXTPose}.<br/>
 * The heading is created based on the valid heading ranges.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 *
 */
public final class NXTPositionFactory implements IPoseFactory<NXTPose,NXTMove> {
	/**
	 * Array of ranges that will be considered as valid for the heading.
	 * It is used to create new particles on the map and check the validity of existing poses.
	 */
	private static AnglePair[] validHeadings = {new AnglePair(0.0f,360.0f)};
	
	@Override
	public NXTPose getPose(Point2D point) {
		final int index = Util.randomNumberBetween(0,validHeadings.length-1);
		final double heading = Util.generateRandomDoubleBetween(validHeadings[index].getFirst(), validHeadings[index].getSecond());
		return new NXTPose((float) point.getX(), (float) point.getY(), (float) heading);
	}

	@Override
	public NXTPose getPose(Point2D point, double heading) {
		return new NXTPose((float) point.getX(), (float) point.getY(), (float) heading);
	}
	
	@Override
	public boolean isHeadingValid(NXTPose pose) {
		final double heading = pose.getHeading();
		for(AnglePair headingRange: validHeadings) {
			if(heading >= headingRange.getFirst() && heading <= headingRange.getSecond()) return true;
		}
		return false;
	}
	
	/**
	 * A pair for two floats.
	 */
	private static class AnglePair extends Pair<Float,Float> {
		public AnglePair(Float a, Float b) {
			super(a, b);
		}
	}
}
