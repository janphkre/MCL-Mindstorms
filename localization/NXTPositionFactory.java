package localization;

import aima.core.robotics.impl.map.IPoseFactory;
import aima.core.util.Util;
import aima.core.util.math2d.Point2D;

public class NXTPositionFactory implements IPoseFactory<NXTPosition,NXTMove> {

	private static float[][] validHeadings = {{0f,360f}};
	
	@Override
	public NXTPosition getPose(Point2D point) {
		final int index = Util.randomNumberBetween(0,validHeadings.length-1);
		final double heading = Util.generateRandomDoubleBetween(validHeadings[index][0], validHeadings[index][1]);
		return new NXTPosition((float) point.getX(), (float) point.getY(), (float) heading);
	}

}
