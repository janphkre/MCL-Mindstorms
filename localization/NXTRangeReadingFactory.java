package localization;

import aima.core.robotics.impl.datatypes.AbstractRangeReading;
import aima.core.robotics.impl.map.IRangeReadingFactory;

/**
 * Implements {@link IRangeReadingFactory} for the {@link NXTRangeReading}.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 * 
 */
public final class NXTRangeReadingFactory implements IRangeReadingFactory<AbstractRangeReading> {

	@Override
	public AbstractRangeReading getRangeReading(double value) {
		return new NXTRangeReading(value);
	}

}
