package training.official.classic_puzzle_medium;

import java.util.Scanner;

class MarsLanderEpisode2 {
	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		Land.instance = new Land(in.nextInt());
		Land.instance.addPoints(in);

		while (true) {
			Capsule.instance = new Capsule(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
			Capsule.instance.goToward(Land.instance.getDestination());
			System.out.println(Capsule.instance.rotationAngle + " " + (int)Capsule.instance.thrusterPower);
		}
	}
}

class Capsule {
	private static final double MARS_GRAVITY = 3.711;
	private static final double MAX_THRUSTER_POWER = 4.0;
	private static final double MIN_THRUSTER_POWER = 0.0;
	private static final double MAX_THRUSTER_POWER_PER_SECOND = 1.0;

	private static final double MAX_LANDING_Y_VELOCITY = -39.0;
	private static final double MAX_LANDING_X_VELOCITY = 19.0;
	private static final double MAX_CRUISING_X_VELOCITY = 40.0;

	private static final int MAX_ROTATION_ANGLE = 90;
	private static final int STABILIZED_ROTATION_ANGLE = 0;
	private static final int MIN_ROTATION_ANGLE = -90;
	private static final int MAX_ROTATION_ANGLE_PER_SECOND = 15;
	private static final int ROTATION_ANGLE_FOR_DRIFT = 22; // can't figure out how to actually calculate this value, found it by tinkering with it for a while

	static Capsule instance;

	private int fuel;
	int rotationAngle;
	double thrusterPower;
	Vector2d position;
	private Vector2d velocity;
	private Vector2d acceleration;

	Capsule(int x, int y, int xVelocity, int yVelocity, int fuel, int rotationAngle, int thrusterPower) {
		this.fuel = fuel;
		this.rotationAngle = rotationAngle;
		this.thrusterPower = (double)thrusterPower;
		this.position = new Vector2d(x, y);
		this.velocity = new Vector2d((double)xVelocity, (double)yVelocity);
		this.acceleration = calculateAcceleration();
	}

	private Capsule(Capsule capsule) {
		this.fuel = capsule.fuel;
		this.rotationAngle = capsule.rotationAngle;
		this.thrusterPower = capsule.thrusterPower;
		this.position = new Vector2d(capsule.position.x, capsule.position.y);
		this.velocity = new Vector2d(capsule.velocity.x, capsule.velocity.y);
		this.acceleration = new Vector2d(capsule.acceleration.x, capsule.acceleration.y);
	}

	void goToward(Vector2d destination) {
		if (destination.x == Capsule.instance.position.x) {
			land(destination.y);
		}
		else {
			cruise(destination.x - Capsule.instance.position.x);
		}
	}

	private void land(double targetY) {
		if (velocity.x > 0) {
			driftLeft();
		}
		else if (velocity.x < 0) {
			driftRight();
		}
		else {

			this.rotationAngle = nextRotationAngleToward(STABILIZED_ROTATION_ANGLE);
			if(needToBrake(position.y - targetY)) {
				thrusterPower = nextThrusterPowerToward(MAX_THRUSTER_POWER);
			}
			else {
				thrusterPower = nextThrusterPowerToward(MIN_THRUSTER_POWER);
			}
		}
	}

	private void cruise(double targetX) {
		if (targetX < 0) {
			if (velocity.x > -MAX_CRUISING_X_VELOCITY) {
				driftLeft();
			}
			else if (velocity.x < -MAX_CRUISING_X_VELOCITY) {
				driftRight();
			}
			else {
				stabilize();
			}
		}
		else if (targetX > 0) {
			if (velocity.x < MAX_CRUISING_X_VELOCITY) {
				driftRight();
			}
			else if (velocity.x > MAX_CRUISING_X_VELOCITY) {
				driftLeft();
			}
			else {
				stabilize();
			}
		}
	}

	private void driftLeft() {
		this.rotationAngle = nextRotationAngleToward(ROTATION_ANGLE_FOR_DRIFT);
		this.thrusterPower = nextThrusterPowerToward(MAX_THRUSTER_POWER);
	}

	private void driftRight() {
		this.rotationAngle = nextRotationAngleToward(-ROTATION_ANGLE_FOR_DRIFT);
		this.thrusterPower = nextThrusterPowerToward(MAX_THRUSTER_POWER);
	}

	private void stabilize() {
		this.rotationAngle = nextRotationAngleToward(STABILIZED_ROTATION_ANGLE);
		if (velocity.y < MAX_LANDING_Y_VELOCITY) {
			this.thrusterPower = nextThrusterPowerToward(MAX_THRUSTER_POWER);
		}
		else {
			this.thrusterPower = nextThrusterPowerToward(MAX_THRUSTER_POWER - MAX_THRUSTER_POWER_PER_SECOND);
		}
	}

	private boolean needToBrake(double distanceToGround) {
		Capsule simulationCapsule = new Capsule(this);
		simulationCapsule.velocity = calculateVelocityInOneSecond();
		distanceToGround += simulationCapsule.velocity.y;

		while (distanceToGround > 0 && simulationCapsule.velocity.y < MAX_LANDING_Y_VELOCITY) {
			simulationCapsule.thrusterPower = nextThrusterPowerToward(MAX_THRUSTER_POWER);
			simulationCapsule.velocity = calculateVelocityInOneSecond();
			distanceToGround += simulationCapsule.velocity.y;
		}

		return distanceToGround <= 0;
	}

	private int nextRotationAngleToward(int aimedRotationAngle) {
		if (rotationAngle >= MIN_ROTATION_ANGLE && rotationAngle <= MAX_ROTATION_ANGLE) {
			if (rotationAngle < aimedRotationAngle) {
				if ((aimedRotationAngle - rotationAngle) > MAX_ROTATION_ANGLE_PER_SECOND) {
					return rotationAngle + MAX_ROTATION_ANGLE_PER_SECOND;
				}
				else {
					return aimedRotationAngle;
				}
			}
			else if (rotationAngle > aimedRotationAngle) {
				if ((rotationAngle - aimedRotationAngle) > MAX_ROTATION_ANGLE_PER_SECOND) {
					return rotationAngle - MAX_ROTATION_ANGLE_PER_SECOND;
				}
				else {
					return aimedRotationAngle;
				}
			}
		}
		return this.rotationAngle;
	}

	private double nextThrusterPowerToward(double aimedThrusterPower) {
		if (this.thrusterPower > 0 && this.thrusterPower > aimedThrusterPower) {
			return this.thrusterPower - 1;
		}
		if (this.thrusterPower < MAX_THRUSTER_POWER && this.thrusterPower < aimedThrusterPower) {
			return this.thrusterPower + 1;
		}
		return this.thrusterPower;
	}

	private Vector2d calculateVelocityInOneSecond() {
		acceleration = calculateAcceleration();
		return new Vector2d(Math.round(velocity.x + acceleration.x), Math.round(velocity.y + acceleration.y));
	}

	private Vector2d calculateAcceleration() {
		double xRatio = (Math.abs(rotationAngle) / MAX_ROTATION_ANGLE);
		double yRatio = 1 - xRatio;
		return new Vector2d(thrusterPower * xRatio, (thrusterPower * yRatio) - MARS_GRAVITY);
	}
}

class Land {
	static Land instance;

	private Vector2d[] land;
	private int landPoints;
	private int landingGroundIndex = 0;

	Land(int landPoints) {
		this.landPoints = landPoints;
		land = new Vector2d[landPoints];
	}

	void addPoints(Scanner in) {
		for (int i = 0; i < landPoints; i++) {
			land[i] = new Vector2d(in.nextInt(), in.nextInt());
		}
		findLandingGroundIndex();
	}

	Vector2d getDestination() {
		return new Vector2d(findLandingGroundX(), findLandingGroundY());
	}

	private void findLandingGroundIndex() {
		for (int i = 0; i < landPoints - 1; i++) {
			if (land[i].y == land[i + 1].y) {
				landingGroundIndex = i;
			}
		}
	}

	private double findLandingGroundX() {
		double landingGroundStart = land[landingGroundIndex].x;
		double landingGroundEnd = land[landingGroundIndex + 1].x;
		if (landingGroundStart > Capsule.instance.position.x) { // landing ground is on the right
			return landingGroundStart;
		}
		else if ((landingGroundStart <= Capsule.instance.position.x) && (landingGroundEnd >= Capsule.instance.position.x)) { // landing ground is below the capsule
			return Capsule.instance.position.x;
		}
		else { //landing ground is on the left
			return landingGroundEnd;
		}
	}

	private double findLandingGroundY() {
		return land[landingGroundIndex].y;
	}
}

class Vector2d {
	double x;
	double y;

	Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}
}
