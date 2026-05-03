package simulants.world;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import simulants.Position;
import simulants.ant.QueenAnt;
import simulants.ant.WorkerAnt;
import simulants.item.Food;

public final class World
{
	public final Colony colony;
	private final QueenAnt queen;
	private final List<WorkerAnt> ants;
	private int foodSpawnTimer;
	private final int baseFoodSpawnCooldown;

	private final Tile[][] grid;
	private final int sizeX, sizeY;

	private final SplittableRandom rng;

	public World()
	{
		rng = new SplittableRandom();
		colony = new Colony();

		sizeX = 20;
		sizeY = 20;
		grid = new Tile[sizeX][sizeY];

		for (int x = 0; x < grid.length; x++)
			for (int y = 0; y < grid[x].length; y++)
				grid[x][y] = new Tile(rng.split());

		queen = new QueenAnt("The Queen", getCenterX() / 2, getCenterY() / 2, rng.split(), 60);
		ants = new ArrayList<>();

		foodSpawnTimer = 0;
		baseFoodSpawnCooldown = 200;
	}

	public void start()
	{
		while (true)
		{
			updateWorld();
			queen.update(this);

			for (final WorkerAnt ant : ants)
				ant.update(this);

			try
			{
				Thread.sleep(100);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private final void updateWorld()
	{
		if (colony.hasPosition())
		{
			updatePheramones();
			getTile(colony.getPosition()).setHomePheromones(Tile.MAX_PHEROMONES);
		}

		if (foodSpawnTimer <= 0)
		{
			addFoodToWorld();
			foodSpawnTimer = baseFoodSpawnCooldown;
		}
		else
		{
			foodSpawnTimer--;
		}
	}

	private final void updatePheramones()
	{
		double[][] nextHome = new double[sizeX][sizeY];

		for (int x = 0; x < sizeX; x++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				final Tile tile = getTile(x, y);

				double value = tile.getHomePheromones();

				value *= 0.995;

				nextHome[x][y] += value * 0.90;

				for (final Position p : getNeighbours(x, y))
				{
					nextHome[p.getX()][p.getY()] += value * 0.025;
				}
			}
		}

		for (int x = 0; x < sizeX; x++)
		{
			for (int y = 0; y < sizeY; y++)
			{
				getTile(x, y).setHomePheromones(nextHome[x][y]);
			}
		}
	}

	private final List<Position> getNeighbours(final int x, final int y)
	{
		final List<Position> neighbours = new ArrayList<>();

		addIfInside(neighbours, x + 1, y);
		addIfInside(neighbours, x - 1, y);
		addIfInside(neighbours, x, y + 1);
		addIfInside(neighbours, x, y - 1);

		return neighbours;
	}

	private final void addIfInside(final List<Position> list, final int x, final int y)
	{
		if (x >= 0 && x < sizeX && y >= 0 && y < sizeY)
			list.add(new Position(x, y));
	}

	private final void addFoodToWorld()
	{
		final int x = rng.nextInt(sizeX);
		final int y = rng.nextInt(sizeY);

		grid[x][y].addFood(new Food(rng.nextInt(5)));
	}

	/*
	 * GETTERS & SETTERS
	 */

	public final Tile getTile(final int x, final int y)
	{ return getTile(new Position(x, y)); }

	public final Tile getTile(final Position pos)
	{ return grid[pos.getX()][pos.getY()]; }

	public final Tile[][] getGrid()
	{ return grid; }

	public final QueenAnt getQueen()
	{ return queen; }

	public final List<WorkerAnt> getAnts()
	{ return ants; }

	public final void spawnWorkerFrom(final QueenAnt qAnt)
	{ ants.add(qAnt.spawnWorker("Worker: " + String.valueOf(ants.size() + 1))); }

	public final int getSizeX()
	{ return sizeX; }

	public final int getSizeY()
	{ return sizeY; }

	public final int getCenterX()
	{ return grid.length / 2; }

	public final int getCenterY()
	{ return grid[0].length / 2; }
}
