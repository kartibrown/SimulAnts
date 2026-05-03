package simulants.item;

public abstract class Item
{
	protected int amount;

	public Item()
	{ amount = 0; }

	/*
	 * GETTERS & SETTERS
	 */

	public final int getAmount()
	{ return this.amount; }

	public final void setAmount(final int amount)
	{ this.amount = amount; }

	public abstract int getWeight();

	public abstract int getWeightPerUnit();
}
