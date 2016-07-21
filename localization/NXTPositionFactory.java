package localization;

import aima.core.robotics.impl.map.IPoseFactory;
import aima.core.util.Util;
import aima.core.util.datastructure.Pair;
import aima.core.util.math.geom.shapes.Point2D;

public final class NXTPositionFactory implements IPoseFactory<NXTPosition,NXTMove> {

	private static class AnglePair extends Pair<Double,Double>{

		public AnglePair(Double a, Double b) {
			super(a, b);
		}}
	
	private static AnglePair[] validHeadings = {new AnglePair(0d,2*Math.PI)};
	
	@Override
	public NXTPosition getPose(Point2D point) {
		final int index = Util.randomNumberBetween(0,validHeadings.length-1);
		final double heading = Util.generateRandomDoubleBetween(validHeadings[index].getFirst(), validHeadings[index].getSecond());
		return new NXTPosition((float) point.getX(), (float) point.getY(), (float) heading);
	}

	@Override
	public NXTPosition getPose(Point2D point, double heading) {
		return new NXTPosition((float) point.getX(), (float) point.getY(), (float) heading);
	}
	
	@Override
	public boolean isHeadingValid(NXTPosition pose) {
		final double heading = pose.getHeading();
		for(AnglePair headingRange: validHeadings) {
			if(heading >= headingRange.getFirst() && heading <= headingRange.getSecond()) return true;
		}
		return false;
	}
}
