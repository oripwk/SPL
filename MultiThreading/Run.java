/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Run
{	
	public static Logger logger;
	
	/**
	 * 
	 * @param args 
	 */
	public static void main(String args[])
	{
		logger = Logger.getLogger("log");
		
		try {
			FileHandler handler = new FileHandler("log.txt");
			handler.setFormatter(new SimpleFormatter());
			handler.setLevel(Level.FINE);
			logger.addHandler(handler);
		} catch(IOException e) {
			System.err.println("ERROR: Can't create log file.");
		}
		
		logger.setLevel(Level.FINE);
		
		Warehouse warehouse = new Warehouse();
		Board board = new Board();
		ChiefOfStaff cof = new ChiefOfStaff(board);
		Observer observer = new Observer(board, cof, warehouse);
		
		board.addChief(cof);
		
		Run run = new Run();
		run.readMissions(args[0], board);
		run.readSergeants(args[1], cof, board, warehouse);
		run.readWarehouse(args[2], warehouse);
		
		
		cof.start();
		observer.start();
	}
	
	private void readWarehouse(String propFile,
									  Warehouse warehouse)
	{
		Properties props = new Properties();
		
		try {
			props.load(new FileInputStream(propFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		String s = props.getProperty("numberOfItems");
		int length = Integer.parseInt(s.trim());
			
		for (int i = 0; i < length; ++i)
		{
			String name = props.getProperty("item" + i + "Name").trim();
			String sAmount = props.getProperty("item" + i + "Amount").trim();
			int amount = Integer.parseInt(sAmount);
			warehouse.addItem(name, amount);
		}
	}
	
	private void readSergeants(String propFile, ChiefOfStaff cof,
							   Board board, Warehouse warehouse)
	{
		Properties props = new Properties();
		
		try {
			props.load(new FileInputStream(propFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		String s = props.getProperty("numberOfSergeants");
		int length = Integer.parseInt(s.trim());
			
		for (int i = 0; i < length; ++i)
		{
			/* Get the properties into strings */
			String name = 		   props.getProperty("s" + i + "Name").trim();
			String sNumOfThreads = props.getProperty("s" + i + "NumOfThreads").trim();
			String sMaxMissions =  props.getProperty("s" + i + "MaxMissions").trim();
			String sWorkHours =    props.getProperty("s" + i + "WorkHours").trim();
			String priorityOrder = props.getProperty("s" + i + "PriorityOrder").trim();
			String sSkills =       props.getProperty("s" + i + "Skills");
			
			/* Convert strings to needed type for Sergeant constructor */
			
			int numOfThreads = Integer.parseInt(sNumOfThreads);
			int maxMissions = Integer.parseInt(sMaxMissions);
			int workHours = Integer.parseInt(sWorkHours);
			
			ArrayList<String> skills = Utils.stringToArrayList(sSkills, ",");
			
			/* Construct a Sergeant object */
			Sergeant sgt = new Sergeant(name, maxMissions,
										workHours, skills,
										priorityOrder, board,
										warehouse, numOfThreads);
			cof.addSgt(sgt);
		}
	}
	
	private void readMissions(String propFile,
			  						 Board board)
	{
		Properties props = new Properties();
		
		try {
			props.load(new FileInputStream(propFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		String numOfMissions = props.getProperty("numberOfMissions");		
		int length = Integer.parseInt(numOfMissions.trim());
		
		/* A list that for each mission, will hold it's prerequisites missions in strings */
		ArrayList<MissionPreqs> missionsPreqs = new ArrayList<MissionPreqs>(0);
		
		for (int i = 0; i < length; ++i)
		{
			/* Get the properties into strings */
			String name = 		  props.getProperty("m" + i + "Name").trim();
			String reqSkill = 	  props.getProperty("m" + i + "Skill");
			String sTime = 		  props.getProperty("m" + i + "Time").trim();
			String sPreMissions = props.getProperty("m" + i + "PreRequisites");
			String sItems = 	  props.getProperty("m" + i + "Items");
			
			
			/* Convert strings to needed type for Mission constructor */
			
			int time = Integer.parseInt(sTime);
			
			ArrayList<String> preMissions = Utils.stringToArrayList(sPreMissions, ",");
			ArrayList<Item> items = Utils.stringToSortedItems(sItems, ",");
			
			/* Construct a Mission object */
			Mission mission = new Mission(name, items,
									      reqSkill, time);
			
			board.addMission(mission);
			missionsPreqs.add(new MissionPreqs(mission, preMissions));
		}
		
		/* Use the missionPreqs list to fill the prerequisites list
		 * of each mission in the board with references to all the 
		 * Mission objects that this mission depends on.
		 */
		for (MissionPreqs mp : missionsPreqs) {
			if (mp.preqs == null)
				continue;
			for (String preName : mp.preqs) {
				Mission pre = board.findMission(preName);
				if (pre == null) {
					System.err.println("ERROR: Mission \"" + preName + "\" doesn't exist.");
					return;
				}
				mp.mission.addPre(pre);
			}
		}
	}
	
	/* Represents a mission and a list of its prerequisites mission names */
	class MissionPreqs
	{
		Mission mission;
		ArrayList<String> preqs;
		
		public MissionPreqs(Mission m_, ArrayList<String> pres_) {
			this.mission = m_;
			this.preqs = pres_;
		}
	}
}
