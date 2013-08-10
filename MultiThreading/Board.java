/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.util.ArrayList;

public class Board
{
	private ArrayList<Mission>	missions;
	private ChiefOfStaff 		cof;
	
	/**
	 * Constructs a new board with empty missions list.
	 */
	public Board()
	{
		this.missions = new ArrayList<Mission>(0);
	}
		
	/**
	 * Gets a mission name and returns the corresponding Mission object.
	 * If there is no such mission it returns null.
	 * @param missionName The mission name whose corresponding Mission 
	 *   object will be returned.
	 * @return The mission object that corresponds missionName. 
	 */
	public Mission findMission(String missionName)
	{
		for (Mission m : this.missions)
			if (m.getName().equals(missionName))
				return m;
		
		return null;
	}
	
	/**
	 * Takes a Mission object and adds it to the board.
	 * @param mission The mission object to be added to the board.
	 */
	public void addMission(Mission mission)
	{
		if (mission == null) {
			System.err.println("WARNING: Tried to add a null mission.");
			return;
		}
		this.missions.add(mission);
		this.cof.wakeUp();
	}

	/**
	 * 
	 * @return A list of missions that have no prerequisites, haven't been
	 * assigned to any sergeant yet, and are not in the Mission Holder already.
	 */
	public ArrayList<Mission> exportMissions()
	{	
		ArrayList<Mission> suitables = new ArrayList<Mission>(0);
		
		for (Mission m : this.missions) {
			if ((!m.hasPrerequisites())
			        && (!m.isInMissionHolder())
			        && (m.getStatus() == Status.FRESH)) {
				suitables.add(m);
			}
		}
		
		if (suitables.isEmpty())
			return null;
		
		return suitables;
	}
	
	/**
	 * Takes a mission and time (in hours) and subtracts the time 
	 * from the mission's remaining time until completion.
	 * @param mission The mission whose time we want to update.
	 * @param time The time that has been achieved in hours.
	 */
	public synchronized void updateMissionTime(Mission mission, int time)
	{
		if (mission == null) {
			System.err.println("ERROR: Trying to modify a null mission.");
			return;
		}
		mission.updateTime(time);
		
		if (mission.getStatus() == Status.DONE) {
			Run.logger.fine("Sgt. " + mission.getSgt().getName()
							    + ": Mission " + mission.getName() + " DONE.");
			deleteFromDependencies(mission);
			this.cof.deleteFromMissionHolder(mission);
		}
		if (areAllComplete()) {
			this.cof.cancel();
			System.out.println("All objectives have been achieved.\n" 
							       + "Enter 'stop' for terminating execution.\n");
			Run.logger.fine("All objectives have been achieved.");
			return;
		}
		this.notifyAll();
		this.cof.wakeUp();
	}
	
	/* Deletes `mission` from all the missions that depend on it */
	private void deleteFromDependencies(final Mission mission)
	{
		/* Run through the mission list, and for every mission,
		   let `mission` delete itself from that mission's pre-
		   missions list (if it's there) */
		for (Mission m : this.missions)
			mission.deleteFromPreMissions(m);
	}
	
	/**
	 * Prints a list of complete missions and their assigned Sgt.
	 */
	public void printCompleteMissions()
	{
		int cnt = 0;
		if (this.missions == null) {
			System.out.println("Missions list is empty.\n");
			return;
		}
		System.out.println("--COMPLETE MISSIONS--\n");
		for (Mission m : this.missions) {
			if (m.getStatus() == Status.DONE) {
				System.out.println("Mission:\t" + m.getName()
								       + "\n" + "Completed by Sgt.\t"
						               + m.getSgt().getName() + "\n");
				++cnt;
			}
		}
		if (cnt > 0)
			System.out.printf("Total: %d\n", cnt);
		else
			System.out.println("\tNONE.\n");
	}
	
	/**
	 * Prints a list of incomplete missions, their remaining
	 * times for completion, and pre-required missions that
	 * haven't been completed yet (If there are any).
	 */
	public void printIncompleteMissions()
	{
		int cnt = 0;
		if (this.missions == null) {
			System.out.println("Missions list is empty.\n");
			return;
		}
		System.out.println("--INCOMPLETE MISSIONS--\n");
		for (Mission m : this.missions) {
			if (m.getStatus() != Status.DONE) {
				System.out.println("Mission:\t" + m.getName()
								       + "\n" + "Time Left:\t" + m.timeLeft()
								       + "\n" + "Prerequisites:");
				if (m.getPreMissions() == null)
					System.out.println("\tNONE");
				else
					for (Mission n : m.getPreMissions())
						System.out.println("\t" + n.getName());
				++cnt;
			}
			System.out.println();
		}
		if (cnt > 0)
			System.out.printf("Total: %d\n", cnt);
		else
			System.out.println("\tNONE.\n");
	}


	/**
	 * 
	 * @return true if all missions are complete and false otherwise.
	 */
	private boolean areAllComplete()
	{
		for (Mission m : this.missions)
			if (m.getStatus() != Status.DONE)
				return false;
		
		return true;
	}
	
	/**
	 * Sets the `cof` field to point to the Chief of Staff object.
	 * @param cof_ A Chief of Staff object.
	 */
	public void addChief(ChiefOfStaff cof_)
	{
		this.cof = cof_;
	}
	
	/**
	 * Gets the missions that are assigned to Sergeant `sgt`.
	 * @param sgt The sergeant whose assigned missions we want to return.
	 * @return Sergeant `sgt` assigned missions. If there are none, it returns null.
	 */
	public ArrayList<Mission> getSgtsMissions(Sergeant sgt)
	{
		ArrayList<Mission> sgtsMissions = new ArrayList<Mission>(0);
		
		for (Mission m : this.missions)
			if ((m.getSgt() != null) && (m.getSgt() == sgt)) 
				sgtsMissions.add(m);
		
		if (sgtsMissions.isEmpty())
			return null;
		
		return sgtsMissions;
	}
}
