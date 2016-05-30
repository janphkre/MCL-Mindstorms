package localization;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;
import robotics.generic.IMclMove;

public class MoveNXT implements IMclMove {
	
	private static final float rotationNoise = 2.0f;//maximum percentage
	private static final float moveNoise = 2.0f;//maximum percentage
	private static Random rand = new Random();
	
	private LinkedList<Move> moveList = new LinkedList<Move>();
	
	public void add(Move move) {
		moveList.add(move);
	}
	
	@Override
	public MoveNXT generateNoise() {
		MoveNXT result = new MoveNXT();
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
	}
}
