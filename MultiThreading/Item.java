/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */




/**
 * Represents an item and it's amount.
 */
public class Item implements Comparable<Item>
{
	protected String name;
	protected int 	 amount;
	
	/**
	 * Constructs a new item with name `name_` and amount `amount_`.
	 * @param name_ The name of the item.
	 * @param amount_ the amount of that item.
	 */
	public Item(String name_, int amount_)
	{
		this.name = name_;
		this.amount = amount_;
	}
	
	/**
	 * Getter for the item name.
	 * @return The name of the item.
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Getter for the item amount.
	 * @return The amount of the item.
	 */
	public int getAmount()
	{
		return this.amount;
	}
	
	/**
	 * @param item The item we want to compare to
	 * @return The result of comparing the amounts;
	 */
	public int compareTo(Item item)
	{
		return this.name.compareTo(item.name);
	}
}
