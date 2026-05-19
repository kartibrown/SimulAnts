package com.kartibrown.simulants.ant;

import java.util.SplittableRandom;

import com.kartibrown.simulants.Position;
import com.kartibrown.simulants.world.Tile;
import com.kartibrown.simulants.world.World;

public final class QueenAnt extends Ant
{
	private int baseSpawnCooldown;
	private int spawnTimer;
	private int timePenalty;

	private int dirtyness;

	private final int energyCostToSpawn;

	private boolean hasSpawnedFirstAnt;

	private Task task;

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

				if (qAnt.isReadyForBirth() && world.colony.hasEnoughFood()
						|| qAnt.getEnergy() > qAnt.getEnergyCostToSpawn() && !qAnt.getHasSpawnedFirstAnt())
				{
					int calc = (world.colony.hasEnoughFood()) ? 0 : -qAnt.getEnergyCostToSpawn();

					qAnt.setEnergy(qAnt.getEnergy() + calc);

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

				if (qAnt.isSuitableForColony(currentTile) && world.colony.getPosition() == null)
				{
					System.out.println(qAnt.getName() + " found a spot for her colony");

					int x = qAnt.getPosition().getX();
					int y = qAnt.getPosition().getY();

					world.colony.setPosition(x, y);
					world.getTile(x, y).setHomePheromones(100);

					qAnt.setTask(Task.SPAWN_WORKER);
				}
				else
				{
					qAnt.move(world);
				}
			}
		};

		public abstract void perform(final QueenAnt qAnt, final World world);
	}

	public QueenAnt(
			final String name, final int x, final int y, final SplittableRandom rng, final int energyCostToSpawn
	)
	{
		super(name, new Position(x, y), rng);

		this.baseSpawnCooldown = 100;
		this.spawnTimer = 100;
		this.timePenalty = 0;

		this.dirtyness = 0;

		this.energyCostToSpawn = energyCostToSpawn;

		this.hasSpawnedFirstAnt = false;

		this.task = Task.FIND_COLONY;
	}

	public final WorkerAnt spawnWorker(final String name)
	{

		System.out.println(this.name + " spawned " + name);
		dirtyness += 2;
		return new WorkerAnt(name, pos, rng.split()); // Namn senare kanske
	}

	@Override
	public final void update(final World world)
	{
		task.perform(this, world);
		spawnTimer--;
		energy++;
	}

	@Override
	public final void move(final World world)
	{
		if (world.colony.getPosition() == null)
		{
			final int xMove = rng.nextBoolean() ? 1 : -1;
			final int yMove = rng.nextBoolean() ? 1 : -1;

			pos.setX(pos.getX() + (rng.nextBoolean() ? xMove : 0));
			pos.setY(pos.getY() + (rng.nextBoolean() ? yMove : 0));

			pos.setX(Math.max(0, Math.min(pos.getX(), world.getSizeX() - 1)));
			pos.setY(Math.max(0, Math.min(pos.getY(), world.getSizeY() - 1)));

			dirtyness += 1;

			System.out.println(name + " moved to X:" + pos.getX() + " Y:" + pos.getY());
		}
	}

	public final void resetSpawnTimer()
	{ this.spawnTimer = this.baseSpawnCooldown + this.timePenalty; }

	/*
	 * GETTERS & SETTERS
	 */

	public final void setHasSpawnedFirstAnt(final boolean hasSpawnedFirstAnt)
	{ this.hasSpawnedFirstAnt = hasSpawnedFirstAnt; }

	public final boolean getHasSpawnedFirstAnt()
	{ return this.hasSpawnedFirstAnt; }

	public final boolean isSuitableForColony(final Tile tile)
	{ return tile.getColonySuitabilityScore() > 5; }

	public final boolean isReadyForBirth()
	{ return this.spawnTimer <= 0; }

	public final int getBaseSpawnCooldown()
	{ return baseSpawnCooldown; }

	public final int getTimePenalty()
	{ return timePenalty; }

	public final void setTask(final Task task)
	{ this.task = task; }

	public final int getEnergyCostToSpawn()
	{ return energyCostToSpawn; }

	public final boolean isDirty()
	{ return dirtyness > 60; }

	public final void setDirtyness(final int dirtyness)
	{ this.dirtyness = dirtyness; }

	public final int getDirtyness()
	{ return dirtyness; }
}
