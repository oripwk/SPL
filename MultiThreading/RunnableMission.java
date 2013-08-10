/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */




public class RunnableMission implements Runnable
{
	private static final int WAIT_TIME_MULTIPILICAND = 1000;
	
	private Mission		mission;
	private Sergeant 	sgt;
	private Board 		board;
	private Warehouse 	warehouse;
	
	/**
	 * 
	 * @param mission_ The mission that this thread will execute.
	 * @param sgt_ The sergeant in command of this thread.
	 * @param board_ A reference to a Board object.
	 * @param warehouse_ A reference to a Warehouse object
	 */
	public RunnableMission(Mission mission_,
						   Sergeant sgt_,
						   Board board_,
						   Warehouse warehouse_)
	{
		this.mission = mission_;
		this.sgt = sgt_;

		this.board = board_;		
		this.warehouse = warehouse_;
	}
	
	/* Updates the time remaining for this mission */
	private void updateMissionTime()
	{
		this.board.updateMissionTime(this.mission, this.sgt.getWorkHours());
	}
	
	/**
	 * Executes the mission.
	 */
	public synchronized void run()
	{	
		this.warehouse.borrowItems(this.mission.getItems(),
								       this.sgt.getName());
		
		try {
			this.wait((this.sgt.getWorkHours()) * WAIT_TIME_MULTIPILICAND);
		} catch (InterruptedException e) {}
		
		this.warehouse.returnItems(this.mission.getItems(),
								       this.sgt.getName());
		this.updateMissionTime();
			
	}
}
