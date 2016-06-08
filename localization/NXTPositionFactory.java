package localization;

import robotics.concrete.IPoseFactory;

public class NXTPositionFactory implements IPoseFactory<NXTPosition,NXTMove> {

	@Override
	public NXTPosition getPose(double x, double y, double heading) {
		return new NXTPosition((float) x, (float) y, (float) heading);
	}

}
