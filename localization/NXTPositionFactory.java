package localization;

import robotics.concrete.IPositionFactory;

public class NXTPositionFactory implements IPositionFactory<NXTPosition> {

	@Override
	public NXTPosition getPosition(double x, double y, double heading) {
		return new NXTPosition((float) x, (float) y, (float) heading);
	}

}
