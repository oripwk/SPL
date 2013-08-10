/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ChiefOfStaff extends Thread
{
	public static int maxThreads;
	public static final int SECS_WAIT_TERMINATION = 60;
	
	private Board 				board;
	private MissionHolder 		missionHolder;
	private ArrayList<Sergeant> sergeants;
	
	/**
	 * Constructs a new Chief Of Staff with empty mission holder
	 * and empty sergeants list.
	 * @param board_ The board that the COF will use.
	 */
	public ChiefOfStaff(Board board_)
	{
		this.board = board_;
		this.missionHolder = new MissionHolder();
		this.sergeants = new ArrayList<Sergeant>(0);
	}
	
	/**
	 * The routine of the COF.
	 */
	public synchronized void run()
	{
		try {
			while (!Thread.currentThread().isInterrupted()) {
				this.scan();
				this.distribute();
				this.wait();
			}
		} catch (InterruptedException e) {
			/* Allow thread to exit */
		}
	}
	
	/**
	 * Terminates the COF.
	 */
	public void cancel()
	{
		interrupt();
	}
	
	/* Scan the board and add relevant missions to missionHolder */
	private void scan()
	{
		this.missionHolder.addMissions(this.board.exportMissions());
	}
	
	/* Distribute missions to sergeants according to their skills
	 * and the priority orders.
	 */
	private void distribute()
	{
		for (Sergeant s : this.sergeants)
		{	
			boolean reverse;	/* Signifies if the missions should be
								   traversed backwards or not */
			ListIterator<Mission> it;
			String priorityOrder = s.getPriority();
			synchronized (this.missionHolder) {
				if (priorityOrder.equals("shortestMission")) {
					it = this.missionHolder.listIterator('l', false);
					reverse = false;
				} else if (priorityOrder.equals("longestMission")) {
					it = this.missionHolder.listIterator('l', true);
					reverse = true;
				} else if (priorityOrder.equals("minItems")) {
					it = this.missionHolder.listIterator('i', false);
					reverse = false;
				} else if (priorityOrder.equals("maxItems")) {
					it = this.missionHolder.listIterator('i', true);
					reverse = true;
				} else {
					System.err.println("ERROR: Priority order \""
									       + priorityOrder + "\" is invalid.");
					return;
				}
				
				while (hasNext(it, reverse)) {
					Mission mission = next(it, reverse);
					if (!mission.canBeHanded())
						continue;
					if ((mission.getSgt() != null) && (mission.getSgt() != s))
						continue;
					if ((s.isAvailable()) && (s.hasSkill(mission.getSkill())))
						s.handMission(mission);
				}
			}
		}
	}
	
	private boolean hasNext(ListIterator<Mission> it, boolean reverse)
	{
		if (reverse)
			return it.hasPrevious();
		else
			return it.hasNext();
	}
	
	private Mission next(ListIterator<Mission> it, boolean reverse)
	{
		if (reverse) 
			return it.previous();
		else
			return it.next();
	}
	
	/**
	 * Wake up the COF.
	 */
	public synchronized void wakeUp()
	{
		this.notify();
	}
	
	/**
	 * Adds a sergeant to the sergeants list.
	 * @param sgt The sergeant to be added to the list.
	 */
	public synchronized void addSgt(Sergeant sgt)
	{
		this.sergeants.add(sgt);
		maxThreads += sgt.numOfThreads();
		this.notify();
	}
		
	/**
	 * Prints a list of sergeants, their assigned missions, and 
	 * the time remaining for completion of each mission.
	 */
	public void printSgts()
	{
		ArrayList<Mission> assignedMissions;
		
		if (this.sergeants == null) {
			System.out.println("Sergeants list is empty.\n");
			return;
		}
		System.out.println("--SERGEANTS--\n");
		for (Sergeant s : this.sergeants)
		{
			System.out.println("--Sgt. "+ s.getName() + "--\n"
							       + "\tAssigned Missions:");
			assignedMissions = this.board.getSgtsMissions(s); 
			
			if (assignedMissions == null)
				System.out.println("\t\tNONE.");
			else 
				for (Mission m : assignedMissions)
					System.out.println("\t\t" + m.getName());
			
			System.out.println();
		}
		System.out.println();
	}
	
	/**
	 * Deletes the mission from the Mission Holder.
	 * @param mission The mission to be removed from the Mission Holder.
	 */
	public void deleteFromMissionHolder(Mission mission)
	{
		this.missionHolder.remove(mission);
		
	}
	
	/**
	 * Shuts down an executor and waits for it to shut down.
	 * @param pool An executor to be shut down.
	 */
	public void shutdownAndAwaitTermination(ExecutorService pool)
	{
		pool.shutdown(); /* Disable new tasks from being submitted */
		try {
			/* Wait a while for existing tasks to terminate */
			if (!pool.awaitTermination(SECS_WAIT_TERMINATION, TimeUnit.SECONDS)) {
				pool.shutdownNow(); /* Cancel currently executing tasks */
				/* Wait a while for tasks to respond to being cancelled */
				if (!pool.awaitTermination(SECS_WAIT_TERMINATION, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			/* (Re-)Cancel if current thread also interrupted */
			pool.shutdownNow();
			/* Preserve interrupt status */
			Thread.currentThread().interrupt();
		}
	}
	/**
	 * Terminates the sergeants.
	 */
	public void shutDownSgts()
	{
		for (Sergeant s : this.sergeants) {
			s.clearQueue();
			this.shutdownAndAwaitTermination(s.getPool());
		}
	}
}
