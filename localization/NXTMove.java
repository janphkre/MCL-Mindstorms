package localization;

import java.util.LinkedList;
import java.util.Random;

import aima.core.robotics.datatypes.IMclMove;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;

public class NXTMove implements IMclMove<NXTMove> {
	
	private static final float rotationNoise = 2.0f;//maximum percentage
	private static final float moveNoise = 2.0f;//maximum percentage
	private static Random rand = new Random();
	
	private LinkedList<Move> moveList = new LinkedList<Move>();
	
	public void add(Move move) {
		moveList.add(move);
	}
	
	@Override
	public NXTMove generateNoise() {
		NXTMove result = new NXTMove();
		for(Move move:moveList) {
			Move moveNew;
			if(move.getMoveType() == MoveType.TRAVEL) {
				final float maxDistanceNoise = moveNoise * (move.getDistanceTraveled());
				final float distance = move.getDistanceTraveled() + maxDistanceNoise * rand.nextFloat() - maxDistanceNoise / 2;
				moveNew = new Move(MoveType.TRAVEL,distance,0.0f,move.isMoving());
			} else if(move.getMoveType() == MoveType.ROTATE) {
				final float maxRotationNoise = rotationNoise * (move.getAngleTurned());
				final float angle = move.getAngleTurned() + maxRotationNoise * rand.nextFloat() - maxRotationNoise / 2;
				moveNew = new Move(MoveType.TRAVEL,0.0f,angle,move.isMoving());
			} else if(move.getMoveType() == MoveType.ARC) {
				final float maxDistanceNoise = moveNoise * (move.getDistanceTraveled());
				final float maxRotationNoise = rotationNoise * (move.getAngleTurned());
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
	
	public Iterator<Move> getMoves() {
		return moveList.iterator();
	@Override
	public String toString() {
		String result = new String();
		for(Move move: moveList) {
			result += move.toString() +"\n";
		}
		return result;
	}
}
