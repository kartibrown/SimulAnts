package com.kartibrown.simulants.item;

public final class Food extends Item
{
	private static final int WEIGHT_PER_UNIT = 2;

	public Food(int amount)
	{ this.amount = amount; }

	@Override
	public final int getWeight()
	{ return amount * WEIGHT_PER_UNIT; }

	@Override
	public final int getWeightPerUnit()
	{ return WEIGHT_PER_UNIT; }
}
