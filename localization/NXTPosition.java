package localization;

import java.util.Iterator;

import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.Pose2D;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;
import lejos.robotics.navigation.Pose;

public class NXTPosition implements Pose2D<NXTPosition,NXTMove> {
	
	private Pose pose;

	public NXTPosition(float x, float y, float heading) {
		pose = new Pose(x, y, heading);
	}
	
	@Override
	public NXTPosition applyMovement(NXTMove moves) {
		NXTPosition result = clone();
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
	public NXTPosition addAngle(Angle angle) {
		return new NXTPosition(pose.getX(), pose.getY(), pose.getHeading() + (float) angle.getValue());
	}
	
	@Override
	public NXTPosition clone() {
		return new NXTPosition(pose.getX(), pose.getY(), pose.getHeading());
	}

	@Override
	public double getX() {
		return pose.getX();
	}

	@Override
	public double getY() {
		return pose.getY();
	}

	@Override
	public double getHeading() {
		return Math.toRadians(pose.getHeading());
	}

	@Override
	public double distanceTo(NXTPosition position) {
		final double vectorX = pose.getX() - position.getX();
		final double vectorY = pose.getY() - position.getY();
		return Math.sqrt(vectorX*vectorX + vectorY*vectorY);
	}

}
