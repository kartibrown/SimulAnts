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
	// need to override the move method from Ant.class
}
