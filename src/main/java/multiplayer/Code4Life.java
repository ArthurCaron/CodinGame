package multiplayer;

import java.util.*;

// Please note that this type of code is written quickly and without much regard for proper architecture or code conventions :)
class Code4Life {
	private static Code4LifeGameState gs = new Code4LifeGameState();
	private static Code4LifeGameState nextGs = null;

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);

		int projectCount = in.nextInt();
		for (int i = 0; i < projectCount; i++) {
			gs.addProject(new Project(Utils.intToMap(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt())));
		}

		while (true) {
			gs.myRobot.update(
					in.next(),
					in.nextInt(),
					in.nextInt(),
					Utils.intToMap(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()),
					Utils.intToMap(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt())
			);
			gs.opponentRobot.update(
					in.next(),
					in.nextInt(),
					in.nextInt(),
					Utils.intToMap(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()),
					Utils.intToMap(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt())
			);
			gs.molecules = new Molecules(Utils.intToMap(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()));

			int sampleCount = in.nextInt();
			gs.diagnosis.availableSamples = new ArrayList<>();
			gs.diagnosis.opponentSamples = new ArrayList<>();
			for (int i = 0; i < sampleCount; i++) {
				gs.diagnosis.addSample(
						gs,
						new Sample(
								in.nextInt(),
								in.nextInt(),
								in.nextInt(),
								in.next(),
								in.nextInt(),
								Utils.intToMap(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt())
						)
				);
			}

			System.err.println("Previous guess: ");
			if (nextGs != null) {
				//				nextGs.print();
			}
			System.err.println("Current: ");
			gs.print();

			if (gs.myRobot.eta > 0) {
				new Wait().print();
				nextGs = gs.computeAction(new Wait());
			} else if (!Mod.SAMPLES.equals(gs.myRobot.target) && gs.diagnosis.mySamples.size() == 0) {
				new GoTo(Mod.SAMPLES).print();
				nextGs = gs.computeAction(new GoTo(Mod.SAMPLES));
			} else {
				if (Mod.SAMPLES.equals(gs.myRobot.target)) {
					if (gs.diagnosis.mySamples.size() < 3) {
						if (Utils.countTotal(gs.myRobot.expertise) + gs.diagnosis.mySamples.size() >= 12) {
							if (gs.diagnosis.mySamples.size() == 2 && gs.diagnosis.countByType(2, Diagnosis.MY_SAMPLES) == 0) {
								new Connect(2).print();
								nextGs = gs.computeAction(new Connect(2));
							} else {
								new Connect(3).print();
								nextGs = gs.computeAction(new Connect(3));
							}
						} else if (Utils.countTotal(gs.myRobot.expertise) + gs.diagnosis.mySamples.size() >= 5) {
							if (gs.diagnosis.mySamples.size() == 2 && gs.diagnosis.countByType(1, Diagnosis.MY_SAMPLES) == 0) {
								new Connect(1).print();
								nextGs = gs.computeAction(new Connect(1));
							} else {
								new Connect(2).print();
								nextGs = gs.computeAction(new Connect(2));
							}
						} else {
							new Connect(1).print();
							nextGs = gs.computeAction(new Connect(1));
						}
					} else {
						new GoTo(Mod.DIAGNOSIS).print();
						nextGs = gs.computeAction(new GoTo(Mod.DIAGNOSIS));
					}
				} else if (Mod.DIAGNOSIS.equals(gs.myRobot.target)) {
					Sample undiagnosedSample = gs.myRobot.getUndiagnosedSample(gs.diagnosis.mySamples);
					if (undiagnosedSample != null) {
						new Connect(undiagnosedSample).print();
						nextGs = gs.computeAction(new Connect(undiagnosedSample));
					} else {
						if (gs.myRobot.hasValidatedSample(gs.diagnosis.mySamples)) {
							new GoTo(Mod.LABORATORY).print();
							nextGs = gs.computeAction(new GoTo(Mod.LABORATORY));
						} else {
							if (!gs.myRobot.hasSampleWithEnoughMoleculesAvailable(gs.diagnosis.mySamples, gs.molecules)) {
								Sample sampleWithNotEnoughMoleculesAvailable = gs.myRobot.popSampleWithNotEnoughMoleculesAvailable(
										gs.diagnosis.mySamples,
										gs.molecules
								);

								if (sampleWithNotEnoughMoleculesAvailable != null) {
									new Connect(sampleWithNotEnoughMoleculesAvailable).print();
									nextGs = gs.computeAction(new Connect(sampleWithNotEnoughMoleculesAvailable));
								} else {
									new GoTo(Mod.MOLECULES).print();
									nextGs = gs.computeAction(new GoTo(Mod.MOLECULES));
								}
							} else {
								new GoTo(Mod.MOLECULES).print();
								nextGs = gs.computeAction(new GoTo(Mod.MOLECULES));
							}
						}
					}
				} else if (Mod.MOLECULES.equals(gs.myRobot.target)) {
					Mol molToCounter = gs.myRobot.getMoleculeToCounter(gs.opponentRobot, gs.diagnosis.opponentSamples, gs.molecules);
					if (molToCounter != null) {
						new Connect(molToCounter).print();
						nextGs = gs.computeAction(new Connect(molToCounter));
					} else {
						Mol molToTake = gs.myRobot.getMolecule(gs.diagnosis.mySamples, gs.molecules);
						if (molToTake != null) {
							new Connect(molToTake).print();
							nextGs = gs.computeAction(new Connect(molToTake));
						} else {
							if (gs.myRobot.hasValidatedSample(gs.diagnosis.mySamples)) {
								new GoTo(Mod.LABORATORY).print();
								nextGs = gs.computeAction(new GoTo(Mod.LABORATORY));
							} else {
								if (gs.diagnosis.mySamples.size() < 3) {
									new GoTo(Mod.SAMPLES).print();
									nextGs = gs.computeAction(new GoTo(Mod.SAMPLES));
								} else {
									new GoTo(Mod.DIAGNOSIS).print();
									nextGs = gs.computeAction(new GoTo(Mod.DIAGNOSIS));
								}
							}
						}
					}
				} else if (Mod.LABORATORY.equals(gs.myRobot.target)) {
					Sample validatedSample = gs.myRobot.popValidatedSample(gs.diagnosis.mySamples);
					if (validatedSample != null) {
						new Connect(validatedSample).print();
						nextGs = gs.computeAction(new Connect(validatedSample));
					} else if (gs.myRobot.hasSampleWithEnoughMoleculesAvailable(gs.diagnosis.mySamples, gs.molecules)) {
						new GoTo(Mod.MOLECULES).print();
						nextGs = gs.computeAction(new GoTo(Mod.MOLECULES));
					} else {
						new GoTo(Mod.SAMPLES).print();
						nextGs = gs.computeAction(new GoTo(Mod.SAMPLES));
					}
				}
			}
		}
	}
}


class Robot {
	String target;
	int eta;
	private int score;
	private Map<Mol, Integer> storage = new HashMap<>();
	private Map<Mol, Integer> storageReserved = new HashMap<>();
	Map<Mol, Integer> expertise = Utils.intToMap(0, 0, 0, 0, 0);
	private List<Sample> reservedSamples = new ArrayList<>();

	Robot() {
		storageReserved.put(Mol.A, 0); storageReserved.put(Mol.B, 0); storageReserved.put(Mol.C, 0); storageReserved.put(Mol.D, 0);
		storageReserved.put(Mol.E, 0);
	}

	Robot copy() {
		Robot robot = new Robot();
		robot.update(target, eta, score, Utils.copyMap(storage), Utils.copyMap(expertise));
		return robot;
	}

	void update(String target, int eta, int score, Map<Mol, Integer> storage, Map<Mol, Integer> expertise) {
		this.target = target; this.eta = eta; this.score = score; this.storage = storage; /*this.expertise = expertise;*/
	}

	void updateExpertise(Sample validatedSample) { // For eval gamestate
		if (validatedSample.expertiseGain == Mol.A) {
			expertise.put(Mol.A, expertise.get(Mol.A) + 1);
		} else if (validatedSample.expertiseGain == Mol.B) {
			expertise.put(Mol.B, expertise.get(Mol.B) + 1);
		} else if (validatedSample.expertiseGain == Mol.C) {
			expertise.put(Mol.C, expertise.get(Mol.C) + 1);
		} else if (validatedSample.expertiseGain == Mol.D) {
			expertise.put(Mol.D, expertise.get(Mol.D) + 1);
		} else if (validatedSample.expertiseGain == Mol.E) {
			expertise.put(Mol.E, expertise.get(Mol.E) + 1);
		}
	}

	Sample getUndiagnosedSample(List<Sample> samples) {
		for (Sample sample : samples) {
			if (!sample.diagnosed) { return sample; }
		}
		return null;
	}

	Mol getMoleculeToCounter(Robot opponentRobot, List<Sample> samples, Molecules molecules) {
		if (samples.size() > 0) {
			boolean canCounterWithAFor5 = true;
			boolean canCounterWithBFor5 = true;
			boolean canCounterWithCFor5 = true;
			boolean canCounterWithDFor5 = true;
			boolean canCounterWithEFor5 = true;
			for (Sample sample : samples) {
				if (opponentRobot.storage.get(Mol.A) != 3 || (sample.getCost(Mol.A) > 0 && sample.getCost(Mol.A) < 5) || molecules.get(Mol.A) < 1) {
					canCounterWithAFor5 = false;
				}
				if (opponentRobot.storage.get(Mol.B) != 3 || (sample.getCost(Mol.B) > 0 && sample.getCost(Mol.B) < 5) || molecules.get(Mol.B) < 1) {
					canCounterWithBFor5 = false;
				}
				if (opponentRobot.storage.get(Mol.C) != 3 || (sample.getCost(Mol.C) > 0 && sample.getCost(Mol.C) < 5) || molecules.get(Mol.C) < 1) {
					canCounterWithCFor5 = false;
				}
				if (opponentRobot.storage.get(Mol.D) != 3 || (sample.getCost(Mol.D) > 0 && sample.getCost(Mol.D) < 5) || molecules.get(Mol.D) < 1) {
					canCounterWithDFor5 = false;
				}
				if (opponentRobot.storage.get(Mol.E) != 3 || (sample.getCost(Mol.E) > 0 && sample.getCost(Mol.E) < 5) || molecules.get(Mol.E) < 1) {
					canCounterWithEFor5 = false;
				}
			}
			if (canCounterWithAFor5) { return Mol.A; }
			if (canCounterWithBFor5) { return Mol.B; }
			if (canCounterWithCFor5) { return Mol.C; }
			if (canCounterWithDFor5) { return Mol.D; }
			if (canCounterWithEFor5) { return Mol.E; }
		}
		return null;
	}

	Mol getMolecule(List<Sample> samples, Molecules molecules) {
		for (Sample sample : samples) {
			if (sample.rank == 3) {
				if (sample.canProceed(molecules, this)) {
					return sample.getNextMolecule(this).get(0);
				}
			}
		}
		for (Sample sample : samples) {
			if (sample.rank == 2) {
				if (sample.canProceed(molecules, this)) {
					return sample.getNextMolecule(this).get(0);
				}
			}
		}
		for (Sample sample : samples) {
			if (sample.rank == 1) {
				if (sample.canProceed(molecules, this)) {
					return sample.getNextMolecule(this).get(0);
				}
			}
		}
		return null;
	}

	boolean hasSampleWithEnoughMoleculesAvailable(List<Sample> samples, Molecules molecules) {
		for (Sample sample : samples) {
			if (sample.enoughMoleculesAvailable(molecules, this) && sample.computeTotalStillNeeded(this) <= storageLeft()) {
				return true;
			}
		}
		return false;
	}

	Sample popSampleWithNotEnoughMoleculesAvailable(List<Sample> samples, Molecules molecules) {
		Sample bestSample = null;
		int bestScore = 0;
		for (Sample sample : samples) {
			if (!(sample.enoughMoleculesAvailable(molecules, this) && sample.computeTotalStillNeeded(this) <= storageLeft())) {
				if (sample.health > bestScore) {
					bestScore = sample.health;
					bestSample = sample;
				}
			}
		}
		if (bestSample != null) {
			samples.remove(bestSample);
			return bestSample;
		}
		return null;
	}

	boolean hasValidatedSample(List<Sample> samples) {
		for (Sample sample : samples) {
			if (sample.validated) { return true; }
		}
		return false;
	}

	Sample popValidatedSample(List<Sample> samples) {
		for (Sample reservedSample : reservedSamples) {
			for (Sample sample : samples) {
				if (sample.equals(reservedSample)) {
					Sample sampleCopy = sample.copy();
					samples.remove(sample);
					reservedSamples.remove(reservedSample);
					unReserveMolecules(sampleCopy.cost);
					return sampleCopy;
				}
			}
			//			if (sample.validated) {
			//				Sample sampleCopy = sample.copy();
			//				samples.remove(sample);
			//				unReserveMolecules(sampleCopy.cost);
			//				return sampleCopy;
			//			}
		}
		return null;
	}

	void reserveMolecules(Map<Mol, Integer> cost) { storageReserved = Utils.addToMap(storageReserved, cost); }

	void reserveSample(Sample sample) {
		reservedSamples.add(sample);
	}

	private void unReserveMolecules(Map<Mol, Integer> cost) { storageReserved = Utils.removeFromMap(storageReserved, cost); }

	Map<Mol, Integer> computeMoleculesAvailable() {
		Map<Mol, Integer> molAvail = new HashMap<>();
		molAvail.put(Mol.A, moleculesAvailable(Mol.A));
		molAvail.put(Mol.B, moleculesAvailable(Mol.B));
		molAvail.put(Mol.C, moleculesAvailable(Mol.C));
		molAvail.put(Mol.D, moleculesAvailable(Mol.D));
		molAvail.put(Mol.E, moleculesAvailable(Mol.E));
		return molAvail;
	}

	int moleculesAvailable(Mol molecule) { return getStorage(molecule) - getStorageReserved(molecule) + getExpertise(molecule); }

	int storageLeft() { return 10 - Utils.countTotal(storage); }

	private int getStorage(Mol mol) { return storage.get(mol); }

	private int getStorageReserved(Mol mol) { return storageReserved.get(mol); }

	private int getExpertise(Mol mol) { return expertise.get(mol); }

	void print() {
		System.err.println("robot: target " + target + " eta " + eta + " score " + score);
		System.err.println("robot storage: A " + getStorage(Mol.A)
				+ " B " + getStorage(Mol.B)
				+ " C " + getStorage(Mol.C)
				+ " D " + getStorage(Mol.D)
				+ " E " + getStorage(Mol.E));
		System.err.println("robot storageReserved: A " + getStorageReserved(Mol.A)
				+ " B " + getStorageReserved(Mol.B)
				+ " C " + getStorageReserved(Mol.C)
				+ " D " + getStorageReserved(Mol.D)
				+ " E " + getStorageReserved(Mol.E));
		System.err.println("robot expertise: A " + getExpertise(Mol.A)
				+ " B " + getExpertise(Mol.B)
				+ " C " + getExpertise(Mol.C)
				+ " D " + getExpertise(Mol.D)
				+ " E " + getExpertise(Mol.E));
	}
}


class Sample {
	int sampleId;
	int carriedBy;
	int rank;
	Mol expertiseGain;
	int health;
	Map<Mol, Integer> cost;
	boolean diagnosed, validated;
	private boolean reserved;

	Sample(int sampleId, int carriedBy, int rank, String expertiseGain, int health, Map<Mol, Integer> cost) {
		this.sampleId = sampleId;
		this.carriedBy = carriedBy;
		this.rank = rank;
		this.expertiseGain = Mol.getByString(expertiseGain);
		this.health = health;
		this.cost = cost;
		this.diagnosed = health >= 0;
		this.validated = false;
		this.reserved = false;
	}

	Sample copy() { return new Sample(sampleId, carriedBy, rank, expertiseGain.toString(), health, Utils.copyMap(cost)); }

	void update(Sample otherSample) {
		if (!this.diagnosed && otherSample.diagnosed) {
			this.expertiseGain = otherSample.expertiseGain;
			this.health = otherSample.health;
			this.cost = Utils.copyMap(otherSample.cost);
			this.diagnosed = true;
		}
	}

	boolean enoughMoleculesAvailable(Molecules molecules, Robot robot) {
		return (getCost(Mol.A) <= (molecules.get(Mol.A) + robot.moleculesAvailable(Mol.A))
				&& getCost(Mol.B) <= (molecules.get(Mol.B) + robot.moleculesAvailable(Mol.B))
				&& getCost(Mol.C) <= (molecules.get(Mol.C) + robot.moleculesAvailable(Mol.C))
				&& getCost(Mol.D) <= (molecules.get(Mol.D) + robot.moleculesAvailable(Mol.D))
				&& getCost(Mol.E) <= (molecules.get(Mol.E) + robot.moleculesAvailable(Mol.E)));
	}

	private boolean enoughMoleculesInStorage(Robot robot) {
		return (getCost(Mol.A) <= robot.moleculesAvailable(Mol.A)
				&& getCost(Mol.B) <= robot.moleculesAvailable(Mol.B)
				&& getCost(Mol.C) <= robot.moleculesAvailable(Mol.C)
				&& getCost(Mol.D) <= robot.moleculesAvailable(Mol.D)
				&& getCost(Mol.E) <= robot.moleculesAvailable(Mol.E));
	}

	boolean canProceed(Molecules molecules, Robot robot) {
		return !validated && diagnosed && enoughMoleculesAvailable(molecules, robot) && computeTotalStillNeeded(robot) <= robot.storageLeft();
	}

	List<Mol> getNextMolecule(Robot robot) { // TODO return a list of needed molecules
		List<Mol> moleculesNeeded = new ArrayList<>();

		if (getCost(Mol.A) > robot.moleculesAvailable(Mol.A)) {
			moleculesNeeded.add(Mol.A);
		} else if (getCost(Mol.B) > robot.moleculesAvailable(Mol.B)) {
			moleculesNeeded.add(Mol.B);
		} else if (getCost(Mol.C) > robot.moleculesAvailable(Mol.C)) {
			moleculesNeeded.add(Mol.C);
		} else if (getCost(Mol.D) > robot.moleculesAvailable(Mol.D)) {
			moleculesNeeded.add(Mol.D);
		} else if (getCost(Mol.E) > robot.moleculesAvailable(Mol.E)) {
			moleculesNeeded.add(Mol.E);
		}

		return moleculesNeeded;
	}

	void evaluateValidated(Robot robot) {
		validated = validated || (diagnosed && enoughMoleculesInStorage(robot));
		if (validated && !reserved) {
			reserved = true;
			robot.reserveMolecules(cost);
			robot.reserveSample(this);
		}
	}

	int getCost(Mol mol) { return cost.get(mol); }

	private int computeTotalCost() { return Utils.countTotalWithClampOnEach(cost); }

	int computeTotalStillNeeded(Robot robot) {
		return Utils.countTotalWithClampOnEach(Utils.removeFromMap(
				Utils.copyMap(cost),
				Utils.copyMap(robot.computeMoleculesAvailable())
		));
	}

	public String toString() { return Integer.toString(sampleId); }

	void print() {
		System.err.println("id " + sampleId
				+ " by " + carriedBy
				+ " diagnosed " + diagnosed
				+ " validated " + validated
				+ " rank " + rank
				+ " expert " + expertiseGain
				+ " health " + health
				+ " getCost(Mol.A) " + getCost(Mol.A)
				+ " getCost(Mol.B) " + getCost(Mol.B)
				+ " getCost(Mol.C) " + getCost(Mol.C)
				+ " getCost(Mol.D) " + getCost(Mol.D)
				+ " getCost(Mol.E) " + getCost(Mol.E)
				+ " totalCost " + computeTotalCost());
	}
}


enum ActionType {
	GOTO,
	CONNECT,
	WAIT
}

interface Code4LifeAction {
	ActionType getType();
}

class GoTo implements Code4LifeAction {
	Mod module;

	GoTo(Mod module) { this.module = module; }

	public ActionType getType() { return ActionType.GOTO; }

	void print() { System.out.println("GOTO " + module.name); }
}

class Connect implements Code4LifeAction {
	String connect;

	Connect(int id) { connect = Integer.toString(id); }

	Connect(Sample sample) { connect = sample.toString(); }

	Connect(Mol molecule) { connect = molecule.toString(); }

	public ActionType getType() { return ActionType.CONNECT; }

	void print() { System.out.println("CONNECT " + connect); }
}

class Wait implements Code4LifeAction {
	public ActionType getType() { return ActionType.WAIT; }

	void print() { System.out.println("WAIT"); }
}


class Code4LifeGameState {
	private List<Project> projects = new ArrayList<>();
	Robot myRobot = new Robot();
	Robot opponentRobot = new Robot();
	Molecules molecules;
	Diagnosis diagnosis = new Diagnosis();

	void addProject(Project project) { projects.add(project); }

	private Code4LifeGameState copy() {
		Code4LifeGameState gs = new Code4LifeGameState();

		for (Project project : projects) {
			gs.projects.add(project.copy());
		}
		gs.myRobot = myRobot.copy();
		gs.opponentRobot = opponentRobot.copy();
		gs.molecules = molecules.copy();
		gs.diagnosis = diagnosis.copy(gs);

		return gs;
	}

	// TODO func generate list authorized actions

	// handle the actions
	// then do func generate list authorized actions
	// then handle actions with opponent and my action
	// then eval func (simple one)
	// then do thing to make sure we don't timeout
	// then we list possible actions, opponent does the best one for him then I do the best one for me
	// (still complicated since each action by each player creates a tree of possibilities. Thankfully, a lot of moments will be straightforward,
	// like when someone moves (only one action possible: WAIT)

	Code4LifeGameState computeAction(Code4LifeAction action) { // remove the return null because we expect correct actions
		Code4LifeGameState gs = this.copy();

		if (action.getType() == ActionType.WAIT) {
			if (gs.myRobot.eta > 0) { gs.myRobot.eta--; }
		} else if (action.getType() == ActionType.GOTO) {
			GoTo goToAction = (GoTo) action;
			if (gs.myRobot.eta == 0) {
				gs.myRobot.eta = 3;
				if (gs.myRobot.target.equals(goToAction.module)) {
					return null;
				} else if (gs.myRobot.target.equals(Mod.START_POS)) {
					gs.myRobot.eta = 2;
				} else if (gs.myRobot.target.equals(Mod.DIAGNOSIS) || gs.myRobot.target.equals(Mod.LABORATORY)) {
					if (goToAction.module == Mod.DIAGNOSIS || goToAction.module == Mod.LABORATORY) {
						gs.myRobot.eta = 4;
					}
				}
				gs.myRobot.target = goToAction.module.toString();
			}
		} else if (action.getType() == ActionType.CONNECT) {
			Connect connectAction = (Connect) action;
			if (gs.myRobot.eta == 0) {
				if (gs.myRobot.target.equals(Mod.START_POS)) {
					return null;
				} else if (gs.myRobot.target.equals(Mod.SAMPLES)) {
					int id = Integer.parseInt(connectAction.connect);
					if (id != 1 && id != 2 && id != 3) {
						return null;
					}
				} else if (gs.myRobot.target.equals(Mod.LABORATORY)) {
					int id = Integer.parseInt(connectAction.connect);
					if (!gs.diagnosis.isInList(gs.diagnosis.get(Diagnosis.MY_SAMPLES), id)) {
						return null;
					}
				}
			}
		}

		return gs;
	}


	void print() {
		System.err.println("Projects: ");
		for (Project project : projects) {
			project.print();
		}
		System.err.println("My robot: ");
		myRobot.print();
		System.err.println("Opponent robot: ");
		opponentRobot.print();
		System.err.println("Molecules: ");
		molecules.print();
		System.err.println("Diagnosis: ");
		diagnosis.print();
	}
}


class Diagnosis {
	static final int AVAILABLE = -1;
	static final int MY_SAMPLES = 0;
	static final int OPPONENT_SAMPLES = 1;

	List<Sample> availableSamples = new ArrayList<>();
	List<Sample> mySamples = new ArrayList<>();
	List<Sample> opponentSamples = new ArrayList<>();

	Diagnosis copy(Code4LifeGameState gs) {
		Diagnosis dia = new Diagnosis();
		for (Sample sample : get(AVAILABLE)) { dia.addSample(gs, sample.copy()); }
		for (Sample sample : get(MY_SAMPLES)) { dia.addSample(gs, sample.copy()); }
		for (Sample sample : get(OPPONENT_SAMPLES)) { dia.addSample(gs, sample.copy()); }
		return dia;
	}

	void addSample(Code4LifeGameState gs, Sample sample) {
		List<Sample> list = get(sample.carriedBy);

		boolean newSample = true;
		for (Sample mySample : list) {
			if (mySample.sampleId == sample.sampleId) {
				newSample = false;
				mySample.update(sample);
			}
		}
		if (newSample) { list.add(sample); }

		for (Sample mySample : list) {
			if (sample.carriedBy == MY_SAMPLES) {
				boolean previouslyValidated = mySample.validated;
				mySample.evaluateValidated(gs.myRobot);
				if (!previouslyValidated && mySample.validated) {
					gs.myRobot.updateExpertise(mySample);
				}
			} else if (sample.carriedBy == OPPONENT_SAMPLES) {
				boolean previouslyValidated = mySample.validated;
				mySample.evaluateValidated(gs.opponentRobot);
				if (!previouslyValidated && mySample.validated) {
					gs.opponentRobot.updateExpertise(mySample);
				}
			}
		}
	}

	int countByType(int rank, int carriedBy) {
		int count = 0;
		for (Sample sample : get(carriedBy)) {
			if (sample.rank == rank) {
				count++;
			}
		}
		return count;
	}

	List<Sample> get(int carrier) {
		if (carrier == 0) { return mySamples; } else if (carrier == 1) { return opponentSamples; } else { return availableSamples; }
	}

	boolean isInList(List<Sample> list, int id) {
		for (Sample sample : list) {
			if (sample.sampleId == id) {
				return true;
			}
		}
		return false;
	}

	void print() {
		//		System.err.println("available samples: ");
		//		for (Sample sample : get(AVAILABLE)) { sample.print(); }
		System.err.println("my samples: ");
		for (Sample sample : get(MY_SAMPLES)) { sample.print(); }
		System.err.println("opponent samples: ");
		for (Sample sample : get(OPPONENT_SAMPLES)) { sample.print(); }
	}
}


class Project {
	private Map<Mol, Integer> values;

	Project(Map<Mol, Integer> values) { this.values = values; }

	Project copy() { return new Project(Utils.copyMap(values)); }

	private int get(Mol mol) { return values.get(mol); }

	void print() {
		System.err.println("project: A " + get(Mol.A)
				+ " B " + get(Mol.B)
				+ " C " + get(Mol.C)
				+ " D " + get(Mol.D)
				+ " E " + get(Mol.E));
	}
}


class Molecules {
	private Map<Mol, Integer> available;

	Molecules(Map<Mol, Integer> available) { this.available = available; }

	Molecules copy() { return new Molecules(Utils.copyMap(available)); }

	int get(Mol mol) { return available.get(mol); }

	void print() {
		System.err.println("project: A " + get(Mol.A)
				+ " B " + get(Mol.B)
				+ " C " + get(Mol.C)
				+ " D " + get(Mol.D)
				+ " E " + get(Mol.E));
	}
}


enum Mol {
	UNDEFINED,
	A,
	B,
	C,
	D,
	E;

	public static Mol getByString(String type) {
		if (type.equals("0")) { return Mol.UNDEFINED; }
		return Mol.valueOf(type);
	}
}


enum Mod {
	SAMPLES("SAMPLES"),
	DIAGNOSIS("DIAGNOSIS"),
	MOLECULES("MOLECULES"),
	LABORATORY("LABORATORY"),
	START_POS("START_POS");
	String name;

	Mod(String name) { this.name = name; }

	public boolean equals(String module) { return this.name.equals(module); }
}


class Utils {
	private static int clamp(int value) { return Math.min(Math.max(value, 0), 100); }

	static Map<Mol, Integer> intToMap(int a, int b, int c, int d, int e) {
		Map<Mol, Integer> values = new HashMap<>();
		values.put(Mol.A, a); values.put(Mol.B, b); values.put(Mol.C, c); values.put(Mol.D, d); values.put(Mol.E, e);
		return values;
	}

	static Map<Mol, Integer> copyMap(Map<Mol, Integer> values) {
		Map<Mol, Integer> copy = new HashMap<>();
		copy.put(Mol.A, values.get(Mol.A)); copy.put(Mol.B, values.get(Mol.B)); copy.put(Mol.C, values.get(Mol.C));
		copy.put(Mol.D, values.get(Mol.D)); copy.put(Mol.E, values.get(Mol.E));
		return copy;
	}

	static Map<Mol, Integer> addToMap(Map<Mol, Integer> map1, Map<Mol, Integer> map2) {
		map1.put(Mol.A, map1.get(Mol.A) + map2.get(Mol.A));
		map1.put(Mol.B, map1.get(Mol.B) + map2.get(Mol.B));
		map1.put(Mol.C, map1.get(Mol.C) + map2.get(Mol.C));
		map1.put(Mol.D, map1.get(Mol.D) + map2.get(Mol.D));
		map1.put(Mol.E, map1.get(Mol.E) + map2.get(Mol.E));
		return map1;
	}

	static Map<Mol, Integer> removeFromMap(Map<Mol, Integer> map1, Map<Mol, Integer> map2) {
		map1.put(Mol.A, map1.get(Mol.A) - map2.get(Mol.A));
		map1.put(Mol.B, map1.get(Mol.B) - map2.get(Mol.B));
		map1.put(Mol.C, map1.get(Mol.C) - map2.get(Mol.C));
		map1.put(Mol.D, map1.get(Mol.D) - map2.get(Mol.D));
		map1.put(Mol.E, map1.get(Mol.E) - map2.get(Mol.E));
		return map1;
	}

	static int countTotal(Map<Mol, Integer> values) {
		return values.get(Mol.A) + values.get(Mol.B) + values.get(Mol.C) + values.get(Mol.D) + values.get(Mol.E);
	}

	static int countTotalWithClampOnEach(Map<Mol, Integer> values) {
		return clamp(values.get(Mol.A)) + clamp(values.get(Mol.B)) + clamp(values.get(Mol.C)) + clamp(values.get(Mol.D)) + clamp(values.get(Mol.E));
	}
}
