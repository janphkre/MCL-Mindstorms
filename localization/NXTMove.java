package localization;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import aima.core.robotics.datatypes.IMclMove;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;

public class NXTMove implements IMclMove<NXTMove> {
	
	private static float ROTATION_NOISE = 2.0f;//maximum percentage
	private static float MOVEMENT_NOISE = 2.0f;//maximum percentage
	private static Random rand = new Random();
	
	public static void setRotationNoise(double value) {
		ROTATION_NOISE = (float) value;
	}
	
	public static void setMovementNoise(double value) {
		MOVEMENT_NOISE = (float) value;
	}
	
	private LinkedList<Move> moveList = new LinkedList<Move>();
	
	public void add(Move move) {
		moveList.add(move);
	}
	
	public Iterator<Move> getMoves() {
		return moveList.iterator();
	}
	
	@Override
	public NXTMove generateNoise() {
		NXTMove result = new NXTMove();
		for(Move move:moveList) {
			Move moveNew;
			if(move.getMoveType() == MoveType.TRAVEL) {
				final float maxDistanceNoise = MOVEMENT_NOISE * (move.getDistanceTraveled());
				final float distance = move.getDistanceTraveled() + maxDistanceNoise * rand.nextFloat() - maxDistanceNoise / 2;
				moveNew = new Move(MoveType.TRAVEL,distance,0.0f,move.isMoving());
			} else if(move.getMoveType() == MoveType.ROTATE) {
				final float maxRotationNoise = ROTATION_NOISE * (move.getAngleTurned());
				final float angle = move.getAngleTurned() + maxRotationNoise * rand.nextFloat() - maxRotationNoise / 2;
				moveNew = new Move(MoveType.TRAVEL,0.0f,angle,move.isMoving());
			} else if(move.getMoveType() == MoveType.ARC) {
				final float maxDistanceNoise = MOVEMENT_NOISE * (move.getDistanceTraveled());
				final float maxRotationNoise = ROTATION_NOISE * (move.getAngleTurned());
				final float distance = move.getDistanceTraveled() + maxDistanceNoise * rand.nextFloat() - maxDistanceNoise / 2;
				final float angle = move.getAngleTurned() + maxRotationNoise * rand.nextFloat() - maxRotationNoise / 2;
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
			result += move.toString() + "\n";
		}
		return result;
	}
}
