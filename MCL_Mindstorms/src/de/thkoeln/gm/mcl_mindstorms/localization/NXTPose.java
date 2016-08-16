package de.thkoeln.gm.mcl_mindstorms.localization;

import java.util.Iterator;

import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;
import lejos.robotics.navigation.Pose;
import aima.core.robotics.impl.datatypes.Angle;
import aima.core.robotics.impl.datatypes.IPose2D;

/**
 * Implementation of {@link IPose2D} for {@link NXTMove}.<br/>
 * This class is used in the context of the NXT environment. 
 * 
 * @author Arno von Borries
 * @author Jan Phillip Kretzschmar
 * @author Andreas Walscheid
 * 
 */
public final class NXTPose implements IPose2D<NXTPose,NXTMove> {
	
	private Pose pose;

	/**
	 * @param x the X coordinate of the pose.
	 * @param y the Y coordinate of the pose.
	 * @param heading the heading of the pose in radians.
	 */
	public NXTPose(float x, float y, float heading) {
		pose = new Pose(x, y, (float) Math.toDegrees(heading));
		normalizeHeading();
	}
	
	/**
	 * @param x the X coordinate of the pose.
	 * @param y the Y coordinate of the pose.
	 * @param heading the heading of the pose in radians or degrees.
	 * @param isDegrees true if the heading is in degrees.
	 */
	public NXTPose(float x, float y, float heading, boolean isDegrees) {
		if(isDegrees) {
			pose = new Pose(x, y, heading);
		} else {
			pose = new Pose(x, y, (float) Math.toDegrees(heading));
		}
		normalizeHeading();
	}
	
	/**
	 * Normalizes the heading to a value between +180 degree and -180 degree.
	 */
	private void normalizeHeading() {
		float heading = pose.getHeading() % 360;
		if(heading > 180) heading -= 360;
		if(heading < -180) heading += 360;
		pose.setHeading(heading);
	}
	
	/**
	 * Returns the heading in degrees.
	 * @return the heading in degrees.
	 */
	public float getDegreeHeading() {
		return pose.getHeading();
	}
	
	@Override
	public NXTPose applyMovement(NXTMove moves) {
		NXTPose result = clone();
		Iterator<Move> iterator = moves.getMoves();
		while(iterator.hasNext()) {
			Move move = iterator.next();
			if(move.getMoveType() == MoveType.TRAVEL) result.pose.moveUpdate(move.getDistanceTraveled());
			else if(move.getMoveType() == MoveType.ROTATE) result.pose.rotateUpdate(move.getAngleTurned());
			else if(move.getMoveType() == MoveType.ARC) result.pose.arcUpdate(move.getDistanceTraveled(), move.getAngleTurned());
		}
		result.normalizeHeading();
		return result;
	}

	@Override
	public NXTPose addAngle(Angle angle) {
		return new NXTPose(pose.getX(), pose.getY(), pose.getHeading()  + (float) angle.getDegreeValue(),true);
	}
	
	@Override
	public NXTPose clone() {
		return new NXTPose(pose.getX(), pose.getY(), pose.getHeading(),true);
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
	public double distanceTo(NXTPose position) {
		final double vectorX = pose.getX() - position.getX();
		final double vectorY = pose.getY() - position.getY();
		return Math.sqrt(vectorX*vectorX + vectorY*vectorY);
	}
}
