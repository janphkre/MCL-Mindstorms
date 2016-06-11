package localization;

import aima.core.robotics.impl.map.IRangeReadingFactory;

public class NXTRangeReadingFactory implements IRangeReadingFactory<NXTRangeReading> {

	@Override
	public NXTRangeReading getRangeReading(double value) {
		return new NXTRangeReading(value);
	}

}
