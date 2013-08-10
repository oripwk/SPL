/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;


public class MissionHolder
{
	/* The lists are kept sorted at all times */
	private ArrayList<Mission> missionsByLength;
	private ArrayList<Mission> missionsByItems;
	
	/**
	 * Constructs a new Mission Holder with empty mission list.
	 */
	public MissionHolder()
	{
		this.missionsByLength = new ArrayList<Mission>(0);
		this.missionsByItems = new ArrayList<Mission>(0);
	}
	
	/**
	 * Adds a list of missions to the Mission Holder.
	 * @param missions_ List of missions.
	 */
	public synchronized void addMissions(ArrayList<Mission> missions_)
	{
		if (missions_ == null)
			return;
		
		for (Mission m : missions_) {
			addInOrder(this.missionsByLength, m, new LengthComparator());
			addInOrder(this.missionsByItems, m, new ItemsComparator());
			m.setInMissionHolder();
		}
	}
	
	/**
	 * Getter for the Mission Holder's size.
	 * @return The number of missions within the Mission Holder right now.
	 */
	public synchronized int size()
	{
		return this.missionsByLength.size();
	}
	
	
	/**
	 * Delete the mission from the Mission Holder.
	 * @param mission The mission we want to delete.
	 */
	public synchronized void remove(Mission mission)
	{
		this.missionsByLength.remove(mission);
		this.missionsByItems.remove(mission);
		mission.unsetInMissionHolder();
	}
	
	/**
	 * Returns an iterator to the right list according to `order`.
	 * If `reverse` is true then it returns the iterator to the end
	 * of the list.
	 * @param order The order in which the list of which the returned
	 * iterator is sorted.
	 * @param reverse If true then an iterator to the end of the list
	 * will be returned.
	 * @return A list iterator.
	 */
	public synchronized ListIterator<Mission> listIterator(char order,
														   boolean reverse)
	{
		if (order == 'l') {
			if (reverse)
				return this.missionsByLength.listIterator(this.missionsByLength.size());
			else
				return this.missionsByLength.listIterator(0);
		} else {
			if (reverse)
				return this.missionsByItems.listIterator(this.missionsByItems.size());
			else
				return this.missionsByItems.listIterator();
		}
	}
	
	/* Takes a list, a mission and a comparator, and adds the mission to the list
	 * in the right place, according to the order of elements dictated by the
	 * comparator */
	private synchronized void addInOrder(ArrayList<Mission> list,
										 Mission m,
										 Comparator<Mission> c)
	{
		if (list.isEmpty()) {
			list.add(m);
			return;
		}
		int i = Collections.binarySearch(list, m, c); 
		if (i < 0)
			list.add((-1)*i - 1, m); /* See documentation of java.util.Collections'
		 								binarySearch(List list, Object key, Comparator c) */ 
		else
			list.add(i, m);
	}
}

class LengthComparator implements Comparator<Mission>
{
	public int compare(Mission m, Mission n)
	{
		return m.getInitTime() - n.getInitTime();
	}
}

class ItemsComparator implements Comparator<Mission>
{
	public int compare(Mission m, Mission n)
	{
		return m.numOfItems() - n.numOfItems();
	}
}