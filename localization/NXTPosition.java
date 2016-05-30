package localization;

import java.util.Iterator;

import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;
import lejos.robotics.navigation.Pose;
import robotics.concrete.Position2D;
import robotics.generic.IMclMove;

public class NXTPosition implements Position2D<NXTPosition> {
	
	private Pose pose;

	public NXTPosition(float x, float y, float heading) {
		pose = new Pose(x, y, heading);
	}
	
	@Override
	public NXTPosition applyMovement(IMclMove move) {
		if(move == null) throw new RuntimeException("Received null as movement!");
		if(move instanceof NXTMove) return applyMovement((NXTMove) move);
		throw new RuntimeException("Movement is not of type MoveNXT!");
	}
	
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
	public NXTPosition addAngle(double angle) {
		return new NXTPosition(pose.getX(), pose.getY(), pose.getHeading() + (float) angle);
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
		return pose.getHeading();
	}

}
