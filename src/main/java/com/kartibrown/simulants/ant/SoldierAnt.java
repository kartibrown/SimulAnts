package com.kartibrown.simulants.ant;

import java.util.SplittableRandom;

import com.kartibrown.simulants.world.World;

public final class SoldierAnt extends WorkerAnt
{
	private static final int MAX_CARRY = 20;
	private static final int STAY_RADIUS = 5;

	Task task;

	private enum Task
	{
		PATROL
		{
			@Override
			public final void perform(final SoldierAnt ant, final World world)
			{
				ant.move(world);;
			}
		};

		public abstract void perform(final SoldierAnt ant, final World world);
	}

	public SoldierAnt(final String name, final int x, final int y, final SplittableRandom rng)
	{
		super(name, x, y, rng);

		this.damage = 4;
	}

	@Override
	public void update(final World world)
	{
		task.perform(this, world);

	}

	// FIX
	// Solider Ants often Patrols near the base and helps out to carry very
	// heavy item/entities
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
}
