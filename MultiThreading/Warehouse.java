/*
 * Authors: Ori Popowski & Dmitry Kravchenko
 */



import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class Warehouse
{
	private ArrayList<StockItem> items;
	
	/**
	 * Constructs a new Warehouse with no items.
	 */
	public Warehouse()
	{
		this.items = new ArrayList<StockItem>(0);
	}
	
	/**
	 * 
	 * @param items_ A list of the items to borrow and their amounts.
	 * @param sgtName The name of the Sgt that wants to borrow these items.
	 */
	public void borrowItems(final ArrayList<Item> items_, String sgtName)
	{
		for (Item sgtItem : items_) {
			StockItem stockItem = findItem(sgtItem.getName());
			if (stockItem == null) {
				System.err.println("ERROR: Item \"" + sgtItem.getName()
								       + "\" doesn't exist in the warehouse.");
				return;
			}
			stockItem.borrow(sgtName, sgtItem.getAmount());
		}
	}
		
	/**
	 * 
	 * @param items_ A list of the items to return and their amounts.
	 * @param sgtName The name of the Sgt that wants to return these items.
	 */
	public void returnItems(final ArrayList<Item> items_, String sgtName)
	{
		for (Item sgtItem : items_) {
			StockItem stockItem = findItem(sgtItem.getName());
			if (stockItem == null) {
				System.err.println("ERROR: Item \"" + sgtItem.getName()
								       + "\" doesn't exist in the warehouse.");
				return;
			}
			stockItem.giveBack(sgtName, sgtItem.getAmount());
		}
	}
	
	/**
	 * 
	 * @param name The name of the item that will be added.
	 * @param amount The amount of that item.
	 */
	public void addItem(String name, int amount)
	{
		StockItem item = findItem(name);
		if (item == null)
			this.items.add(new StockItem(name, amount));
		else 
			item.addToAmount(amount);
	}
	
	/* Gets item name and returns the corresponding item in the warehouse */
	private StockItem findItem(String itemName)
	{	
		for (StockItem item : this.items)
			if (item.getName().equals(itemName))
				return item;
		
		return null;
	}
	
	/**
	 * Prints the items in the warehouse. If an item is absent
	 * it prints the sergeants that hold it and the amount.
	 */
	public void printWarehouse()
	{
		if (this.items == null) {
			System.out.println("Warehouse is empty.\n");
			return;
		}
		
		for (StockItem item : this.items) {
			int initAmount = item.getInitAmount();
			int currAmount = item.getCurrAmount();
			System.out.println ("Item " + item.getName() + ":\n"
								    + "\tInitial Amount: " + initAmount + "\n"
								    + "\tCurrent Amount: " + currAmount);
			if (initAmount > currAmount) {
				System.out.println("\tHoldings:");
				for (BorrowedTo b : item.borrows)
					System.out.println("\t\tSgt. " + b.getSgtName()
									       + ": " + b.getAmount() + " units.");
			}
			System.out.println();
		}
	}
	
	
	/* Nested classes */
	
	/*
	 * Represents an item in the warehouse
	 */ 
	class StockItem extends Item
	{
		private int currAmount;
		/* List of sergeants that hold this type of
		   item, and how many of it they hold */
		private ArrayList<BorrowedTo> borrows;
		private ArrayBlockingQueue<Thread> queue;
		
		public StockItem(String name_, int amount_)
		{
			super(name_, amount_);
			this.currAmount = amount_;
			this.borrows = new ArrayList<BorrowedTo>(0);
			this.queue = new ArrayBlockingQueue<Thread>(ChiefOfStaff.maxThreads);
		}
		
		public String getName() {
			return this.name;
		}
		
		public int getInitAmount() {
			return this.amount;
		}
		
		public synchronized int getCurrAmount() {
			return this.currAmount;
		}
		
		public synchronized BorrowedTo findSgt(String sgtName) {
			for (BorrowedTo item : this.borrows)
				if (item.getSgtName().equals(sgtName))
					return item;
			
			return null;
		}
		
		public synchronized void borrow(String sgtName, int amount)
		{
			if (amount > this.currAmount) {
				try {
					this.queue.put(Thread.currentThread());
				} catch (InterruptedException e) {}
			
				do {
					try {
						this.wait();
					} catch (InterruptedException e) {}
				} while ((amount > this.currAmount)
					       || (this.queue.peek() != Thread.currentThread()));
				
				this.queue.remove();
			}
			
			this.currAmount -= amount;
			updateBorrows(sgtName, amount);
			
			
		}
		public synchronized void giveBack(String sgtName, int amount)
		{
			this.currAmount += amount;
			updateBorrows(sgtName, (-1)*amount);
			this.notifyAll();
			
		}
		
		public synchronized void updateBorrows(String sgtName, int amount)
		{
			BorrowedTo item = this.findSgt(sgtName);
			if (item == null)
				this.borrows.add(new BorrowedTo(sgtName, amount));
			else
				item.changeAmount(amount);
		}
		
		public synchronized void addToAmount(int amount_)
		{
			this.amount += amount_;
			this.currAmount += amount_;
			this.notifyAll();
		}
	}
	
	/*
	 * Represents an entry in an item's borrow list.
	 */
	class BorrowedTo
	{
		String sgtName;
		int amount;
		
		BorrowedTo(String sgtName_, int amount_) {
			this.sgtName = sgtName_;
			this.amount = amount_;
		}
		
		String getSgtName() {
			return this.sgtName;
		}
		
		void changeAmount(int amount_) {
			this.amount += amount_; 
		}
		
		int getAmount() {
			return this.amount;
		}
	}
}