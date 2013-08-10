/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;


public class Utils
{
	/**
	 * Takes a string which represent a list of words delimited with `delim`
	 * and returns an ArrayList of Strings, each of which is a word in that
	 * String.
	 * @param list A `delim` delimited list.
	 * @param delim The delimiter of the list.
	 * @return ArrayList of Strings, each of which is a word in `list`.
	 */
	public static ArrayList<String> stringToArrayList(String list, String delim)
	{
		String trimmed = list.trim();
		if (trimmed.equals(""))
			return null;
		
		/* Extract a string for each skill */
		String arr[] = trimmed.split(delim);
		
		/* Trim whitespace */
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = arr[i].trim();
		}
		
		/* Make a ArrayList out of the skills list */
		return new ArrayList<String>(Arrays.asList(arr));
	}
	
	/**
	 * Takes a string which represent a list of items in the format
	 * "[item1](delim)[amount1](delim)...(delim)[itemn](delim)[amountn]...
	 * and returns an ArrayList of Items.
	 * @param list A `delim` delimited list.
	 * @param delim The delimiter of the list.
	 * @return ArrayList of Items.
	 */
	public static ArrayList<Item> stringToSortedItems(String list, String delim)
	{
		String trimmed = list.trim();
		if (trimmed.equals(""))
			return null;
		
		/* Extract a string for each item */
		String arr[] = trimmed.split(delim);
		
		/* Trim whitespace */
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = arr[i].trim();
		}
		
		ArrayList<Item> items = new ArrayList<Item>();
		for (int i = 0; i < arr.length; i += 2) {
			String name = arr[i].trim();
			int amount = Integer.parseInt(arr[i+1].trim());
			addInOrder(items, new Item(name, amount));
		}
		
		return items;
	}
	
	/* Adds item to list while keeping the list ordered according
	 * to the natural order of Item (Recall that Item implements Comparable).
	 */
	private static void addInOrder(ArrayList<Item> list, Item item)
	{
		if (list.isEmpty()) {
			list.add(item);
			return;
		}
		int i = Collections.binarySearch(list, item);
		if (i < 0)
			list.add((-1)*i-1, item); /* See documentation of java.util.Collections'
										 binarySearch(List list, Object key) */ 
		else
			list.add(i, item);
	}
}


