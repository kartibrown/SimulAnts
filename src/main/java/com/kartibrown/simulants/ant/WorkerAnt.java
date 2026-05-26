package com.kartibrown.simulants.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import com.kartibrown.simulants.Position;
import com.kartibrown.simulants.item.Food;
import com.kartibrown.simulants.item.Item;
import com.kartibrown.simulants.world.Colony;
import com.kartibrown.simulants.world.Tile;
import com.kartibrown.simulants.world.World;

public class WorkerAnt extends Ant
{
	private static final int CLEAN_RADIUS = 3;
	private static final int MAX_CARRY = 10;

	protected final List<Item> inventory;

	private Task task;

	private enum Task
	{
		FIND_FOOD
		{
			@Override
			final void perform(final WorkerAnt ant, final World world)
			{
				if (world.getQueen().isDirty())
				{
					ant.setTask(Task.CLEAN_QUEEN);
					world.log(world.getQueen().getName() + " needs cleaning!");
					return;
				}

				if (ant.isTired())
				{
					ant.setTask(Task.RETURN_HOME);
				}
				else
				{
					final int x = ant.getPosition().getX();
					final int y = ant.getPosition().getY();

					if (world.getGrid()[x][y].hasFood())
					{
						ant.pickUpFoodFrom(world.getGrid()[x][y]);
					}
					else
					{
						ant.move(world);
					}
				}
			}
		},
		RETURN_HOME
		{
			@Override
			final void perform(final WorkerAnt ant, final World world)
			{
				if (ant.getPosition().equals(world.colony.getPosition()))
				{
					ant.setTask(Task.REST);
				}
				else
				{
					ant.goTo(world.colony, world);
				}
			}
		},
		CLEAN_QUEEN
		{
			@Override
			final void perform(final WorkerAnt ant, final World world)
			{
				if (ant.isTired())
				{
					ant.setTask(Task.REST);
					return;
				}

				final QueenAnt qAnt = world.getQueen();

				if (qAnt.getDirtiness() <= 0)
				{
					world.log(ant.getName() + " has finished cleaning " + qAnt.getName());
					qAnt.setDirtiness(0);
					ant.setTask(REST);
					return;
				}

				if (ant.isNear(qAnt, CLEAN_RADIUS) && qAnt.getDirtiness() > 0)
				{
					ant.clean(world.getQueen());
				}
				else
				{
					ant.goTo(world.getQueen(), world);
				}
			}
		},
		REST
		{
			@Override
			final void perform(final WorkerAnt ant, final World world)
			{
				if (ant.getCurrentInventoryWeight() > 0)
					ant.storeFoodAt(world.colony);

				if (ant.isRested())
				{
					ant.setTask(Task.FIND_FOOD);
				}
				else
				{
					ant.setEnergy(ant.getEnergy() + 2);
				}
			}
		};

		abstract void perform(final WorkerAnt ant, final World world);
	}

	public WorkerAnt(final String name, final Position pos, final SplittableRandom rng)
	{
		super(name, pos, rng);

		task = Task.REST;
		inventory = new ArrayList<>();
		this.damage = 2;
	}

	public WorkerAnt(final String name, final int x, final int y, final SplittableRandom rng)
	{
		super(name, new Position(x, y), rng);

		task = Task.REST;
		inventory = new ArrayList<>();
		this.damage = 2;
	}

	@Override
	public void update(final World world)
	{
		task.perform(this, world);

		if (task != Task.REST)
			energy--;
	}

	@Override
	public void move(final World world)
	{
		final int xMove = rng.nextBoolean() ? 1 : -1;
		final int yMove = rng.nextBoolean() ? 1 : -1;

		pos.setX(pos.getX() + (rng.nextBoolean() ? xMove : 0));
		pos.setY(pos.getY() + (rng.nextBoolean() ? yMove : 0));

		pos.setX(Math.max(0, Math.min(pos.getX(), world.getSizeX() - 1)));
		pos.setY(Math.max(0, Math.min(pos.getY(), world.getSizeY() - 1)));

		this.energy--;
	}

	protected final void storeFoodAt(final Colony colony)
	{
		colony.storeAll(inventory);

		inventory.clear();
	}

	protected final void pickUpFoodFrom(final Tile tile)
	{
		if (!tile.hasFood() || getRemainingInventoryCapacity() <= 0)
			return;

		final Food food = tile.peekFood();

		final int maxAmountByWeight = getRemainingInventoryCapacity() / food.getWeightPerUnit();
		final int amountToTake = Math.min(food.getAmount(), maxAmountByWeight);

		if (amountToTake <= 0)
			return;

		final Food takenFood = tile.takeFood(amountToTake);
		inventory.add(takenFood);
	}

	public final void clean(final QueenAnt qAnt)
	{ qAnt.setDirtiness(qAnt.getDirtiness() - 5); }

	/*
	 * GETTERS & SETTERS
	 */

	private final int getCurrentInventoryWeight()
	{
		int total = 0;

		for (final Item item : inventory)
			total += item.getWeight();

		return total;
	}

	private final int getRemainingInventoryCapacity()
	{ return MAX_CARRY - getCurrentInventoryWeight(); }

	private final Task getTask()
	{ return task; }

	private void setTask(final Task task)
	{ this.task = task; }

	private final boolean canCarryMore()
	{ return getRemainingInventoryCapacity() > 0; }
}
