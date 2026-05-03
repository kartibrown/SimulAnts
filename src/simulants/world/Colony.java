package simulants.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import simulants.Position;
import simulants.item.Item;

public class Colony
{

	private Position pos;

	private final List<Item> inventory;

	private int storedFood;

	public Colony()
	{
		pos = null;
		inventory = new ArrayList<>();
		storedFood = 0;
	}

	/*
	 * GETTERS & SETTERS
	 */

	public final void storeAll(final Collection<? extends Item> c)
	{
		for (final Item item : c)
		{
			store(item);
		}
	}

	public final void store(final Item item)
	{
		if (item == null)
			return;

		inventory.add(item);
		storedFood += item.getAmount();
	}

	public final Position getPosition()
	{ return pos; }

	public final boolean hasPosition()
	{ return pos != null; }

	public final void setPosition(final int x, final int y)
	{
		if (pos == null)
			pos = new Position(x, y);

		pos.setX(x);
		pos.setY(y);
	}

	public final int getStoredFood()
	{ return storedFood; }

	public final boolean hasEnoughFood()
	{ return getStoredFood() >= 5; }
}
