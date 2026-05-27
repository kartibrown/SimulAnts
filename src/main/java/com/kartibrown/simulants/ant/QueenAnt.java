package com.kartibrown.simulants.ant;

import java.util.SplittableRandom;

import com.kartibrown.simulants.Position;
import com.kartibrown.simulants.world.Tile;
import com.kartibrown.simulants.world.World;

public final class QueenAnt extends Ant
{
	public static final int FOOD_COST_SPAWN_ANT = 5;

	private final int baseSpawnCooldown;
	private int spawnTimer;
	private final int timePenalty;

	private int dirtiness;

	private final int energyCostToSpawn;

	private boolean hasSpawnedFirstAnt;

	private Task task;

	public QueenAnt(
			final String name, final int x, final int y, final SplittableRandom rng, final int energyCostToSpawn
	)
	{
		super(name, new Position(x, y), rng);

		this.hunger = 100; // Does not have logic HERE yet

		this.baseSpawnCooldown = 100;
		this.spawnTimer = 100;
		this.timePenalty = 0;

		this.dirtiness = 0;

		this.energyCostToSpawn = energyCostToSpawn;

		this.hasSpawnedFirstAnt = false;

		this.task = Task.FIND_COLONY;
	}

	public WorkerAnt spawnWorker(final String name, final World world)
	{
		world.log(this.name + " spawned " + name);

		dirtiness += 2;
		return new WorkerAnt(name, new Position(pos.getX(), pos.getY()), rng.split());
	}

	@Override
	public void update(final World world)
	{
		task.perform(this, world);
		spawnTimer--;
		energy++;
	}

	@Override
	public boolean canMove(final World world)
	{
		return !world.colony.hasPosition();
	}

	private void createColony(final World world)
	{
		world.log(getName() + " found a spot for her colony");

		final int x = getPosition().getX();
		final int y = getPosition().getY();

		world.colony.setPosition(x, y);
		world.getTile(x, y).setHomePheromones(100);

		setTask(Task.SPAWN_WORKER);
	}

	private void moveWhileSearchingForColony(final World world)
	{
		if (!canMove(world))
		{
			return;
		}

		move(world);
		setDirtiness(getDirtiness() + 1);
	}

	public void resetSpawnTimer()
	{ this.spawnTimer = this.baseSpawnCooldown + this.timePenalty; }

	/*
	 * GETTERS & SETTERS
	 */

	public void setHasSpawnedFirstAnt(final boolean hasSpawnedFirstAnt)
	{ this.hasSpawnedFirstAnt = hasSpawnedFirstAnt; }

	public boolean getHasSpawnedFirstAnt()
	{ return this.hasSpawnedFirstAnt; }

	public boolean isSuitableForColony(final Tile tile)
	{ return tile.getColonySuitabilityScore() > 5; }

	public boolean isReadyForBirth()
	{ return this.spawnTimer <= 0; }

	public int getBaseSpawnCooldown()
	{ return baseSpawnCooldown; }

	public int getTimePenalty()
	{ return timePenalty; }

	private void setTask(final Task task)
	{ this.task = task; }

	public int getEnergyCostToSpawn()
	{ return energyCostToSpawn; }

	public boolean isDirty()
	{ return dirtiness > 60; }

	public void setDirtiness(final int dirtyness)
	{ this.dirtiness = dirtyness; }

	public int getDirtiness()
	{ return dirtiness; }

	private enum Task
	{
		SPAWN_WORKER
		{
			@Override
			public final void perform(final QueenAnt qAnt, final World world)
			{
				if (!world.colony.hasPosition())
				{
					qAnt.setTask(Task.FIND_COLONY);
					return;
				}

				final boolean isFirstWorker = !qAnt.getHasSpawnedFirstAnt();
				final boolean canSpawnFirstWorker = isFirstWorker && qAnt.getEnergy() >= qAnt.getEnergyCostToSpawn();
				final boolean canSpawnWithFood = !isFirstWorker && qAnt.isReadyForBirth() && world.colony.hasEnoughFood();

				if (canSpawnFirstWorker || canSpawnWithFood)
				{
					if (isFirstWorker)
					{
						qAnt.setEnergy(qAnt.getEnergy() - qAnt.getEnergyCostToSpawn());
					}
					else
					{
						world.colony.consumeFood(FOOD_COST_SPAWN_ANT);
					}

					world.spawnWorkerFrom(qAnt);
					qAnt.resetSpawnTimer();
					qAnt.setHasSpawnedFirstAnt(true);
				}
			}
		},
		FIND_COLONY
		{
			@Override
			public final void perform(final QueenAnt qAnt, final World world)
			{
				final Tile currentTile = world.getTile(qAnt.getPosition());

				if (qAnt.isSuitableForColony(currentTile) && !world.colony.hasPosition())
				{
					qAnt.createColony(world);
					return;
				}

				qAnt.moveWhileSearchingForColony(world);
			}
		};

		public abstract void perform(final QueenAnt qAnt, final World world);
	}
}
