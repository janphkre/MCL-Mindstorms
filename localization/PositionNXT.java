package localization;

import java.util.Iterator;

import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;
import lejos.robotics.navigation.Pose;
import robotics.generic.IMclMove;
import robotics.generic.IMclPosition;

public class PositionNXT implements IMclPosition {
	
	private Pose pose;
	
	@Override
	public PositionNXT applyMovement(IMclMove moves) {
		if(moves == null) throw new RuntimeException("Received null as Movement!");
		if(moves instanceof MoveNXT) return applyMovement((MoveNXT)moves);
		throw new RuntimeException("Movement is not of type MoveNXT!");
	}
	
	public PositionNXT applyMovement(MoveNXT moves) {
		PositionNXT result = clone();
		Iterator<Move> iterator = moves.getMoves();
		while(iterator.hasNext()) {
			Move move = iterator.next();
			if(move.getMoveType() == MoveType.TRAVEL) result.pose.moveUpdate(move.getDistanceTraveled());
			else if(move.getMoveType() == MoveType.ROTATE) result.pose.rotateUpdate(move.getAngleTurned());
			else if(move.getMoveType() == MoveType.ARC) result.pose.arcUpdate(move.getDistanceTraveled(),move.getAngleTurned());
		}
		return result;
	}

	@Override
	public PositionNXT addAngle(double angle) {
		PositionNXT result = new PositionNXT();
		result.pose = new Pose(pose.getX(), pose.getY(), pose.getHeading() + (float) angle);
		return result;
	}
	
	@Override
	public PositionNXT clone() {
		PositionNXT result = new PositionNXT();
		result.pose = new Pose(pose.getX(), pose.getY(), pose.getHeading());
		return result;
	}

	

}
