package localization;

import java.util.Iterator;
import java.util.LinkedList;

import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;
import aima.core.robotics.datatypes.IMclMove;
import aima.core.util.Util;
import aima.gui.applications.robotics.util.GuiBase;

/**
 * This class implements {@link IMclMove} for the NXT environment. Basically it is a list of {@link Move} that were performed by the NXT.
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 * 
 */
public final class NXTMove implements IMclMove<NXTMove> {
	
	private static float ROTATION_NOISE;//maximum percentage
	private static float MOVEMENT_NOISE;//maximum percentage
	
	private LinkedList<Move> moveList = new LinkedList<Move>();
	
	/**
	 * Sets the move noise model for the rotation.
	 * @param value the radiant value of the noise.
	 */
	public static void setRotationNoise(double value) {
		ROTATION_NOISE = (float) value;
	}
	
	/**
	 * Sets the move noise model for the distance.
	 * @param value the absolute value of the noise.
	 */
	public static void setMovementNoise(double value) {
		MOVEMENT_NOISE = (float) value;
	}
	
	/**
	 * Adds a {@link Move} at the end of the list of moves.
	 * @param move the move to be added.
	 */
	public void add(Move move) {
		moveList.add(move);
	}
	
	/**
	 * Returns an {@link Iterator} over the list of stored moves.
	 * @return an iterator.
	 */
	public Iterator<Move> getMoves() {
		return moveList.iterator();
	}
	
	@Override
	public NXTMove generateNoise() {
		NXTMove result = new NXTMove();
		for(Move move: moveList) {
			Move moveNew;
			if(move.getMoveType() == MoveType.TRAVEL) {
				final float maxDistanceNoise = MOVEMENT_NOISE * (move.getDistanceTraveled());
				final float distance = Util.generateRandomFloatBetween(move.getDistanceTraveled() - maxDistanceNoise, move.getDistanceTraveled() - maxDistanceNoise);
				moveNew = new Move(MoveType.TRAVEL,distance,0.0f,move.isMoving());
			} else if(move.getMoveType() == MoveType.ROTATE) {
				final float maxRotationNoise = ROTATION_NOISE * (move.getAngleTurned());
				final float angle = Util.generateRandomFloatBetween(move.getAngleTurned() - maxRotationNoise, move.getDistanceTraveled() - maxRotationNoise);
				moveNew = new Move(MoveType.TRAVEL,0.0f,angle,move.isMoving());
			} else if(move.getMoveType() == MoveType.ARC) {
				final float maxDistanceNoise = MOVEMENT_NOISE * (move.getDistanceTraveled());
				final float maxRotationNoise = ROTATION_NOISE * (move.getAngleTurned());
				final float distance = Util.generateRandomFloatBetween(move.getDistanceTraveled() - maxDistanceNoise, move.getDistanceTraveled() - maxDistanceNoise);
				final float angle = Util.generateRandomFloatBetween(move.getAngleTurned() - maxRotationNoise, move.getDistanceTraveled() - maxRotationNoise);
				moveNew = new Move(MoveType.TRAVEL,distance,angle,move.isMoving());
			} else {
				moveNew = new Move(MoveType.STOP,0.0f,0.0f,false);
			}
			result.add(moveNew);
		}
		return result;
	}
	
	@Override
	public String toString() {
		String result = new String();
		for(Move move: moveList) {
			result += move.getMoveType().toString() + " ";
			if(move.getMoveType() == MoveType.TRAVEL) {
				result += GuiBase.getFormat().format(move.getDistanceTraveled());
			} else if(move.getMoveType() == MoveType.ROTATE) {
				result += GuiBase.getFormat().format(move.getAngleTurned());
			} else if(move.getMoveType() == MoveType.ARC) {
				result += GuiBase.getFormat().format(move.getArcRadius());
			}
			result += "\n";
		}
		return result;
	}
}
