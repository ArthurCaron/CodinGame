package multiplayer;

import java.util.*;

// Please note that this type of code is written quickly and without much regard for proper architecture or code conventions :)
class CodersOfTheCaribbean {
	public static void main(String args[]) {
		Scanner inputs = new Scanner(System.in);
		while (true) {
			CodersOfTheCaribbeanGameState.reset();
			int entityCount = inputs.nextInt();
			for (int i = 0; i < entityCount; i++) { CodersOfTheCaribbeanGameState.addOrUpdateEntity(inputs); }
			CodersOfTheCaribbeanGameState.removeBarrelsTaken();
			CodersOfTheCaribbeanGameState.reevaluateWhoIsTheSurvivor();
			CodersOfTheCaribbeanAction action;
			for (Ship ship : CodersOfTheCaribbeanGameState.getMyShipsAsList()) {
				if (CodersOfTheCaribbeanGameState.losing()) { action = ActionHandler.selectActionFighter(ship); } else {
					if (ship.getId() == CodersOfTheCaribbeanGameState.survivorId) {
						System.err.println("Survivor: " + CodersOfTheCaribbeanGameState.survivorId); action = ActionHandler.selectActionSurvivor(ship);
					} else { action = ActionHandler.selectActionFighter(ship); }
				}
				CodersOfTheCaribbeanGameState.updateGameState(ship, action);
				System.out.println(action);
			}
		}
	}
}

enum CodersOfTheCaribbeanAction {
	FASTER,
	SLOWER,
	PORT,
	STARBOARD,
	FIRE,
	MINE,
	WAIT;
	public Coord target;
	public boolean isAuthorized = true;

	public String toString() {
		if (this == FIRE && target != null) {
			return "FIRE " + target.x + " " + target.y;
		} else if (this == FIRE && target == null) {
			return "MINE";
		} else {
			return super.toString();
		}
	}

	public String toCoolerString() {
		if (this == FASTER) {
			return "Go Faster";
		} else if (this == SLOWER) {
			return "Go Slower";
		} else if (this == PORT) {
			return "Go Left";
		} else if (this == STARBOARD) {
			return "Go Right";
		} else if (this == FIRE) {
			return "FEUER FREI";
		} else if (this == MINE) {
			return "Mine the fuck out";
		} else {
			return "Don't do shit";
		}
	}

	public void print() { System.err.println(this.toCoolerString()); }
}

class ActionHandler {
	private static Map<Integer, CodersOfTheCaribbeanAction> previousActions = new HashMap<>();

	static CodersOfTheCaribbeanAction selectActionSurvivor(Ship ship) {
		CodersOfTheCaribbeanAction action = null;

		boolean shipIsCloseToOpponents = CollisionsUtils.positionsIsInCloseRangeWithShips(ship, CodersOfTheCaribbeanGameState.getOpponentShipsAsList());

		if (!shipIsCloseToOpponents && !CodersOfTheCaribbeanGameState.noMoreBarrels()) {
			Barrel closestBarrel = BarrelTrackingHandler.findClosestBarrel(ship.getPosition());
			if (closestBarrel != null) {
				System.err.println("Action fetch: " + closestBarrel.getPosition().print());
				action = MovementHandler.move(ship, closestBarrel.getPosition());
			}
		}

		if (action == null) {
			Coord fleeingTarget = FleeHandler.findFleeingTarget(ship);
			if (fleeingTarget != null) {
				System.err.println("Action flee: " + fleeingTarget.print());
				action = MovementHandler.move(ship, fleeingTarget);
			} else { FleeHandler.didNotFlee(ship); }
		} else { FleeHandler.didNotFlee(ship); }

		if (ship.getCannonCooldown() == 0 && (action == null || action == CodersOfTheCaribbeanAction.WAIT)) {
			FleeHandler.didNotFlee(ship);
			Coord target = FiringHandler.getFiringTarget(ship);
			if (target != null && target.isInsideMap()) {
				System.err.println("Action fire: " + target.print()); action = CodersOfTheCaribbeanAction.FIRE; action.target = target;
				ship.resetCannonCooldown();
			}
		}

		if (action == null) {
			FleeHandler.didNotFlee(ship);
			action = CodersOfTheCaribbeanAction.WAIT;
		}

		previousActions.put(ship.getId(), action);
		action.print();

		return action;
	}

	static CodersOfTheCaribbeanAction selectActionFighter(Ship ship) {
		CodersOfTheCaribbeanAction action = null;

		if (!CodersOfTheCaribbeanGameState.noMoreBarrels()) {
			Barrel closestBarrel = BarrelTrackingHandler.findClosestBarrel(ship.getPosition());
			if (closestBarrel != null) {
				System.err.println("Action fetch: " + closestBarrel.getPosition().print());
				action = MovementHandler.move(ship, closestBarrel.getPosition());
			}
		}

		if (action == null) {
			Coord attackTarget = AttackHandler.findAttackTarget(ship);
			if (attackTarget != null) {
				System.err.println("Action attack: " + attackTarget.print());
				action = MovementHandler.move(ship, attackTarget);
			} else { AttackHandler.didNotAttack(ship); }
		}

		if (ship.getCannonCooldown() == 0 && action == CodersOfTheCaribbeanAction.WAIT) {
			AttackHandler.didNotAttack(ship);
			Coord target = FiringHandler.getFiringTarget(ship);
			if (target != null && target.isInsideMap()) {
				System.err.println("Action fire: " + target.print()); action = CodersOfTheCaribbeanAction.FIRE; action.target = target;
				ship.resetCannonCooldown();
			}
		}

		if (action == null) {
			Coord fleeingTarget = FleeHandler.findFleeingTarget(ship);
			if (fleeingTarget != null) {
				System.err.println("Action flee: " + fleeingTarget.print());
				action = MovementHandler.move(ship, fleeingTarget);
			} else { FleeHandler.didNotFlee(ship); }
		} else { FleeHandler.didNotFlee(ship); }

		if (action == null) {
			FleeHandler.didNotFlee(ship);

			if (ship.getCannonCooldown() == 0) {
				AttackHandler.didNotAttack(ship);
				Coord target = FiringHandler.getFiringTarget(ship);
				if (target != null && target.isInsideMap()) {
					System.err.println("Action fire: " + target.print()); action = CodersOfTheCaribbeanAction.FIRE; action.target = target;
					ship.resetCannonCooldown();
				}
			}
		}

		previousActions.put(ship.getId(), action);
		return action;
	}
}

class FiringHandler {
	static Coord getFiringTarget(Ship ship) {
		for (Barrel barrel : CodersOfTheCaribbeanGameState.getBarrels()) {
			int shipDistanceToBarrel = ship.getPosition().distanceTo(barrel.getPosition());
			if (shipDistanceToBarrel < Cts.FIRE_DISTANCE_MAX) {
				for (Ship opponentShip : CodersOfTheCaribbeanGameState.getOpponentShipsAsList()) {
					int opponentDistanceToBarrel = opponentShip.getPosition().distanceTo(barrel.getPosition()) - opponentShip.getSpeed();
					if (opponentDistanceToBarrel < shipDistanceToBarrel) {
						if (opponentShip.inFrontOf(opponentShip.getTargetAngle(barrel.getPosition()))) {
							if (ship.firingTravelTime(shipDistanceToBarrel) <= opponentDistanceToBarrel) { return barrel.getPosition(); }
						}
					}
				}
			}
		}

		Coord target = null;
		int targetTravelTime = Integer.MAX_VALUE;
		for (Ship opponentShip : CodersOfTheCaribbeanGameState.getOpponentShipsAsList()) {
			if (opponentShip.getSpeed() == 0) {
				Coord opponentPosition = opponentShip.getPosition();
				int travelTime = ship.firingTravelTime(opponentPosition);
				if (travelTime <= 3 && travelTime < targetTravelTime) { target = opponentPosition; targetTravelTime = travelTime; }
			} else if (opponentShip.getSpeed() == 1) {
				Coord opponentPosition = opponentShip.getNeighborSameOrientation(1);
				int travelTime = ship.firingTravelTime(opponentPosition);
				if (travelTime <= 2 && travelTime < targetTravelTime) { target = opponentPosition; targetTravelTime = travelTime; } else {
					opponentPosition = opponentPosition.neighbor(opponentShip.getOrientation());
					travelTime = ship.firingTravelTime(opponentPosition);
					if (travelTime <= 3 && travelTime < targetTravelTime) { target = opponentPosition; targetTravelTime = travelTime; }
				}
			} else if (opponentShip.getSpeed() == 2) {
				Coord opponentPosition = opponentShip.getNeighborSameOrientation(3);
				int travelTime = ship.firingTravelTime(opponentPosition);
				if (travelTime <= 1 && travelTime < targetTravelTime) { target = opponentPosition; targetTravelTime = travelTime; } else {
					opponentPosition = opponentPosition.neighbor(opponentShip.getOrientation());
					travelTime = ship.firingTravelTime(opponentPosition);
					if (travelTime <= 2 && travelTime < targetTravelTime) { target = opponentPosition; targetTravelTime = travelTime; } else {
						opponentPosition = opponentPosition.neighbor(opponentShip.getOrientation());
						travelTime = ship.firingTravelTime(opponentPosition);
						if (travelTime <= 3 && travelTime < targetTravelTime) { target = opponentPosition; targetTravelTime = travelTime; }
					}
				}
			}
		}

		if (target != null && target.intersectWithShips(CodersOfTheCaribbeanGameState.getMyShipsAsList())) { target = null; }
		return target;
	}
}

class AttackHandler {
	enum Direction {
		LEFT,
		FRONT,
		RIGHT,
		LEFT_LEFT,
		OPPOSITE,
		RIGHT_RIGHT
	}

	private static Direction[] leftPreferences = new Direction[]{Direction.LEFT, Direction.LEFT_LEFT, Direction.FRONT, Direction.RIGHT,
			Direction.RIGHT_RIGHT, Direction.OPPOSITE};
	private static Direction[] frontPreferences = new Direction[]{Direction.FRONT, Direction.LEFT, Direction.RIGHT, Direction.LEFT_LEFT,
			Direction.RIGHT_RIGHT, Direction.OPPOSITE};
	private static Direction[] rightPreferences = new Direction[]{Direction.RIGHT, Direction.RIGHT_RIGHT, Direction.FRONT, Direction.LEFT,
			Direction.LEFT_LEFT, Direction.OPPOSITE};
	private static Direction[] leftLeftPreferences = new Direction[]{Direction.LEFT_LEFT, Direction.LEFT, Direction.OPPOSITE, Direction.FRONT,
			Direction.RIGHT_RIGHT, Direction.RIGHT};
	private static Direction[] oppositePreferences = new Direction[]{Direction.OPPOSITE, Direction.LEFT_LEFT, Direction.RIGHT_RIGHT, Direction.LEFT,
			Direction.RIGHT, Direction.FRONT};
	private static Direction[] rightRightPreferences = new Direction[]{Direction.RIGHT_RIGHT, Direction.RIGHT, Direction.OPPOSITE, Direction.FRONT,
			Direction.LEFT_LEFT, Direction.LEFT};
	private static Map<Integer, Direction> previousActions = new HashMap<>();

	static void didNotAttack(Ship ship) { previousActions.put(ship.getId(), null); }

	static Coord findAttackTarget(Ship ship) {
		Direction direction = directionToAttack(ship);
		if (direction == null) { return null; }
		Direction previousAction = previousActions.get(ship.getId());

		if (previousAction != null && ship.getSpeed() == 0) {
			if (previousAction == Direction.RIGHT && direction == Direction.LEFT) {
				previousActions.put(ship.getId(), Direction.RIGHT); return findValidTarget(ship, Direction.RIGHT);
			} else if (previousAction == Direction.RIGHT_RIGHT && direction == Direction.LEFT_LEFT) {
				previousActions.put(ship.getId(), Direction.RIGHT_RIGHT); return findValidTarget(ship, Direction.RIGHT_RIGHT);
			} else if (previousAction == Direction.LEFT && direction == Direction.RIGHT) {
				previousActions.put(ship.getId(), Direction.LEFT); return findValidTarget(ship, Direction.LEFT);
			} else if (previousAction == Direction.LEFT_LEFT && direction == Direction.RIGHT_RIGHT) {
				previousActions.put(ship.getId(), Direction.LEFT_LEFT); return findValidTarget(ship, Direction.LEFT_LEFT);
			} else { previousActions.put(ship.getId(), direction); return findValidTarget(ship, direction); }
		} else { previousActions.put(ship.getId(), direction); return findValidTarget(ship, direction); }
	}

	private static Direction directionToAttack(Ship ship) {
		Ship shipToAttack = CodersOfTheCaribbeanGameState.findOpponentWithMostLife();
		if (shipToAttack == null) { return null; } // Game is over so we don't really care

		int distanceToShip = ship.getPosition().distanceTo(shipToAttack.getPosition());
		double shipToFleeAngle = ship.getTargetAngle(shipToAttack.getPosition());
		boolean isOpponentInFrontOfShip = ship.inFrontOf(shipToFleeAngle);
		boolean isOpponentOnTheRightSideOfShip = ship.rightSideOf(shipToFleeAngle);
		boolean isOpponentOnTheLeftSideOfShip = ship.leftSideOf(shipToFleeAngle);

		if (distanceToShip < 8) {
			if (shipToAttack.sameOrientation(ship) || shipToAttack.oppositeOrientation(ship)) {
				if (isOpponentInFrontOfShip) {
					if (isOpponentOnTheRightSideOfShip) {
						return Direction.RIGHT;
					} else if (isOpponentOnTheLeftSideOfShip) {
						return Direction.LEFT;
					} else {
						return Direction.FRONT;
					}
				} else {
					if (isOpponentOnTheRightSideOfShip) {
						return Direction.RIGHT_RIGHT;
					} else if (isOpponentOnTheLeftSideOfShip) {
						return Direction.LEFT_LEFT;
					} else {
						return Direction.OPPOSITE;
					}
				}
			} else if ((shipToAttack.leftOrientation(ship) || shipToAttack.rightOrientation(ship)) && !isOpponentInFrontOfShip) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.RIGHT_RIGHT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.LEFT_LEFT;
				} else {
					return Direction.LEFT_LEFT;
				}
			} else if (shipToAttack.leftOrientation(ship)) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.FRONT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.LEFT;
				} else {
					return Direction.LEFT;
				}
			} else if (shipToAttack.rightOrientation(ship)) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.RIGHT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.FRONT;
				} else {
					return Direction.RIGHT;
				}
			} else if (shipToAttack.leftLeftOrientation(ship)) {
				if (isOpponentInFrontOfShip && (isOpponentOnTheRightSideOfShip || !isOpponentOnTheLeftSideOfShip)) { return Direction.LEFT; } else {
					return Direction.LEFT_LEFT;
				}
			} else if (shipToAttack.rightRightOrientation(ship)) {
				if (isOpponentInFrontOfShip && (isOpponentOnTheLeftSideOfShip || !isOpponentOnTheRightSideOfShip)) { return Direction.RIGHT; } else {
					return Direction.RIGHT_RIGHT;
				}
			} else { return Direction.FRONT; }
		} else {
			if (isOpponentInFrontOfShip) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.RIGHT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.LEFT;
				} else {
					return Direction.FRONT;
				}
			} else {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.RIGHT_RIGHT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.LEFT_LEFT;
				} else {
					return Direction.OPPOSITE;
				}
			}
		}
	}

	private static Coord findValidTarget(Ship ship, Direction preferredDirection) {
		// 3 neighbors seems to work best
		Direction[] preferredDirections = preferredDirection == Direction.LEFT
				? leftPreferences
				: preferredDirection == Direction.RIGHT
						? rightPreferences
						: preferredDirection == Direction.FRONT
								? frontPreferences
								: preferredDirection == Direction.LEFT_LEFT
										? leftLeftPreferences
										: preferredDirection == Direction.RIGHT_RIGHT ? rightRightPreferences : oppositePreferences;
		// 3 neighbors seems to work best
		for (Direction direction : preferredDirections) {
			if (direction == Direction.LEFT) {
				Coord leftTarget = ship.getNeighborLeftOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), leftTarget, ship.getLeftOrientation(), 1)) { return leftTarget; }
			} else if (direction == Direction.FRONT) {
				Coord frontTarget = ship.getNeighborSameOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), frontTarget, ship.getSameOrientation(), 1)) { return frontTarget; }
			} else if (direction == Direction.RIGHT) {
				Coord rightTarget = ship.getNeighborRightOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), rightTarget, ship.getRightOrientation(), 1)) { return rightTarget; }
			} else if (direction == Direction.LEFT_LEFT) {
				Coord leftLeftTarget = ship.getNeighborLeftLeftOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), leftLeftTarget, ship.getLeftLeftOrientation(), 1)) { return leftLeftTarget; }
			} else if (direction == Direction.OPPOSITE) {
				Coord oppositeTarget = ship.getNeighborOppositeOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), oppositeTarget, ship.getOppositeOrientation(), 1)) { return oppositeTarget; }
			} else if (direction == Direction.RIGHT_RIGHT) {
				Coord rightRightTarget = ship.getNeighborRightRightOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), rightRightTarget, ship.getRightRightOrientation(), 1)) { return rightRightTarget; }
			}
		}
		return null;
	}
}

class FleeHandler {
	enum Direction {
		LEFT,
		FRONT,
		RIGHT,
		LEFT_LEFT,
		OPPOSITE,
		RIGHT_RIGHT;
	}

	private static Direction[] leftPreferences = new Direction[]{Direction.LEFT, Direction.LEFT_LEFT, Direction.FRONT, Direction.RIGHT,
			Direction.RIGHT_RIGHT, Direction.OPPOSITE};
	private static Direction[] frontPreferences = new Direction[]{Direction.FRONT, Direction.LEFT, Direction.RIGHT, Direction.LEFT_LEFT,
			Direction.RIGHT_RIGHT, Direction.OPPOSITE};
	private static Direction[] rightPreferences = new Direction[]{Direction.RIGHT, Direction.RIGHT_RIGHT, Direction.FRONT, Direction.LEFT,
			Direction.LEFT_LEFT, Direction.OPPOSITE};
	private static Map<Integer, Direction> previousActions = new HashMap<>();

	static void didNotFlee(Ship ship) { previousActions.put(ship.getId(), null); }

	static Coord findFleeingTarget(Ship ship) {
		Ship shipToFlee = CodersOfTheCaribbeanGameState.findClosestOpponent(ship.getPosition()); // now there's 3 ships, need refacto
		if (shipToFlee == null) { return findValidTarget(ship, Direction.FRONT); } // Game is over so we don't really care

		Quadrant shipQuadrant = ship.getPosition().getQuadrant();
		Quadrant shipToFleeQuadrant = shipToFlee.getPosition().getQuadrant();
		if (!shipQuadrant.equals(shipToFleeQuadrant)) { return findQuadrantToFlee(shipQuadrant, shipToFleeQuadrant); }

		Direction direction = directionToFlee(ship, shipToFlee);
		Direction previousAction = previousActions.get(ship.getId());

		if (previousAction != null && previousAction != Direction.FRONT) {
			if (previousAction == direction) { previousActions.put(ship.getId(), direction); return findValidTarget(ship, direction); } else {
				previousActions.put(ship.getId(), Direction.FRONT); return findValidTarget(ship, Direction.FRONT);
			}
		} else { previousActions.put(ship.getId(), direction); return findValidTarget(ship, direction); }
	}

	private static Direction directionToFlee(Ship ship, Ship shipToFlee) {
		double shipToFleeAngle = ship.getTargetAngle(shipToFlee.getPosition());
		boolean isOpponentInFrontOfShip = ship.inFrontOf(shipToFleeAngle);
		boolean isOpponentOnTheRightSideOfShip = ship.rightSideOf(shipToFleeAngle);
		boolean isOpponentOnTheLeftSideOfShip = ship.leftSideOf(shipToFleeAngle);

		if (shipToFlee.sameOrientation(ship)) {
			if (isOpponentInFrontOfShip) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			} else {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			}
		} else if (shipToFlee.oppositeOrientation(ship)) {
			if (isOpponentInFrontOfShip) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			} else {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			}
		} else if (shipToFlee.leftOrientation(ship)) {
			if (isOpponentInFrontOfShip) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			} else {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			}
		} else if (shipToFlee.rightOrientation(ship)) {
			if (isOpponentInFrontOfShip) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			} else {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			}
		} else if (shipToFlee.leftLeftOrientation(ship)) {
			if (isOpponentInFrontOfShip) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			} else {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.RIGHT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			}
		} else if (shipToFlee.rightRightOrientation(ship)) {
			if (isOpponentInFrontOfShip) {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.RIGHT;
				} else {
					return Direction.FRONT;
				}
			} else {
				if (isOpponentOnTheRightSideOfShip) {
					return Direction.LEFT;
				} else if (isOpponentOnTheLeftSideOfShip) {
					return Direction.LEFT;
				} else {
					return Direction.FRONT;
				}
			}
		} else { return Direction.FRONT; }
	}

	private static Coord findQuadrantToFlee(Quadrant shipQuadrant, Quadrant shipToFleeQuadrant) {
		// use quadrants
		Quadrant result = new Quadrant();
		result.setX(0, Cts.MAP_WIDTH);
		result.setY(0, Cts.MAP_HEIGHT);

		for (int rotation = 0; rotation < 4; rotation++) {
			if (shipToFleeQuadrant.equals(0, 0, rotation)) {
				if (shipQuadrant.equals(new int[][]{{1, 1}, {2, 1}, {1, 2}, {3, 1}, {3, 2}, {3, 3}, {1, 3}, {2, 3}}, rotation)) {
					result.setQuadrant(2, 2);
				} else if (shipQuadrant.equals(new int[][]{{1, 0}, {2, 0}, {3, 0}}, rotation)) {
					result.setQuadrant(2, 1);
				} else if (shipQuadrant.equals(new int[][]{{0, 1}, {0, 2}, {0, 3}}, rotation)) {
					result.setQuadrant(1, 2);
				} else if (shipQuadrant.equals(0, 0, rotation)) {
					result.setQuadrant(1, 1);
				}
			} else if (shipToFleeQuadrant.equals(0, 1, rotation)) {
				if (shipQuadrant.equals(new int[][]{{1, 1}, {2, 1}, {1, 2}, {3, 1}, {3, 2}, {3, 3}, {1, 3}, {2, 3}}, rotation)) {
					result.setQuadrant(2, 2);
				} else if (shipQuadrant.equals(new int[][]{{1, 0}, {2, 0}, {3, 0}}, rotation)) {
					result.setQuadrant(2, 1);
				} else if (shipQuadrant.equals(0, 0, rotation)) {
					result.setQuadrant(1, 0);
				} else if (shipQuadrant.equals(0, 2, rotation)) {
					result.setQuadrant(1, 2);
				} else if (shipQuadrant.equals(0, 3, rotation)) {
					result.setQuadrant(1, 3);
				} else if (shipQuadrant.equals(0, 1, rotation)) {
					result.setQuadrant(1, 2);
				}
			} else if (shipToFleeQuadrant.equals(1, 0, rotation)) {
				if (shipQuadrant.equals(new int[][]{{1, 1}, {2, 1}, {1, 2}, {3, 1}, {3, 2}, {3, 3}, {1, 3}, {2, 3}}, rotation)) {
					result.setQuadrant(2, 2);
				} else if (shipQuadrant.equals(new int[][]{{0, 1}, {0, 2}, {0, 3}}, rotation)) {
					result.setQuadrant(1, 2);
				} else if (shipQuadrant.equals(0, 0, rotation)) {
					result.setQuadrant(0, 1);
				} else if (shipQuadrant.equals(2, 0, rotation)) {
					result.setQuadrant(2, 1);
				} else if (shipQuadrant.equals(3, 0, rotation)) {
					result.setQuadrant(3, 1);
				} else if (shipQuadrant.equals(1, 0, rotation)) {
					result.setQuadrant(2, 1);
				}
			} else if (shipToFleeQuadrant.equals(1, 1, rotation)) {
				if (shipQuadrant.equals(0, 0, rotation)) {
					result.setQuadrant(0, 1);
				} else if (shipQuadrant.equals(0, 1, rotation)) {
					result.setQuadrant(0, 2);
				} else if (shipQuadrant.equals(1, 0, rotation)) {
					result.setQuadrant(2, 0);
				} else if (shipQuadrant.equals(new int[][]{{0, 2}, {0, 3}}, rotation)) {
					result.setQuadrant(1, 3);
				} else if (shipQuadrant.equals(new int[][]{{2, 0}, {3, 0}}, rotation)) {
					result.setQuadrant(3, 1);
				} else if (shipQuadrant.equals(new int[][]{{1, 2}, {1, 3}}, rotation)) {
					result.setQuadrant(2, 3);
				} else if (shipQuadrant.equals(new int[][]{{2, 1}, {3, 1}}, rotation)) {
					result.setQuadrant(3, 2);
				} else if (shipQuadrant.equals(2, 3, rotation)) {
					result.setQuadrant(3, 2);
				} else if (shipQuadrant.equals(3, 2, rotation)) {
					result.setQuadrant(2, 3);
				} else if (shipQuadrant.equals(2, 2, rotation)) {
					result.setQuadrant(3, 3);
				} else if (shipQuadrant.equals(3, 3, rotation)) {
					result.setQuadrant(2, 2);
				} else if (shipQuadrant.equals(1, 1, rotation)) {
					result.setQuadrant(2, 2);
				}
			}
		}

		return findQuadrantTarget(result);
	}

	private static Coord findValidTarget(Ship ship, Direction preferredDirection) {
		// 3 neighbors seems to work best
		Direction[] preferredDirections = preferredDirection == Direction.LEFT
				? leftPreferences
				: preferredDirection == Direction.RIGHT ? rightPreferences : frontPreferences;
		// 3 neighbors seems to work best
		for (Direction direction : preferredDirections) {
			if (direction == Direction.LEFT) {
				Coord leftTarget = ship.getNeighborLeftOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), leftTarget, ship.getLeftOrientation(), 1)) { return leftTarget; }
			} else if (direction == Direction.FRONT) {
				Coord frontTarget = ship.getNeighborSameOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), frontTarget, ship.getSameOrientation(), 1)) { return frontTarget; }
			} else if (direction == Direction.RIGHT) {
				Coord rightTarget = ship.getNeighborRightOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), rightTarget, ship.getRightOrientation(), 1)) { return rightTarget; }
			} else if (direction == Direction.LEFT_LEFT) {
				Coord leftLeftTarget = ship.getNeighborLeftLeftOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), leftLeftTarget, ship.getLeftLeftOrientation(), 1)) { return leftLeftTarget; }
			} else if (direction == Direction.OPPOSITE) {
				Coord oppositeTarget = ship.getNeighborOppositeOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), oppositeTarget, ship.getOppositeOrientation(), 1)) { return oppositeTarget; }
			} else if (direction == Direction.RIGHT_RIGHT) {
				Coord rightRightTarget = ship.getNeighborRightRightOrientation(3);
				if (!CollisionsUtils.shipCollides(ship.getId(), rightRightTarget, ship.getRightRightOrientation(), 1)) { return rightRightTarget; }
			}
		}
		return null;
	}

	private static Coord findQuadrantTarget(Quadrant quadrant) {
		return new Coord(
				((quadrant.maxX + quadrant.minX) / 2),
				((quadrant.maxY + quadrant.minY) / 2)
		);
	}
}

class MovementHandler {
	static CodersOfTheCaribbeanAction move(Ship ship, Coord targetPosition) {
		boolean fasterIsAuthorized = false;
		boolean slowerIsAuthorized = false;
		boolean waitIsAuthorized = false;
		boolean portIsAuthorized = false;
		boolean starboardIsAuthorized = false;

		boolean fasterCollidesWithMine = true;
		//		boolean slowerCollidesWithMine = true;
		boolean waitCollidesWithMine = true;
		boolean portCollidesWithMine = true;
		boolean starboardCollidesWithMine = true;

		boolean fasterCollidesWithCannonball = true;
		//		boolean slowerCollidesWithCannonball = true;
		boolean waitCollidesWithCannonball = true;
		boolean portCollidesWithCannonball = true;
		boolean starboardCollidesWithCannonball = true;

		List<CodersOfTheCaribbeanAction> availableOptions;

		availableOptions = checkingEdgeOfMap(ship); // Add check for speed == 2
		printOptions("checking EdgeOfMap", availableOptions);
		for (CodersOfTheCaribbeanAction action : availableOptions) {
			if (action == CodersOfTheCaribbeanAction.FASTER) {
				fasterIsAuthorized = action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.SLOWER) {
				slowerIsAuthorized = action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.WAIT) {
				waitIsAuthorized = action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.PORT) {
				portIsAuthorized = action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.STARBOARD) {
				starboardIsAuthorized = action.isAuthorized;
			}
		}

		availableOptions = checkingCollisions(CollisionsUtils.Type.SHIP, ship);
		printOptions("checking SHIP Collisions", availableOptions);
		for (CodersOfTheCaribbeanAction action : availableOptions) {
			if (action == CodersOfTheCaribbeanAction.FASTER) {
				fasterIsAuthorized = fasterIsAuthorized && action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.SLOWER) {
				slowerIsAuthorized = slowerIsAuthorized && action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.WAIT) {
				waitIsAuthorized = waitIsAuthorized && action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.PORT) {
				portIsAuthorized = portIsAuthorized && action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.STARBOARD) {
				starboardIsAuthorized = starboardIsAuthorized && action.isAuthorized;
			}
		}

		availableOptions = checkingCollisions(CollisionsUtils.Type.MINE, ship);
		printOptions("checking MINE Collisions", availableOptions);
		for (CodersOfTheCaribbeanAction action : availableOptions) {
			if (action == CodersOfTheCaribbeanAction.FASTER) {
				fasterCollidesWithMine = !action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.SLOWER) {
				//				slowerCollidesWithMine = !action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.WAIT) {
				waitCollidesWithMine = !action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.PORT) {
				portCollidesWithMine = !action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.STARBOARD) {
				starboardCollidesWithMine = !action.isAuthorized;
			}
		}

		availableOptions = checkingCollisions(CollisionsUtils.Type.CANNONBALL, ship);
		printOptions("checking CANNONBALL Collisions", availableOptions);
		for (CodersOfTheCaribbeanAction action : availableOptions) {
			if (action == CodersOfTheCaribbeanAction.FASTER) {
				fasterCollidesWithCannonball = !action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.SLOWER) {
				//				slowerCollidesWithCannonball = !action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.WAIT) {
				waitCollidesWithCannonball = !action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.PORT) {
				portCollidesWithCannonball = !action.isAuthorized;
			} else if (action == CodersOfTheCaribbeanAction.STARBOARD) {
				starboardCollidesWithCannonball = !action.isAuthorized;
			}
		}

		boolean collisionWithCannonballOnCenter = ship.getPosition().intersectWithEntities(CodersOfTheCaribbeanGameState.getCannonballsThatExplodesInXTurns(1));
		if (collisionWithCannonballOnCenter) { System.err.println("COLLISION WITH CANNONBALL ON CENTER"); }

		if (ship.getSpeed() == 0) {
			double targetAngle = ship.getTargetAngle(targetPosition);
			double angleStraight = ship.getAngleStraight(targetAngle);
			double anglePort = ship.getAnglePort(targetAngle);
			double angleStarboard = ship.getAngleStarboard(targetAngle);

			double centerAngle = ship.getAngleToCenter();
			double anglePortCenter = ship.getAnglePort(centerAngle);
			double angleStarboardCenter = ship.getAngleStarboard(centerAngle);

			boolean forwardIsBetterThanBothLeftAndRight = !(fasterCollidesWithCannonball || fasterCollidesWithMine) && ((portCollidesWithCannonball
					|| portCollidesWithMine) || angleStraight <= anglePort) && ((starboardCollidesWithCannonball || starboardCollidesWithMine)
					|| angleStraight <= angleStarboard);
			boolean rightIsBetterThanLeft = !(starboardCollidesWithCannonball || starboardCollidesWithMine) && ((portCollidesWithCannonball
					|| portCollidesWithMine) || angleStarboard < anglePort);
			boolean rightIsBetterThanLeftInCaseOfEquality = !(starboardCollidesWithCannonball || starboardCollidesWithMine) && (angleStarboard
					== anglePort && angleStarboardCenter < anglePortCenter
					|| angleStarboard == anglePort && angleStarboardCenter == anglePortCenter && (ship.getOrientation() == 1
					|| ship.getOrientation() == 4));

			if (!fasterIsAuthorized && !portIsAuthorized && !starboardIsAuthorized) {
				return CodersOfTheCaribbeanAction.WAIT;
			} else if (fasterIsAuthorized && (collisionWithCannonballOnCenter || (portCollidesWithCannonball && starboardCollidesWithCannonball))) {
				return CodersOfTheCaribbeanAction.FASTER;
			} else if (starboardIsAuthorized && fasterCollidesWithCannonball && portCollidesWithCannonball) {
				return CodersOfTheCaribbeanAction.STARBOARD;
			} else if (portIsAuthorized && fasterCollidesWithCannonball && starboardCollidesWithCannonball) {
				return CodersOfTheCaribbeanAction.PORT;
			}

			// Cannonball is better than mine
			else if (fasterIsAuthorized && (fasterCollidesWithCannonball || fasterCollidesWithMine) && (portCollidesWithCannonball
					|| portCollidesWithMine) && (starboardCollidesWithCannonball || starboardCollidesWithMine)) {
				return CodersOfTheCaribbeanAction.FASTER;
			} // TODO redo

			else {
				if (forwardIsBetterThanBothLeftAndRight) {
					return CodersOfTheCaribbeanAction.FASTER;
				} else if (rightIsBetterThanLeft || rightIsBetterThanLeftInCaseOfEquality) {
					return CodersOfTheCaribbeanAction.STARBOARD;
				} else {
					return CodersOfTheCaribbeanAction.PORT;
				}
			}
		} else if (ship.getSpeed() == 1) {
			double targetAngle = ship.getTargetAngle(targetPosition);
			double angleStraight = ship.getAngleStraight(targetAngle);

			int forwardDistanceToTarget = !waitIsAuthorized || (waitCollidesWithCannonball || waitCollidesWithMine)
					? Integer.MAX_VALUE
					: ship.getNeighborSameOrientation(1).distanceTo(targetPosition);
			int portDistanceToTarget = !portIsAuthorized || (portCollidesWithCannonball || portCollidesWithMine)
					? Integer.MAX_VALUE
					: ship.getNeighborLeftOrientation(1).distanceTo(targetPosition);
			int starboardDistanceToTarget = !starboardIsAuthorized || (starboardCollidesWithCannonball || starboardCollidesWithMine)
					? Integer.MAX_VALUE
					: ship.getNeighborRightOrientation(1).distanceTo(targetPosition);

			if (slowerIsAuthorized && !waitIsAuthorized && !portIsAuthorized && !starboardIsAuthorized && !fasterIsAuthorized) {
				return CodersOfTheCaribbeanAction.SLOWER;
			} else if (waitIsAuthorized && ship.getPosition().distanceTo(targetPosition) == 1 && angleStraight > 1.5 && !(waitCollidesWithCannonball
					|| waitCollidesWithMine)) {
				return CodersOfTheCaribbeanAction.WAIT; // WAIT seems better, since moving is life
			} // TODO redo

			//if (fasterIsAuthorized && collisionWithCannonballOnCenter) { return Action.FASTER; }

			else {
				//if (fasterIsAuthorized && ! (fasterCollidesWithCannonball || fasterCollidesWithMine)) {
				//    return Action.FASTER;
				//}
				if (forwardDistanceToTarget <= starboardDistanceToTarget && forwardDistanceToTarget <= portDistanceToTarget) {
					return CodersOfTheCaribbeanAction.WAIT;
				} else if (portDistanceToTarget <= starboardDistanceToTarget && portDistanceToTarget <= forwardDistanceToTarget) {
					return CodersOfTheCaribbeanAction.PORT;
				} else if (starboardDistanceToTarget <= forwardDistanceToTarget && starboardDistanceToTarget <= portDistanceToTarget) {
					return CodersOfTheCaribbeanAction.STARBOARD;
				}
			}
		} else {
			// speed == 2
			// SLOWER, WAIT, PORT, STARBOARD
			double targetAngle = ship.getTargetAngle(targetPosition);
			double angleStraight = ship.getAngleStraight(targetAngle);

			int forwardDistanceToTarget = !waitIsAuthorized || (waitCollidesWithCannonball || waitCollidesWithMine)
					? Integer.MAX_VALUE
					: ship.getNeighborSameOrientation(2).distanceTo(targetPosition);
			int portDistanceToTarget = !portIsAuthorized || (portCollidesWithCannonball || portCollidesWithMine)
					? Integer.MAX_VALUE
					: ship.getNeighborLeftOrientation(2).distanceTo(targetPosition);
			int starboardDistanceToTarget = !starboardIsAuthorized || (starboardCollidesWithCannonball || starboardCollidesWithMine)
					? Integer.MAX_VALUE
					: ship.getNeighborRightOrientation(2).distanceTo(targetPosition);

			if (slowerIsAuthorized && !waitIsAuthorized && !portIsAuthorized && !starboardIsAuthorized) {
				return CodersOfTheCaribbeanAction.SLOWER;
			} else if (waitIsAuthorized && ship.getPosition().distanceTo(targetPosition) == 2 && angleStraight > 1.5 && !(waitCollidesWithCannonball
					|| waitCollidesWithMine)) {
				return CodersOfTheCaribbeanAction.WAIT; // WAIT seems better, since moving is life
			} // TODO redo

			else {
				if (forwardDistanceToTarget <= starboardDistanceToTarget && forwardDistanceToTarget <= portDistanceToTarget) {
					return CodersOfTheCaribbeanAction.WAIT;
				} else if (portDistanceToTarget <= starboardDistanceToTarget && portDistanceToTarget <= forwardDistanceToTarget) {
					return CodersOfTheCaribbeanAction.PORT;
				} else if (starboardDistanceToTarget <= forwardDistanceToTarget && starboardDistanceToTarget <= portDistanceToTarget) {
					return CodersOfTheCaribbeanAction.STARBOARD;
				}
			}
		}

		return CodersOfTheCaribbeanAction.WAIT;
	}

	public static List<CodersOfTheCaribbeanAction> checkingEdgeOfMap(Ship ship) {
		int x = ship.previousPosition.x; // TEMP while I update this function
		int y = ship.previousPosition.y; // TEMP while I update this function
		int orient = ship.getOrientation();

		boolean fasterIsAuthorized = true;
		boolean slowerIsAuthorized = true;
		boolean portIsAuthorized = true;
		boolean starboardIsAuthorized = true;
		boolean waitIsAuthorized = true;

		if (ship.getSpeed() == 0) {
			if (y % 2 == 1) {
				if (x == 0) {
					if (orient == 2) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					}
					if (orient == 3) {
						fasterIsAuthorized = false;
						waitIsAuthorized = false;
					}
					if (orient == 4) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					}
				} else if (x == 22) {
					if (orient == 1) {
						fasterIsAuthorized = false;
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					}
					if (orient == 3) {
						fasterIsAuthorized = false;
						waitIsAuthorized = false;
					}
					if (orient == 5) {
						fasterIsAuthorized = false;
						waitIsAuthorized = false;
						portIsAuthorized = false;
					}
				}
			} else {
				if (x == 0) {
					if (orient == 2) {
						fasterIsAuthorized = false;
						waitIsAuthorized = false;
						portIsAuthorized = false;
					}
					if (orient == 3) {
						fasterIsAuthorized = false;
						waitIsAuthorized = false;
					}
					if (orient == 4) {
						fasterIsAuthorized = false;
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					}
				} else if (x == 22) {
					if (orient == 1) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					}
					if (orient == 3) {
						fasterIsAuthorized = false;
						waitIsAuthorized = false;
					}
					if (orient == 5) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					}
				}
			}
		} else if (ship.getSpeed() == 1) {
			if (y % 2 == 1) {
				if (x == 0) {
					if (orient == 2) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					}
					if (orient == 4) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					}
				} else if (x == 1 && orient == 3) {
					if (y == 1) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					} else if (y == 19) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					} else {
						waitIsAuthorized = false;
					}
				} else if (x == 20 && orient == 0) {
					if (y == 1) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					} else {
						waitIsAuthorized = false;
					}
				} else if (x == 21) {
					if (orient == 1) {
						starboardIsAuthorized = false;
					}
					if (orient == 5) {
						portIsAuthorized = false;
					}
				}
			} else {
				if (x == 1) {
					if (orient == 2) {
						portIsAuthorized = false;
					}
					if (orient == 4) {
						starboardIsAuthorized = false;
					}
				} else if (x == 2 && orient == 3) {
					if (y == 0) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					} else if (y == 20) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					} else {
						waitIsAuthorized = false;
					}
				} else if (x == 21 && orient == 0) {
					if (y == 0) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					} else if (y == 20) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					} else {
						waitIsAuthorized = false;
					}
				} else if (x == 22) {
					if (orient == 1) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					}
					if (orient == 5) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					}
				}
			}

			if (y == 1) {
				if (orient == 1) {
					waitIsAuthorized = false;
					portIsAuthorized = false;
				} else if (orient == 2) {
					waitIsAuthorized = false;
					starboardIsAuthorized = false;
				}
			} else if (y == 19) {
				if (orient == 4) {
					waitIsAuthorized = false;
					portIsAuthorized = false;
				} else if (orient == 5) {
					waitIsAuthorized = false;
					starboardIsAuthorized = false;
				}
			}
		} else {
			if (y % 2 == 1) {
				if (x == 0) {
					if (orient == 2) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					}
					if (orient == 4) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					}
				} else if ((x == 1 || x == 2 || x == 3) && orient == 3) {
					if (y == 1) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					} else if (y == 19) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					} else {
						waitIsAuthorized = false;
					}
				} else if ((x == 20 || x == 19 || x == 18) && orient == 0) {
					if (y == 1) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					} else {
						waitIsAuthorized = false;
					}
				} else if (x == 21) {
					if (orient == 1) {
						starboardIsAuthorized = false;
					}
					if (orient == 5) {
						portIsAuthorized = false;
					}
				}
			} else {
				if (x == 1) {
					if (orient == 2) {
						portIsAuthorized = false;
					}
					if (orient == 4) {
						starboardIsAuthorized = false;
					}
				} else if ((x == 2 || x == 3 || x == 4) && orient == 3) {
					if (y == 0) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					} else if (y == 20) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					} else {
						waitIsAuthorized = false;
					}
				} else if ((x == 21 || x == 20 || x == 19) && orient == 0) {
					if (y == 0) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					} else if (y == 20) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					} else {
						waitIsAuthorized = false;
					}
				} else if (x == 22) {
					if (orient == 1) {
						waitIsAuthorized = false;
						starboardIsAuthorized = false;
					}
					if (orient == 5) {
						waitIsAuthorized = false;
						portIsAuthorized = false;
					}
				}
			}

			if (y == 1 || y == 2 || y == 3) {
				if (orient == 1) {
					waitIsAuthorized = false;
					portIsAuthorized = false;
				} else if (orient == 2) {
					waitIsAuthorized = false;
					starboardIsAuthorized = false;
				}
			} else if (y == 19 || y == 18 || y == 17) {
				if (orient == 4) {
					waitIsAuthorized = false;
					portIsAuthorized = false;
				} else if (orient == 5) {
					waitIsAuthorized = false;
					starboardIsAuthorized = false;
				}
			}
		}

		if (y == 0) {
			if (orient == 0) {
				waitIsAuthorized = false;
				portIsAuthorized = false;
			} else if (orient == 3) {
				waitIsAuthorized = false;
				starboardIsAuthorized = false;
			}
		} else if (y == 20) {
			if (orient == 0) {
				waitIsAuthorized = false;
				starboardIsAuthorized = false;
			} else if (orient == 3) {
				waitIsAuthorized = false;
				portIsAuthorized = false;
			}
		}

		List<CodersOfTheCaribbeanAction> availableOptions = new ArrayList<>();
		CodersOfTheCaribbeanAction action = CodersOfTheCaribbeanAction.FASTER;
		action.isAuthorized = fasterIsAuthorized;
		availableOptions.add(action);
		action = CodersOfTheCaribbeanAction.SLOWER;
		action.isAuthorized = slowerIsAuthorized;
		availableOptions.add(action);
		action = CodersOfTheCaribbeanAction.WAIT;
		action.isAuthorized = waitIsAuthorized;
		availableOptions.add(action);
		action = CodersOfTheCaribbeanAction.PORT;
		action.isAuthorized = portIsAuthorized;
		availableOptions.add(action);
		action = CodersOfTheCaribbeanAction.STARBOARD;
		action.isAuthorized = starboardIsAuthorized;
		availableOptions.add(action);

		return availableOptions;
	}

	private static List<CodersOfTheCaribbeanAction> checkingCollisions(CollisionsUtils.Type type, Ship ship) {
		boolean fasterIsAuthorized = true;
		boolean slowerIsAuthorized = true;
		boolean portIsAuthorized = true;
		boolean starboardIsAuthorized = true;
		boolean waitIsAuthorized = true;

		int sameOrientation = ship.getSameOrientation();
		int leftOrientation = ship.getLeftOrientation();
		int rightOrientation = ship.getRightOrientation();
		Coord samePosition = ship.getPosition();
		Coord positionOneForward = ship.getNeighborSameOrientation(1);
		Coord positionTwoForward = ship.getNeighborSameOrientation(2);
		Coord positionThreeForward = ship.getNeighborSameOrientation(3);
		Coord positionLeftOneForward = ship.getNeighborLeftOrientation(1);
		Coord positionRightOneForward = ship.getNeighborRightOrientation(1);
		Coord positionLeftTwoForward = ship.getNeighborLeftOrientation(2);
		Coord positionRightTwoForward = ship.getNeighborRightOrientation(2);

		if (ship.getSpeed() == 0) {
			// Faster
			if (CollisionsUtils.shipCollides(type, ship.getId(), positionOneForward, sameOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionTwoForward, sameOrientation, 2)) {
				fasterIsAuthorized = false;
			}
			// Port
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, leftOrientation, 1)) { portIsAuthorized = false; }
			// Starboard
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, rightOrientation, 1)) { starboardIsAuthorized = false; }
		} else if (ship.getSpeed() == 1) {
			// Faster
			if (CollisionsUtils.shipCollides(type, ship.getId(), positionOneForward, sameOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionThreeForward, sameOrientation, 2)) {
				fasterIsAuthorized = false;
			}
			// Slower
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, sameOrientation, 1)) { slowerIsAuthorized = false; }
			// Wait
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, sameOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionOneForward, sameOrientation, 2)) {
				waitIsAuthorized = false;
			}
			// Port
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, leftOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionLeftOneForward, leftOrientation, 2)) {
				portIsAuthorized = false;
			}
			// Starboard
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, rightOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionRightOneForward, rightOrientation, 2)) {
				starboardIsAuthorized = false;
			}
		} else if (ship.getSpeed() == 2) {
			// Slower
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, sameOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionOneForward, sameOrientation, 2)) {
				slowerIsAuthorized = false;
			}
			// Wait
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, sameOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionTwoForward, sameOrientation, 2)) {
				waitIsAuthorized = false;
			}
			// Port
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, leftOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionLeftTwoForward, leftOrientation, 2)) {
				portIsAuthorized = false;
			}
			// Starboard
			if (CollisionsUtils.shipCollides(type, ship.getId(), samePosition, rightOrientation, 1)
					|| CollisionsUtils.shipCollides(type, ship.getId(), positionRightTwoForward, rightOrientation, 2)) {
				starboardIsAuthorized = false;
			}
			System.err.println("positionRightTwoForward " + positionRightTwoForward);
		}

		List<CodersOfTheCaribbeanAction> availableOptions = new ArrayList<>();
		CodersOfTheCaribbeanAction action = CodersOfTheCaribbeanAction.FASTER;
		action.isAuthorized = fasterIsAuthorized;
		availableOptions.add(action);
		action = CodersOfTheCaribbeanAction.SLOWER;
		action.isAuthorized = slowerIsAuthorized;
		availableOptions.add(action);
		action = CodersOfTheCaribbeanAction.WAIT;
		action.isAuthorized = waitIsAuthorized;
		availableOptions.add(action);
		action = CodersOfTheCaribbeanAction.PORT;
		action.isAuthorized = portIsAuthorized;
		availableOptions.add(action);
		action = CodersOfTheCaribbeanAction.STARBOARD;
		action.isAuthorized = starboardIsAuthorized;
		availableOptions.add(action);

		return availableOptions;
	}

	private static void printOptions(String title, List<CodersOfTheCaribbeanAction> availableOptions) {
		System.err.print(title);
		for (CodersOfTheCaribbeanAction action : availableOptions) {
			System.err.print(" " + action.isAuthorized + " |");
		}
		System.err.println("");
	}
}

class BarrelTrackingHandler {
	static Barrel findClosestBarrel(Coord position) {
		Barrel barrel = (Barrel) position.findClosest(CodersOfTheCaribbeanGameState.getBarrelsNotTaken());
		if (barrel != null) { barrel.setAimedByMyShip(true); }
		return barrel;
	}
}

class CollisionsUtils {
	enum Type {
		SHIP,
		CANNONBALL,
		MINE
	}

	static boolean shipCollides(Type type, int shipId, Coord position, int orientation, int turn) {
		if (type == Type.SHIP) {
			return shipCollidesWithWall(shipId, position, orientation);
		} else if (type == Type.CANNONBALL) {
			return shipCollidesWithCannonball(shipId, position, orientation, turn);
		} else if (type == Type.MINE) {
			return shipCollidesWithMine(shipId, position, orientation);
		} else {
			return shipCollides(shipId, position, orientation, turn);
		}
	}

	static boolean shipCollides(int shipId, Coord position, int orientation, int turn) {
		Ship ghostShip = new Ship(shipId, position, orientation);
		return shipCollides(ghostShip, turn);
	}

	private static boolean shipCollides(Ship ship, int turn) {
		return shipCollidesWithMapBorder(ship)
				|| shipCollidesWithWall(ship)
				|| shipCollidesWithCannonball(ship, turn)
				|| shipCollidesWithMine(ship);
	}

	public static boolean shipCollidesWithMapBorder(int shipId, Coord position, int orientation) {
		return shipCollidesWithMapBorder(new Ship(shipId, position, orientation));
	}

	private static boolean shipCollidesWithMapBorder(Ship ship) { return !ship.getPosition().isInsideMap(); }

	private static boolean shipCollidesWithWall(int shipId, Coord position, int orientation) {
		return shipCollidesWithWall(new Ship(shipId, position, orientation));
	}

	static boolean shipCollidesWithWall(Ship ship) {
		return ship.intersectWithShips(CodersOfTheCaribbeanGameState.getOpponentShipsAsList())
				|| ship.intersectWithShips(CodersOfTheCaribbeanGameState.getMyOtherShipsAsList(ship.getId()));
	}

	private static boolean shipCollidesWithCannonball(int shipId, Coord position, int orientation, int turn) {
		return shipCollidesWithCannonball(new Ship(shipId, position, orientation), turn);
	}

	private static boolean shipCollidesWithCannonball(Ship ship, int turn) {
		return ship.intersectWithEntities(CodersOfTheCaribbeanGameState.getCannonballsThatExplodesInXTurns(turn));
	}

	private static boolean shipCollidesWithMine(int shipId, Coord position, int orientation) {
		return shipCollidesWithMine(new Ship(shipId, position, orientation));
	}

	private static boolean shipCollidesWithMine(Ship ship) { return ship.intersectWithEntities(CodersOfTheCaribbeanGameState.getMines()); }

	public static boolean shipCollidesWithBarrel(int shipId, Coord position, int orientation) {
		return shipCollidesWithBarrel(new Ship(shipId, position, orientation));
	}

	private static boolean shipCollidesWithBarrel(Ship ship) { return ship.intersectWithEntities(CodersOfTheCaribbeanGameState.getBarrels()); }

	static boolean positionsIsInCloseRangeWithShips(Ship thisShip, List<Ship> ships) {
		if (thisShip != null) {
			for (Ship ship : ships) {
				if (thisShip.getId() != ship.getId() && positionsIsInCloseRangeWithShip(thisShip, ship)) { return true; }
			}
		}
		return false;
	}

	private static boolean positionsIsInCloseRangeWithShip(Ship ship, Ship other) {
		return bowIsInCloseRangeWithShip(ship, other)
				|| centerIsInCloseRangeWithShip(ship, other)
				|| sternIsInCloseRangeWithShip(ship, other);
	}

	private static boolean bowIsInCloseRangeWithShip(Ship ship, Ship other) {
		return ship.getBow().distanceTo(other.getBow())
				<= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE
				|| ship.getBow().distanceTo(other.getPosition()) <= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE
				|| ship.getBow().distanceTo(other.getStern()) <= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE;
	}

	private static boolean centerIsInCloseRangeWithShip(Ship ship, Ship other) {
		return ship.getPosition().distanceTo(other.getBow())
				<= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE
				|| ship.getPosition().distanceTo(other.getPosition()) <= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE
				|| ship.getPosition().distanceTo(other.getStern()) <= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE;
	}

	private static boolean sternIsInCloseRangeWithShip(Ship ship, Ship other) {
		return ship.getStern().distanceTo(other.getBow())
				<= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE
				|| ship.getStern().distanceTo(other.getPosition()) <= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE
				|| ship.getStern().distanceTo(other.getStern()) <= Cts.DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE;
	}
}

class CodersOfTheCaribbeanGameState {
	private static List<Ship> myShipsAsList = new ArrayList<>();
	private static List<Ship> opponentShipsAsList = new ArrayList<>();
	private static List<Mine> mines = new ArrayList<>();
	private static List<Cannonball> cannonballs = new ArrayList<>();
	private static List<Barrel> barrels = new ArrayList<>();
	static int survivorId = -1;
	private static boolean survivorIsAlive = false;
	private static HashMap<Integer, Integer> mineCooldowns = new HashMap<>();
	private static HashMap<Integer, Integer> cannonCooldowns = new HashMap<>();

	static void reset() {
		for (Ship ship : myShipsAsList) {
			mineCooldowns.put(ship.getId(), ship.decrementMineCooldown().getMineCooldown());
			cannonCooldowns.put(ship.getId(), ship.decrementCannonCooldown().getCannonCooldown());
		}
		survivorIsAlive = false;
		myShipsAsList = new ArrayList<>();
		opponentShipsAsList = new ArrayList<>();
		mines = new ArrayList<>();
		cannonballs = new ArrayList<>();
		barrels = new ArrayList<>();
	}

	static void addOrUpdateEntity(Scanner inputs) {
		int entityId = inputs.nextInt(); String entityType = inputs.next(); int x = inputs.nextInt(); int y = inputs.nextInt();

		if (entityType.equals("SHIP")) {
			Ship ship = new Ship(entityId, x, y, inputs.nextInt(), inputs.nextInt(), inputs.nextInt(), inputs.nextInt());
			ship.evaluateNextPosition();

			if (ship.getOwner() == 1) {
				if (survivorId == entityId) { survivorIsAlive = true; }
				Integer mineCooldown = mineCooldowns.get(entityId);
				Integer cannonCooldown = cannonCooldowns.get(entityId);
				ship.setMineCooldown(mineCooldown != null ? mineCooldown : 0);
				ship.setCannonCooldown(cannonCooldown != null ? cannonCooldown : 0);
				myShipsAsList.add(ship);
			} else { opponentShipsAsList.add(ship); }
		} else if (entityType.equals("MINE")) {
			mines.add(new Mine(entityId, x, y)); inputs.nextInt(); inputs.nextInt(); inputs.nextInt(); inputs.nextInt();
		} else if (entityType.equals("CANNONBALL")) {
			inputs.nextInt(); cannonballs.add(new Cannonball(entityId, x, y, inputs.nextInt())); inputs.nextInt(); inputs.nextInt();
		} else if (entityType.equals("BARREL")) {
			barrels.add(new Barrel(entityId, x, y, inputs.nextInt())); inputs.nextInt(); inputs.nextInt(); inputs.nextInt();
		}
	}

	static void updateGameState(Ship ship, CodersOfTheCaribbeanAction action) {
		if (action == CodersOfTheCaribbeanAction.FASTER) {
			ship.setSpeed(Cts.clamp(ship.getSpeed() + 1, 0, 2)); ship.setPosition(ship.getNeighborSameOrientation(1));
		} else if (action == CodersOfTheCaribbeanAction.SLOWER) {
			ship.setSpeed(Cts.clamp(ship.getSpeed() - 1, 0, 2));
		} else if (action == CodersOfTheCaribbeanAction.PORT) {
			ship.setOrientation(ship.getLeftOrientation());
		} else if (action == CodersOfTheCaribbeanAction.STARBOARD) {
			ship.setOrientation(ship.getRightOrientation());
		} else if (action == CodersOfTheCaribbeanAction.FIRE) {
			cannonballs.add(new Cannonball(-1, action.target.x, action.target.y, ship.firingTravelTime(action.target)));
		} else if (action == CodersOfTheCaribbeanAction.MINE) {
			Coord mineCoord = ship.previousSternCoordinate.neighbor(ship.getOppositeOrientation());
			mines.add(new Mine(-1, mineCoord.x, mineCoord.y));
		}
	}

	static void removeBarrelsTaken() {
		for (Ship ship : myShipsAsList) {
			for (int i = 0; i < barrels.size(); i++) {
				if (ship.getPosition().intersectWithEntity(barrels.get(i))
						|| ship.getBow().intersectWithEntity(barrels.get(i))
						|| ship.getStern().intersectWithEntity(barrels.get(i))) {
					barrels.remove(i);
				}
			}
		}
		for (Ship ship : opponentShipsAsList) {
			for (int i = 0; i < barrels.size(); i++) {
				if (ship.getPosition().intersectWithEntity(barrels.get(i))
						|| ship.getBow().intersectWithEntity(barrels.get(i))
						|| ship.getStern().intersectWithEntity(barrels.get(i))) {
					barrels.remove(i);
				}
			}
		}
	}

	static boolean noMoreBarrels() {
		for (Barrel barrel : barrels) {
			if (!barrel.getAimedByMyShip()) {
				return false;
			}
		}
		return true;
	}

	private static boolean noMoreOpponentShips() { return opponentShipsAsList.isEmpty(); }

	static boolean losing() {
		int myScore = 0;
		int opponentScore = 0;
		if (noMoreBarrels()) {
			for (Ship ship : myShipsAsList) { myScore = Math.max(ship.getHealth(), myScore); }
			for (Ship ship : opponentShipsAsList) { opponentScore = Math.max(ship.getHealth(), opponentScore); }
			if (myScore <= opponentScore) { return true; }
		}
		return false;
	}

	static void reevaluateWhoIsTheSurvivor() {
		if (!noMoreOpponentShips()) {
			if (!survivorIsAlive) { survivorId = myShipsAsList.get(0).getId(); }
			Ship survivor = null;
			for (Ship ship : myShipsAsList) {
				if (ship.getId() == survivorId) {
					survivor = ship;
				}
			}
			if (survivor != null) {
				Ship opponent = findClosestOpponent(survivor.getPosition());
				if (opponent != null) {
					int survivorDistance = opponent.getPosition().distanceTo(survivor.getPosition());
					int survivorHealth = survivor.getHealth();
					for (Ship ship : myShipsAsList) {
						if (ship.getId() != survivorId) {
							int distance = findClosestOpponent(ship.getPosition()).getPosition().distanceTo(ship.getPosition());
							int health = ship.getHealth();
							if (((survivorHealth + 10) < health) && (survivorDistance < distance)) {
								survivorId = ship.getId();
							} else if ((survivorHealth < health) && ((survivorDistance + 5) < distance)) {
								survivorId = ship.getId();
							}
						}
					}
				}
			} else { for (Ship ship : myShipsAsList) { survivorId = ship.getId(); } }
		}
	}

	static Ship findClosestOpponent(Coord position) { return (Ship) position.findClosest(opponentShipsAsList); }

	static Ship findOpponentWithMostLife() {
		Ship opponentWithMostLife = null;
		int chosenOpponentHealth = 0;
		for (Ship ship : opponentShipsAsList) {
			if (ship.getHealth() > chosenOpponentHealth) {
				chosenOpponentHealth = ship.getHealth();
				opponentWithMostLife = ship;
			}
		}
		return opponentWithMostLife;
	}

	static List<Ship> getMyOtherShipsAsList(int shipIdToRemove) {
		List<Ship> myOtherShips = new ArrayList<>();
		for (Ship ship : myShipsAsList) { if (ship.getId() != shipIdToRemove) { myOtherShips.add(ship); } }
		return myOtherShips;
	}

	static List<Ship> getMyShipsAsList() { return myShipsAsList; }

	static List<Ship> getOpponentShipsAsList() { return opponentShipsAsList; }

	static List<Mine> getMines() { return mines; }

	public static List<Cannonball> getCannonballs() { return cannonballs; }

	static List<Cannonball> getCannonballsThatExplodesInXTurns(int turn) {
		List<Cannonball> cannonballsThatExplodesInXTurns = new ArrayList<>();
		for (Cannonball cannonball : cannonballs) {
			if (cannonball.getRemainingTurns() == turn) { cannonballsThatExplodesInXTurns.add(cannonball); }
		}
		return cannonballsThatExplodesInXTurns;
	}

	static List<Barrel> getBarrelsNotTaken() {
		List<Barrel> myBarrelsNotTaken = new ArrayList<>();
		for (Barrel barrel : barrels) { if (!barrel.getAimedByMyShip()) { myBarrelsNotTaken.add(barrel); } }
		return myBarrelsNotTaken;
	}

	static List<Barrel> getBarrels() { return barrels; }
}

class Cts {
	static int clamp(int val, int min, int max) { return Math.max(min, Math.min(max, val)); }

	static final int MAP_WIDTH = 23;
	static final int MAP_HEIGHT = 21;
	public static final int MAX_SHIP_HEALTH = 100;
	static final int COOLDOWN_CANNON = 2;
	static final int COOLDOWN_MINE = 5;
	static final int FIRE_DISTANCE_MAX = 10;
	public static final int LOW_DAMAGE = 25;
	public static final int HIGH_DAMAGE = 50;
	public static final int MINE_DAMAGE = 25;
	public static final int NEAR_MINE_DAMAGE = 10;
	static final int DISTANCE_MIN_TO_OPPONENT_SHIPS_TO_FLEE = 2;
	// Quadrants:
	static final int QUADRANT_X_0 = 5;
	static final int QUADRANT_X_1 = 12;
	static final int QUADRANT_X_2 = 18;
	static final int QUADRANT_X_3 = 22;

	static final int QUADRANT_Y_0 = 5;
	static final int QUADRANT_Y_1 = 11;
	static final int QUADRANT_Y_2 = 16;
	static final int QUADRANT_Y_3 = 20;
}

class Quadrant {
	private static final int[][][] rotation0 = new int[][][]{
			{{0, 0}, {1, 0}, {2, 0}, {3, 0}},
			{{0, 1}, {1, 1}, {2, 1}, {3, 1}},
			{{0, 2}, {1, 2}, {2, 2}, {3, 2}},
			{{0, 3}, {1, 3}, {2, 3}, {3, 3}}
	};
	private static final int[][][] rotation1 = new int[][][]{
			{{3, 0}, {3, 1}, {3, 2}, {3, 3}},
			{{2, 0}, {2, 1}, {2, 2}, {2, 3}},
			{{1, 0}, {1, 1}, {1, 2}, {1, 3}},
			{{0, 0}, {0, 1}, {0, 2}, {0, 3}}
	};
	private static final int[][][] rotation2 = new int[][][]{
			{{3, 3}, {2, 3}, {1, 3}, {0, 3}},
			{{3, 2}, {2, 2}, {1, 2}, {0, 2}},
			{{3, 1}, {2, 1}, {1, 1}, {0, 1}},
			{{3, 0}, {2, 0}, {1, 0}, {0, 0}}
	};
	private static final int[][][] rotation3 = new int[][][]{
			{{0, 3}, {0, 2}, {0, 1}, {0, 0}},
			{{1, 3}, {1, 2}, {1, 1}, {1, 0}},
			{{2, 3}, {2, 2}, {2, 1}, {2, 0}},
			{{3, 3}, {3, 2}, {3, 1}, {3, 0}}
	};
	int minX, maxX, minY, maxY;

	void setX(int minX, int maxX) { this.minX = minX; this.maxX = maxX; }

	void setY(int minY, int maxY) { this.minY = minY; this.maxY = maxY; }

	void setQuadrant(int xQuadrant, int yQuadrant) {
		if (xQuadrant == 0) {
			minX = 0; maxX = Cts.QUADRANT_X_0;
		} else if (xQuadrant == 1) {
			minX = Cts.QUADRANT_X_0 + 1; maxX = Cts.QUADRANT_X_1;
		} else if (xQuadrant == 2) {
			minX = Cts.QUADRANT_X_1 + 1; maxX = Cts.QUADRANT_X_2;
		} else if (xQuadrant == 3) {
			minX = Cts.QUADRANT_X_2 + 1; maxX = Cts.QUADRANT_X_3;
		}

		if (yQuadrant == 0) {
			minY = 0; maxY = Cts.QUADRANT_Y_0;
		} else if (yQuadrant == 1) {
			minY = Cts.QUADRANT_Y_0 + 1; maxY = Cts.QUADRANT_Y_1;
		} else if (yQuadrant == 2) {
			minY = Cts.QUADRANT_Y_1 + 1; maxY = Cts.QUADRANT_Y_2;
		} else if (yQuadrant == 3) {
			minY = Cts.QUADRANT_Y_2 + 1; maxY = Cts.QUADRANT_Y_3;
		}
	}

	boolean equals(int[][] quadrants, int rotation) {
		for (int[] quadrant : quadrants) { if (this.equals(quadrant[0], quadrant[1], rotation)) { return true; } }
		return false;
	}

	boolean equals(Quadrant otherQuadrant) {
		return minX == otherQuadrant.minX
				&& maxX == otherQuadrant.maxX
				&& minY == otherQuadrant.minY
				&& maxY == otherQuadrant.maxY;
	}

	boolean equals(int xQuadrant, int yQuadrant, int rotation) {
		int x, y;
		if (rotation == 0) {
			x = rotation0[xQuadrant][yQuadrant][0]; y = rotation0[xQuadrant][yQuadrant][1];
		} else if (rotation == 1) {
			x = rotation1[xQuadrant][yQuadrant][0]; y = rotation1[xQuadrant][yQuadrant][1];
		} else if (rotation == 2) {
			x = rotation2[xQuadrant][yQuadrant][0]; y = rotation2[xQuadrant][yQuadrant][1];
		} else {
			x = rotation3[xQuadrant][yQuadrant][0]; y = rotation3[xQuadrant][yQuadrant][1];
		}
		if (x == 0 && maxX > Cts.QUADRANT_X_0) {
			return false;
		} else if (x == 1 && (minX <= Cts.QUADRANT_X_0 || maxX > Cts.QUADRANT_X_1)) {
			return false;
		} else if (x == 2 && (minX <= Cts.QUADRANT_X_1 || maxX > Cts.QUADRANT_X_2)) {
			return false;
		} else if (x == 3 && minX <= Cts.QUADRANT_X_2) {
			return false;
		}
		if (y == 0 && maxY > Cts.QUADRANT_Y_0) {
			return false;
		} else if (y == 1 && (minY <= Cts.QUADRANT_Y_0 || maxY > Cts.QUADRANT_Y_1)) {
			return false;
		} else if (y == 2 && (minY <= Cts.QUADRANT_Y_1 || maxY > Cts.QUADRANT_Y_2)) {
			return false;
		} else if (y == 3 && minY <= Cts.QUADRANT_Y_2) {
			return false;
		}
		return true;
	}
}

class Coord {
	private final static int[][] DIRECTIONS_EVEN = new int[][]{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
	private final static int[][] DIRECTIONS_ODD = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}};
	final int x, y;

	Coord(int x, int y) { this.x = x; this.y = y; }

	Coord(Coord other) { this(other.x, other.y); }

	private CubeCoord toCubeCoord() { return new CubeCoord(this); }

	double getAngleToCenter() { return getTargetAngle(new Coord(Cts.MAP_WIDTH / 2, Cts.MAP_HEIGHT / 2)); }

	double getTargetAngle(Coord targetPosition) {
		double dy = (targetPosition.y - this.y) * Math.sqrt(3) / 2;
		double dx = (targetPosition.x - this.x) + ((this.y - targetPosition.y) & 1) * 0.5;
		double angle = -Math.atan2(dy, dx) * 3 / Math.PI;
		if (angle < 0) {
			angle += 6;
		} else if (angle >= 6) {
			angle -= 6;
		}
		return angle;
	}

	Coord neighbor(int orientation, int distance) {
		Coord neighbor = this;
		for (int i = 0; i < distance; i++) { neighbor = neighbor.neighbor(orientation); }
		return neighbor;
	}

	Coord neighbor(int orientation) {
		if (this.y % 2 == 1) { return new Coord(this.x + DIRECTIONS_ODD[orientation][0], this.y + DIRECTIONS_ODD[orientation][1]); } else {
			return new Coord(this.x + DIRECTIONS_EVEN[orientation][0], this.y + DIRECTIONS_EVEN[orientation][1]);
		}
	}

	Entity findClosest(List<? extends Entity> entities) {
		Entity closest = null;
		int bestResult = Integer.MAX_VALUE;
		for (Entity entity : entities) {
			int result = entity.getPosition().distanceTo(this);
			if (bestResult > result) {
				closest = entity;
				bestResult = result;
			}
		}
		return closest;
	}

	Quadrant getQuadrant() {
		Quadrant result = new Quadrant();
		if (x < Cts.QUADRANT_X_0) {
			result.setX(0, Cts.QUADRANT_X_0);
		} else if (x < Cts.QUADRANT_X_1) {
			result.setX(Cts.QUADRANT_X_0 + 1, Cts.QUADRANT_X_1);
		} else if (x < Cts.QUADRANT_X_2) {
			result.setX(Cts.QUADRANT_X_1 + 1, Cts.QUADRANT_X_2);
		} else {
			result.setX(Cts.QUADRANT_X_2 + 1, Cts.QUADRANT_X_3);
		}
		if (y < Cts.QUADRANT_Y_0) {
			result.setY(0, Cts.QUADRANT_Y_0);
		} else if (y < Cts.QUADRANT_Y_1) {
			result.setY(Cts.QUADRANT_Y_0 + 1, Cts.QUADRANT_Y_1);
		} else if (y < Cts.QUADRANT_Y_2) {
			result.setY(Cts.QUADRANT_Y_1 + 1, Cts.QUADRANT_Y_2);
		} else {
			result.setY(Cts.QUADRANT_Y_2 + 1, Cts.QUADRANT_Y_3);
		}
		return result;
	}

	boolean isInsideMap() { return x >= 0 && x < Cts.MAP_WIDTH && y >= 0 && y < Cts.MAP_HEIGHT; }

	int distanceTo(Coord dst) { return dst == null ? Integer.MAX_VALUE : this.toCubeCoord().distanceTo(dst.toCubeCoord()); }

	boolean intersectWithEntity(Entity entity) { return this.equals(entity.getPosition()); }

	boolean intersectWithEntities(List<? extends Entity> entities) {
		for (Entity entity : entities) { if (intersectWithEntity(entity)) { return true; } }
		return false;
	}

	boolean intersectWithCoord(Coord coord) { return this.equals(coord); }

	boolean intersectWithCoords(List<Coord> coords) {
		for (Coord coord : coords) { if (intersectWithCoord(coord)) { return true; } }
		return false;
	}

	boolean intersectWithShip(Ship ship) { return this.equals(ship.getPosition()) || this.equals(ship.getBow()) || this.equals(ship.getStern()); }

	boolean intersectWithShips(List<Ship> ships) {
		for (Ship ship : ships) { if (intersectWithShip(ship)) { return true; } }
		return false;
	}

	String print() { return this.x + " " + this.y; }

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) { return false; }
		Coord other = (Coord) obj;
		return y == other.y && x == other.x;
	}
}

class CubeCoord {
	private static int[][] directions = new int[][]{{1, -1, 0}, {+1, 0, -1}, {0, +1, -1}, {-1, +1, 0}, {-1, 0, +1}, {0, -1, +1}};
	private int x;
	private int y;
	private int z;

	private CubeCoord(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }

	CubeCoord(Coord other) { x = other.x - (other.y - (other.y & 1)) / 2; z = other.y; y = -(x + z); }

	Coord toOffsetCoordinate() { return new Coord((x + (z - (z & 1)) / 2), z); }

	CubeCoord add(CubeCoord other) { return new CubeCoord(x + other.x, y + other.y, z + other.z); }

	CubeCoord sub(CubeCoord other) { return new CubeCoord(x - other.x, y - other.y, z - other.z); }

	CubeCoord rotateRight() { return new CubeCoord(-z, -x, -y); }

	CubeCoord rotateLeft() { return new CubeCoord(-y, -z, -x); }

	CubeCoord neighbor(int orientation) {
		return new CubeCoord(
				this.x + directions[orientation][0],
				this.y + directions[orientation][1],
				this.z + directions[orientation][2]
		);
	}

	int distanceTo(CubeCoord dst) {
		return (Math.abs(x - dst.x) + Math.abs(y - dst.y) + Math.abs(z - dst.z)) / 2;
	}
}

abstract class Entity {
	enum Type {
		SHIP,
		BARREL,
		MINE,
		CANNONBALL
	}

	private int id;
	private Type type;
	private Coord position;

	Entity(int entityId, Type type, int x, int y) { this.setId(entityId).setType(type).setPosition(x, y); }

	Entity(int entityId, Type type, Coord position) { this.setId(entityId).setType(type).setPosition(position); }

	Entity(Entity entity) { this.setId(entity.getId()).setType(entity.getType()).setPosition(new Coord(entity.getPosition())); }

	public boolean intersectWithEntity(Entity entity) { return this.position.intersectWithEntity(entity); }

	public boolean intersectWithEntities(List<? extends Entity> entities) { return this.position.intersectWithEntities(entities); }

	public boolean intersectWithCoord(Coord coord) { return this.position.intersectWithCoord(coord); }

	public boolean intersectWithCoords(List<Coord> coords) { return this.position.intersectWithCoords(coords); }

	public boolean intersectWithShip(Ship ship) { return this.position.intersectWithShip(ship); }

	public boolean intersectWithShips(List<Ship> ships) { return this.position.intersectWithShips(ships); }

	private Entity setId(int id) { this.id = id; return this; }

	private Entity setType(Type type) { this.type = type; return this; }

	public Entity setPosition(Coord position) { this.position = new Coord(position); return this; }

	public Entity setPosition(int x, int y) { this.position = new Coord(x, y); return this; }

	int getId() { return id; }

	private Type getType() { return type; }

	Coord getPosition() { return new Coord(position); }
}

class Barrel extends Entity {
	private int health;
	private boolean aimedByMyShip = false;

	Barrel(int entityId, int x, int y, int health) { super(entityId, Type.BARREL, x, y); this.setHealth(health); }

	private Barrel setHealth(int health) { this.health = health; return this; }

	Barrel setAimedByMyShip(boolean aimedByMyShip) { this.aimedByMyShip = aimedByMyShip; return this; }

	boolean getAimedByMyShip() { return aimedByMyShip; }
}

class Mine extends Entity {
	Mine(int entityId, int x, int y) {
		super(entityId, Type.MINE, x, y);
	}

	public Mine(Mine mine) {
		super(mine);
	}
}

class Cannonball extends Entity {
	private int remainingTurns;

	Cannonball(int entityId, int x, int y, int remainingTurns) {
		super(entityId, Type.CANNONBALL, x, y);
		this.setRemainingTurns(remainingTurns);
	}

	private Cannonball setRemainingTurns(int remainingTurns) { this.remainingTurns = remainingTurns; return this; }

	int getRemainingTurns() { return this.remainingTurns; }
}

class Ship extends Entity {
	private int orientation, speed, health, owner, mineCooldown, cannonCooldown;
	private Coord bowCoordinate, sternCoordinate;
	Coord previousPosition, previousBowCoordinate, previousSternCoordinate;

	Ship(int entityId, int x, int y, int orientation, int speed, int health, int owner) {
		super(entityId, Type.SHIP, x, y);
		this.setOrientation(orientation).setSpeed(speed).setHealth(health).setOwner(owner).setMineCooldown(0).setCannonCooldown(0);
	}

	private Ship(Ship ship) {
		super(ship.getId(), Type.SHIP, ship.getPosition()); this.setOrientation(ship.getOrientation())
				.setSpeed(ship.getSpeed())
				.setHealth(ship.getHealth())
				.setOwner(ship.owner)
				.setMineCooldown(ship.getMineCooldown())
				.setCannonCooldown(ship.getCannonCooldown()); previousPosition = new Coord(ship.previousPosition); previousBowCoordinate = new Coord(
				ship.previousBowCoordinate); previousSternCoordinate = new Coord(ship.previousSternCoordinate);
	}

	Ship(int shipId, Coord position, int orientation) {
		super(shipId, Type.SHIP, position);
		this.setOrientation(orientation);
	} // ship for collisions

	public Ship copy() { return new Ship(this); }

	void evaluateNextPosition() {
		previousPosition = getPosition();
		previousBowCoordinate = getBow();
		previousSternCoordinate = getStern();
		Coord newPosition = getNeighborSameOrientation(getSpeed());
		if (!CollisionsUtils.shipCollidesWithWall(this)) { setPosition(newPosition); }
	}

	public void evaluateNextPosition(Coord position, int orientation, int speed) {
		previousPosition = getPosition();
		previousBowCoordinate = getBow();
		previousSternCoordinate = getStern();
		setPosition(position);
		setOrientation(orientation);
		setSpeed(speed);
	}

	public void resetMineCooldown() { mineCooldown = Cts.COOLDOWN_MINE; }

	void resetCannonCooldown() { cannonCooldown = Cts.COOLDOWN_CANNON; }

	int getSameOrientation() { return orientation; }

	int getOppositeOrientation() { return (orientation + 3) % 6; }

	int getLeftOrientation() { return (orientation + 1) % 6; }

	int getRightOrientation() { return (orientation + 5) % 6; }

	int getLeftLeftOrientation() { return (orientation + 2) % 6; }

	int getRightRightOrientation() { return (orientation + 4) % 6; }

	Coord getNeighborSameOrientation(int distance) { return getPosition().neighbor(getSameOrientation(), distance); }

	Coord getNeighborOppositeOrientation(int distance) { return getPosition().neighbor(getOppositeOrientation(), distance); }

	Coord getNeighborLeftOrientation(int distance) { return getPosition().neighbor(getLeftOrientation(), distance); }

	Coord getNeighborRightOrientation(int distance) { return getPosition().neighbor(getRightOrientation(), distance); }

	Coord getNeighborLeftLeftOrientation(int distance) { return getPosition().neighbor(getLeftLeftOrientation(), distance); }

	Coord getNeighborRightRightOrientation(int distance) { return getPosition().neighbor(getRightRightOrientation(), distance); }

	public boolean intersectWithEntity(Entity entity) {
		return getPosition().intersectWithEntity(entity)
				|| this.bowCoordinate.intersectWithEntity(entity)
				|| this.sternCoordinate.intersectWithEntity(entity);
	}

	public boolean intersectWithEntities(List<? extends Entity> entities) {
		return getPosition().intersectWithEntities(entities)
				|| this.bowCoordinate.intersectWithEntities(entities)
				|| this.sternCoordinate.intersectWithEntities(entities);
	}

	public boolean intersectWithCoord(Coord coord) {
		return getPosition().intersectWithCoord(coord)
				|| this.bowCoordinate.intersectWithCoord(coord)
				|| this.sternCoordinate.intersectWithCoord(coord);
	}

	public boolean intersectWithCoords(List<Coord> coords) {
		return getPosition().intersectWithCoords(coords)
				|| this.bowCoordinate.intersectWithCoords(coords)
				|| this.sternCoordinate.intersectWithCoords(coords);
	}

	public boolean intersectWithShip(Ship ship) {
		return getPosition().intersectWithShip(ship)
				|| this.bowCoordinate.intersectWithShip(ship)
				|| this.sternCoordinate.intersectWithShip(ship);
	}

	public boolean intersectWithShips(List<Ship> ships) {
		return getPosition().intersectWithShips(ships)
				|| this.bowCoordinate.intersectWithShips(ships)
				|| this.sternCoordinate.intersectWithShips(ships);
	}

	private Coord bow() {
		return getPosition().neighbor(getSameOrientation(), 1);
	}

	private Coord stern() {
		return getPosition().neighbor(getOppositeOrientation(), 1);
	}

	public Ship setPosition(Coord position) { super.setPosition(position); this.bowCoordinate = bow(); this.sternCoordinate = stern(); return this; }

	public Ship setPosition(int x, int y) { super.setPosition(x, y); this.bowCoordinate = bow(); this.sternCoordinate = stern(); return this; }

	Ship setOrientation(int orientation) {
		this.orientation = orientation; this.bowCoordinate = bow(); this.sternCoordinate = stern(); return
				this;
	}

	Ship setSpeed(int speed) { this.speed = speed; return this; }

	private Ship setHealth(int health) { this.health = health; return this; }

	private Ship setOwner(int owner) { this.owner = owner; return this; }

	Ship decrementMineCooldown() { this.mineCooldown = Cts.clamp(this.mineCooldown - 1, 0, Cts.COOLDOWN_MINE); return this; }

	Ship decrementCannonCooldown() { this.cannonCooldown = Cts.clamp(this.cannonCooldown - 1, 0, Cts.COOLDOWN_CANNON); return this; }

	Ship setMineCooldown(int mineCooldown) { this.mineCooldown = mineCooldown; return this; }

	Ship setCannonCooldown(int cannonCooldown) { this.cannonCooldown = cannonCooldown; return this; }

	int getOrientation() { return orientation; }

	int getSpeed() { return speed; }

	int getHealth() { return health; }

	int getOwner() { return owner; }

	int getMineCooldown() { return mineCooldown; }

	int getCannonCooldown() { return cannonCooldown; }

	Coord getBow() { return new Coord(bowCoordinate); }

	Coord getStern() { return new Coord(sternCoordinate); }

	int firingTravelTime(Coord coord) { return (int) (1 + Math.round(this.getBow().distanceTo(coord) / 3.0)); }

	int firingTravelTime(int distanceToTarget) { return (int) (1 + Math.round(distanceToTarget / 3.0)); }

	double getAngleToCenter() { return getPosition().getAngleToCenter(); }

	double getTargetAngle(Coord target) { return getPosition().getTargetAngle(target); }

	double getAngleStraight(double angle) { return Math.min(Math.abs(getSameOrientation() - angle), 6 - Math.abs(getOrientation() - angle)); }

	private double getAngleBack(double angle) {
		return Math.min(
				Math.abs(getOppositeOrientation() - angle),
				6 - Math.abs(getOppositeOrientation() - angle)
		);
	}

	double getAnglePort(double angle) { return Math.min(Math.abs(getOrientation() + 1 - angle), Math.abs(getOrientation() - 5 - angle)); }

	double getAngleStarboard(double angle) {
		return Math.min(Math.abs(getOrientation() + 5 - angle), Math.abs(getOrientation() - 1 - angle));
	}

	boolean sameOrientation(Ship ship) {
		if (getOrientation() == ship.getSameOrientation()) {
			System.err.println("sameOrientation"); return true;
		} else { return false; }
	}

	boolean oppositeOrientation(Ship ship) {
		if (getOrientation() == ship.getOppositeOrientation()) {
			System.err.println("oppositeOrientation"); return true;
		} else { return false; }
	}

	boolean leftOrientation(Ship ship) {
		if (getOrientation() == ship.getRightOrientation()) {
			System.err.println("leftOrientation"); return true;
		} else { return false; }
	} // ship is going more left than me

	boolean rightOrientation(Ship ship) {
		if (getOrientation() == ship.getLeftOrientation()) {
			System.err.println("rightOrientation"); return true;
		} else { return false; }
	} // ship is going more right than me

	boolean leftLeftOrientation(Ship ship) {
		if (getOrientation() == ship.getRightRightOrientation()) {
			System.err.println("leftLeftOrientation"); return true;
		} else { return false; }
	} // ship is going even more left than me

	boolean rightRightOrientation(Ship ship) {
		if (getOrientation() == ship.getLeftLeftOrientation()) {
			System.err.println("rightRightOrientation"); return true;
		} else { return false; }
	} // ship is going even more right than me

	boolean inFrontOf(double targetAngle) { return inFrontOf(getAngleStraight(targetAngle), getAngleBack(targetAngle)); }

	boolean rightSideOf(double targetAngle) { return rightSideOf(getAnglePort(targetAngle), getAngleStarboard(targetAngle)); }

	boolean leftSideOf(double targetAngle) { return leftSideOf(getAnglePort(targetAngle), getAngleStarboard(targetAngle)); }

	private boolean inFrontOf(double angleStraight, double angleBack) {
		if (angleStraight < angleBack) {
			System.err.println("inFrontOf"); return true;
		} else { return false; }
	}

	private boolean rightSideOf(double anglePort, double angleStarboard) {
		if (angleStarboard < anglePort) {
			System.err.println("rightSideOf"); return true;
		} else { return false; }
	}

	private boolean leftSideOf(double anglePort, double angleStarboard) {
		if (anglePort < angleStarboard) {
			System.err.println("leftSideOf"); return true;
		} else { return false; }
	}
}
