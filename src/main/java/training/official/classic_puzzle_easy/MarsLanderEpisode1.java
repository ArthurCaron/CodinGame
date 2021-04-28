package training.official.classic_puzzle_easy;

import java.util.Scanner;

class MarsLanderEpisode1 {
	private static final double MARS_GRAVITY = 3.711;
	private static final double MAX_THRUSTER_POWER = 4.0;
	private static final double MAX_LANDING_VELOCITY = -39.0;

	private static int[] landX;
	private static int[] landY;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int surfaceN = in.nextInt(); // the number of points used to draw the surface of Mars.

		landX = new int[surfaceN];
		landY = new int[surfaceN];
		for (int i = 0; i < surfaceN; i++) {
			landX[i] = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
			landY[i] = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the
			// surface of Mars.
		}

		// game loop
		while (true) {
			int capsuleX = in.nextInt();
			int capsuleY = in.nextInt();
			int hVelocity = in.nextInt(); // the horizontal speed (in m/s), can be negative.
			int vVelocity = in.nextInt(); // the vertical speed (in m/s), can be negative.
			int fuel = in.nextInt(); // the quantity of remaining fuel in liters.
			int rotate = in.nextInt(); // the rotation angle in degrees (-90 to 90).
			int capsuleAcceleration = in.nextInt(); // the thrust power (0 to 4).

			double distanceToGround = capsuleY - findGroundYBeneathCapsule(capsuleX);
			if (needToBrake(distanceToGround, (double) capsuleAcceleration, (double) vVelocity)) {
				if (capsuleAcceleration < MAX_THRUSTER_POWER) {
					capsuleAcceleration++;
				}
				System.out.println(rotate + " " + capsuleAcceleration);
			} else {
				if (capsuleAcceleration > 0) {
					capsuleAcceleration--;
				}
				System.out.println(rotate + " " + capsuleAcceleration);
			}
		}
	}

	private static double calculateVelocityInOneSecond(double velocity, double acceleration) {
		return velocity + (acceleration - MARS_GRAVITY);
	}

	private static boolean needToBrake(double distanceToGround, double currentAcceleration, double currentVelocity) {
		currentVelocity = Math.floor(calculateVelocityInOneSecond(currentVelocity, currentAcceleration));
		distanceToGround += currentVelocity;

		while (distanceToGround > 0 && currentVelocity < MAX_LANDING_VELOCITY) {
			if (currentAcceleration < MAX_THRUSTER_POWER) {
				currentAcceleration++;
			}
			currentVelocity = calculateVelocityInOneSecond(currentVelocity, currentAcceleration);
			distanceToGround += currentVelocity;
		}

		return distanceToGround <= 0;
	}

	private static int findGroundYBeneathCapsule(int capsuleX) {
		for (int i = 0; i < landX.length; i++) {
			if (landX[i] > capsuleX) {
				return landY[i];
			}
		}
		return 0;
	}
}
