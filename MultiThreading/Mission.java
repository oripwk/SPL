/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.util.ArrayList;
import java.util.Iterator;

public class Mission 
{
	private final String 		  name;
	private final ArrayList<Item> items;
	private Sergeant 			  assignedSgt;
	private final String 		  reqSkill;
	private int 				  timeLeft;
	private final int 			  initTime;
	private ArrayList<Mission>	  preMissions;
	private boolean 			  inMissionHolder;	 /* True if the mission is
														in the MH, false otherwise */
	private int 				  assignmentCounter; /* Starts as initTime and
														decreases `workHours` hours
													   	for every assignment of the
													   	mission to the assigned Sgt. */
	
	/**
	 * Constructs a mission object with no Sgt. assigned
	 * and empty pre-missions list
	 * @param name_ The name of the mission.
	 * @param items_ A list of items required for the mission, sorted
	 * by amounts.
	 * @param reqSkill_ A single skill required for the mission.
	 * @param time_ Hours needed to complete the mission.
	 */
	public Mission(String name_,
				   final ArrayList<Item> items_,
				   String reqSkill_,
				   int time_)
	{
		this.name = name_;
		this.items = items_;
		this.assignedSgt = null;
		this.reqSkill = reqSkill_;
		this.timeLeft = time_;
		this.initTime = time_;
		this.preMissions = new ArrayList<Mission>(0);
		this.inMissionHolder = false;
		this.assignmentCounter = this.initTime;
	}
	
	/**
	 * Adds missions `m` to this mission's prerequisites list.
	 * @param m The mission we want to add to the prerequisites list.
	 */
	public void addPre(Mission m)
	{
		this.preMissions.add(m);
	}
	
	/**
	 * @return true if the mission's prerequisites list is not empty.
	 */
	public boolean hasPrerequisites()
	{
		return !this.preMissions.isEmpty();
	}
	
	/**
	 * Subtracts the time from the mission's 
	 * remaining time until completion.
	 * @param t Time in hours.
	 */
	public void updateTime(int t)
	{
		this.timeLeft -= t;
	}
	
	/**
	 * Deletes this mission from the pre-missions list of `mission_`.
	 * @param mission_ The mission whose pre-missions list we want this
	 * object to be deleted from.
	 */
	public void deleteFromPreMissions(final Mission mission_)
	{	
		Iterator<Mission> it;
		for (it = mission_.preMissions.iterator() ; it.hasNext() ; )
			if (it.next() == this)
				it.remove();

	}
	
	/**
	 * Getter for the remaining time until completion.
	 * @return The remaining time until completion.
	 */
	public int timeLeft()
	{
		return this.timeLeft;
	}
	
	/**
	 * Getter for the pre-required missions list.
	 * @return The pre-required missions list.
	 */
	public ArrayList<Mission> getPreMissions()
	{
		if (this.preMissions.isEmpty())
			return null;
		return this.preMissions;
	}
	
	/**
	 * Getter for the list of required items.
	 * @return The list of items of this mission.
	 */
	public ArrayList<Item> getItems()
	{
		return this.items;
	}
	
	/**
	 * Getter for the sergeant that is assigned to this mission.
	 * @return The Sgt's name.
	 */
	public Sergeant getSgt()
	{
		return this.assignedSgt;
	}
	
	/**
	 * Getter for mission name.
	 * @return The name of this mission.
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Sets `sgt_` to be the sergeant that is assigned to this mission.
	 * @param sgt_ The desired name.
	 */
	public void setSgt(Sergeant sgt_)
	{
		this.assignedSgt = sgt_;
	}
	
	/**
	 * Takes a mission and returns it's status.
	 * @return The status of this mission: FRESH/IN_PROGRESS/DONE.
	 */
	public Status getStatus()
	{
		if (this.initTime == this.timeLeft)
			return Status.FRESH;
		else if (this.timeLeft <= 0)
			return Status.DONE;
		else
			return Status.IN_PROGRESS;
	}
	
	/**
	 * 
	 * @return The amount of work hours this mission initially needed.
	 */
	public int getInitTime()
	{
		return this.initTime;
	}

	/**
	 * 
	 * @return Number of different kinds of items this mission requires.
	 */
	public int numOfItems()
	{
		return this.items.size();
	}
	
	/**
	 * 
	 * @return The required skill to execute the mission.
	 */
	public String getSkill()
	{
		return this.reqSkill;
	}
	
	/**
	 * Mark this mission as a mission which is in the Mission Holder.
	 */
	public void setInMissionHolder()
	{
		this.inMissionHolder = true;
	}
	
	/**
	 * Mark this mission as a mission which is in the Mission Holder.
	 */
	public void unsetInMissionHolder()
	{
		this.inMissionHolder = false;
	}

	/**
	 * 
	 * @return True of the mission is in the Mission holder,
	 * and false otherwise.
	 */
	public boolean isInMissionHolder() 
	{
		return this.inMissionHolder;
	}

	/**
	 * Decrease the assignments counter
	 * @param time Time in hours to decrease the counter. 
	 */
	public void decCounter(int time)
	{
		this.assignmentCounter -= time;
		
	}
	
	/**
	 * 
	 * @return True if this mission hasn't been handed to
	 * the assigned sergeant too many times (i.e. a number of
	 * times which is over the number of times that is required
	 * to work in the mission in order to complete it).
	 */
	public boolean canBeHanded()
	{
		return this.assignmentCounter > 0;
	}
}
