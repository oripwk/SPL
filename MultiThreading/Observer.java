/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.util.Scanner;
import java.util.ArrayList;

public class Observer extends Thread
{
	private ChiefOfStaff	cof;
	private Board 			board;
	private Warehouse 		warehouse;
	
	/**
	 * 
	 * @param board_ The Board.
	 * @param cof_ The Chief Of Staff.
	 * @param warehouse_ The Warehouse.
	 */
	public Observer(Board board_, ChiefOfStaff cof_, Warehouse warehouse_)
	{
		this.cof = cof_;
		this.board = board_;
		this.warehouse = warehouse_;
	}
	
	/**
	 * The routine of the observer.
	 */
	public void run()
	{
		Scanner scanner = new Scanner(System.in);
		String input = "";
		boolean stop = false;
		
		for (;;)
		{
			System.out.println("Waiting for command...");
			input = scanner.nextLine();
			stop = parseAndExecute(input);
			if (stop) {
				System.out.println("Please wait while system is terminating...");
				this.cof.cancel();
				this.cof.shutDownSgts();
				System.out.println("System is now terminated.");
				break;
			}
		}
	}
	
	/* Takes the input string and parse the command it
	 * assumes to contain.
	 */
	private boolean parseAndExecute(String input)
	{
		
		String splitted[] = input.split(" ");
		String command = splitted[0].trim();
		
		if (command.equals("completeMissions")) {
			this.board.printCompleteMissions();
			return false;
		} else if (command.equals("incompleteMissions")) {
			this.board.printIncompleteMissions();
			return false;
		} else if (command.equals("sergeants")) {
			this.cof.printSgts();
			return false;
		} else if (command.equals("warehouse")) {
			this.warehouse.printWarehouse();
			return false;
		} else if (command.equals("addMission")) {
			Mission mission = parseMission(input);
			this.board.addMission(mission);
			return false;	
		} else if (command.equals("addSergeant")) {
			Sergeant sgt = parseSergeant(input);
			this.cof.addSgt(sgt);
			return false;	
		} else if (command.equals("addItem")) {
			this.warehouse.addItem(splitted[1], Integer.parseInt(splitted[2]));
			return false;
		} else if (command.equals("stop")) {
			return true;
		}
		/* If none: */
		System.out.println("Unknown command");
		return false;
	}
	
	/* Parses the command addItem and it's arguments */
	private Mission parseMission(String input)
	{
		String splitted[] = input.split(",");
		splitted[0] = splitted[0].trim();
		String segment1[] = splitted[0].split(" ");
		
		String name = segment1[1].trim();
		String skill = segment1[2].trim();
		String sTime = segment1[3].trim(); /* Yes, 3 is a magic number. Can't you see
		 									  a bunny coming out of this hat? */
		int time = Integer.parseInt(sTime);
		
		ArrayList<Item> items =
				Utils.stringToSortedItems(splitted[1], " ");
		ArrayList<String> preMissions =
		        Utils.stringToArrayList(splitted[2], " ");
		
		/* Construct a Mission object */
		Mission mission = new Mission(name, items, skill, time);
		
		if (preMissions != null) {
			for (String preName : preMissions) {
				Mission pre = this.board.findMission(preName);
				if (pre == null) {
					System.err.println("ERROR: Mission \""
									       + preName + "\" doesn't exist.");
					return mission;
				}
				if (!pre.getStatus().equals(Status.DONE)) 
					mission.addPre(pre);
			}
		}
		
		return mission;
	}
	
	/* Parses the command addSergeant and it's arguments */
	private Sergeant parseSergeant(String input)
	{
		String splitted[] = input.split(",");
		splitted[0] = splitted[0].trim();
		splitted[2] = splitted[2].trim();
		String segment1[] = splitted[0].split(" ");
		String segment3[] = splitted[2].split(" ");
		
		String name = segment1[1].trim();
		String sNumOfThreads = segment1[2].trim();
		String sMaxMissions = segment1[3].trim(); 
		
		String sWorkHours = segment3[0].trim();
		String priorityOrder = segment3[1].trim();
		
		int numOfThreads = Integer.parseInt(sNumOfThreads);
		int maxMissions = Integer.parseInt(sMaxMissions);
		int workHours = Integer.parseInt(sWorkHours);
		
		ArrayList<String> skills = Utils.stringToArrayList(splitted[1], " ");
		
		/* Construct a Sergeant object */
		Sergeant sgt = new Sergeant(name, maxMissions,
									workHours, skills,
									priorityOrder, this.board,
									this.warehouse, numOfThreads);
		return sgt;
	}
}
