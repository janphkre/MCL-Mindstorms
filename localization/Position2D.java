package localization;

import java.util.Iterator;

import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;
import lejos.robotics.navigation.Pose;
import robotics.generic.IMclPosition;

public class Position2D implements IMclPosition<Move2D,Position2D> {
	
	private Pose pose;
	
	@Override
	public Position2D applyMovement(Move2D moves) {
		Position2D result = clone();
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
	public Position2D addAngle(double angle) {
		Position2D result = new Position2D();
		result.pose = new Pose(pose.getX(), pose.getY(), pose.getHeading() + (float) angle);
		return result;
	}
	
	@Override
	public Position2D clone() {
		Position2D result = new Position2D();
		result.pose = new Pose(pose.getX(), pose.getY(), pose.getHeading());
		return result;
	}

	

}
