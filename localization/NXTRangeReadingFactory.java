package localization;

import aima.core.robotics.impl.datatypes.AbstractRangeReading;
import aima.core.robotics.impl.map.IRangeReadingFactory;

public final class NXTRangeReadingFactory implements IRangeReadingFactory<AbstractRangeReading> {

	@Override
	public AbstractRangeReading getRangeReading(double value) {
		return new NXTRangeReading(value);
	}

}
