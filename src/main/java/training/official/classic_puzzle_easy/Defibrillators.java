package training.official.classic_puzzle_easy;

import java.util.Scanner;

class Defibrillators {
	private static final int NAME = 1;
	private static final int LONGITUDE = 4;
	private static final int LATITUDE = 5;
	private static final int EARTH_RADIUS = 6371;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		Double userLongitude = convertPositionToDouble(in.next());
		Double userLatitude = convertPositionToDouble(in.next());
		int N = in.nextInt();
		if (in.hasNextLine()) {
			in.nextLine();
		}

		String answer = "";
		Double currentSmallestDistance = Double.MAX_VALUE;

		for (int i = 0; i < N; i++) {
			String[] defib = in.nextLine().split(";");

			Double defibLongitude = convertPositionToDouble(defib[LONGITUDE]);
			Double defibLatitude = convertPositionToDouble(defib[LATITUDE]);

			Double distanceToUser = calculateGpsCoordinatesDistance(userLongitude, userLatitude, defibLongitude, defibLatitude);

			if (distanceToUser < currentSmallestDistance) {
				currentSmallestDistance = distanceToUser;
				answer = defib[NAME];
			}
		}

		System.out.println(answer);
	}

	private static double convertPositionToDouble(String position) {
		return Double.parseDouble(position.replace(',', '.'));
	}

	private static double calculateGpsCoordinatesDistance(Double longitudeA, Double latitudeA, Double longitudeB, Double latitudeB) {
		Double x = (longitudeB - longitudeA) * Math.cos((latitudeA + latitudeB) / 2);
		Double y = latitudeB - latitudeA;
		return Math.sqrt(x * x + y * y) * EARTH_RADIUS;
	}
}
