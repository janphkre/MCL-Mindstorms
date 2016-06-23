package localization;

import aima.core.robotics.impl.datatypes.RangeReading;
import aima.core.robotics.impl.map.IRangeReadingFactory;

public class NXTRangeReadingFactory implements IRangeReadingFactory<RangeReading> {

	@Override
	public RangeReading getRangeReading(double value) {
		return new NXTRangeReading(value);
	}

}
