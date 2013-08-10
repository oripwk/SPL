/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.util.concurrent.*;
import java.util.ArrayList;

public class Sergeant
{
	private String 						  name;
	private final int 					  maxMissions;
	private int 						  currMissions;
	private final int 					  workHours;
	private final ArrayList<String> 	  skills;
	private final String 				  priorityOrder;
	private Board 						  board;
	private Warehouse 					  warehouse;
	private ThreadPoolExecutor 			  pool;
	private final BlockingQueue<Runnable> missionQueue;
	private final int					  numOfThreads;
	
	
	/**
	 * 
	 * @param name_ The name of the Sgt.
	 * @param maxMissions_ Max missions that can be assigned to him
	 * at a given time.
	 * @param workHours_ Number of hours he can work in a sequence.
	 * @param skills_ List of skills.
	 * @param priorityOrder_ What kind of missions he prefers the
	 * best (longestMission, shortestMission, maxItems, minItems).
	 * @param board_ The board object that will be used to update
	 * missions statuses.
	 * @param warehouse_ The warehouse object that will be used
	 * to borrow/return gear.
	 * @param numOfThreads_ The number of threads this Sgt utilizes.
	 */
	public Sergeant(String name_, int maxMissions_,
					int workHours_, ArrayList<String> skills_,
					String priorityOrder_, Board board_,
					Warehouse warehouse_, int numOfThreads_)
	{
		this.name = name_;
		this.maxMissions = maxMissions_;
		this.workHours = workHours_;
		this.skills = skills_;
		this.priorityOrder = priorityOrder_;
		
		this.board = board_;
		this.warehouse = warehouse_;
		
		this.numOfThreads = numOfThreads_;
		this.missionQueue =
				new LinkedBlockingQueue<Runnable>(maxMissions_*workHours_);
		
		this.pool = new ThreadPoolExecutor(numOfThreads_, numOfThreads_,
										   0L, TimeUnit.MILLISECONDS,
										   this.missionQueue);
	}
	
	/**
	 * Accepts a mission for execution.
	 * @param mission The mission.
	 */
	public void handMission(Mission mission)
	{	
		
		RunnableMission rm = new RunnableMission(mission, this,
												 this.board, this.warehouse);
		
		if (mission.getSgt() == null) {
			Run.logger.fine("Sgt. " + this.getName() + ": Mission "
							    + mission.getName() + " STARTED.");
		}
		
		mission.setSgt(this);
		mission.decCounter(this.workHours);
		
		try {
			++this.currMissions;
			this.pool.execute(rm);
			--this.currMissions;
		} catch (RejectedExecutionException e) {
			if (!this.pool.isShutdown())
				System.out.println("Mission rejected.");
		}
	}
	
	/**
	 * 
	 * @return true if this Sgt can handle more missions right now.
	 */
	public boolean isAvailable()
	{
		return (this.currMissions < this.maxMissions)
			        && (this.missionQueue.remainingCapacity() > 0);
	}
	
	/**
	 * 
	 * @return The name of this sergeant.
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * 
	 * @return The work hours of this sergeant.
	 */
	public int getWorkHours()
	{
		return this.workHours;
	}
	
	/**
	 * Getter for the priorityOrder
	 * @return the priority order of this sergeant.
	 */
	public String getPriority()
	{
		return this.priorityOrder;
	}
	
	/**
	 * 
	 * @param skill A name of a skill.
	 * @return True of this sergeant has this skill, false otherwise.
	 */
	public boolean hasSkill(String skill)
	{
		for (String s : this.skills)
			if (s.equals(skill))
				return true;
		return false;
	}
	
	/**
	 * 
	 * @return Number of threads of this sergeant.
	 */
	public int numOfThreads()
	{
		return this.numOfThreads;
	}
	
	/**
	 * Gets the thread pool.
	 * @return The ExecutorService.
	 */
	public ExecutorService getPool()
	{
		return this.pool;
	}
	
	/**
	 * Clears the mission queue of the sergeant.
	 */
	public void clearQueue()
	{
		this.missionQueue.clear();
	}
}