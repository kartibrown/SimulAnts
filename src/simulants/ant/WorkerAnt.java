package simulants.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import simulants.Position;
import simulants.item.Food;
import simulants.item.Item;
import simulants.world.Colony;
import simulants.world.Tile;
import simulants.world.World;

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
					System.out.println(world.getQueen().getName() + " needs cleaning!");
					return;
				}

				if (ant.isTierd())
				{
					ant.setTask(Task.RETURN_HOME);
					System.out.println(ant.getName() + " is returning home");
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
					System.out.println(ant.getName() + " is resting!");
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
				if (ant.isTierd())
				{
					ant.setTask(Task.REST);
					return;
				}

				final QueenAnt qAnt = world.getQueen();

				if (qAnt.getDirtyness() <= 0)
				{
					System.out.println(ant.getName() + " has finished cleaning " + qAnt.getName());
					qAnt.setDirtyness(0);
					ant.setTask(REST);
					return;
				}

				if (ant.isNear(qAnt, CLEAN_RADIUS) && qAnt.getDirtyness() > 0)
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
		System.out.println(this.getName() + " is storing food at the colony");

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

		System.out.println(this.getName() + " picked up food!");
	}

	public final void clean(final QueenAnt qAnt)
	{ qAnt.setDirtyness(qAnt.getDirtyness() - 5); }

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

	public final Task getTask()
	{ return task; }

	public final void setTask(final Task task)
	{ this.task = task; }

	private final boolean canCarryMore()
	{ return getRemainingInventoryCapacity() > 0; }
}
