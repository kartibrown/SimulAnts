package com.kartibrown.simulants.world;

import java.util.SplittableRandom;

import com.kartibrown.simulants.item.Food;

public class Tile
{
	public static final double MAX_PHEROMONES = 100;

	// Maybe need this later if an apple tree randomly drops an apple
	private final SplittableRandom rng;

	private Food food;
	private boolean isOpen;

	private double homePheromones, foodPheromones;

	public Tile(final SplittableRandom rng)
	{
		this.rng = rng;

		if (rng.nextInt(100) > 90)
			addFood(new Food(rng.nextInt(5)));

		isOpen = rng.nextInt(100) > 40;

		homePheromones = foodPheromones = 0;
	}

	public final void addFood(final Food food)
	{ this.food = food; }

	public final boolean hasFood()
	{ return food != null && food.getAmount() > 0; }

	public final Food peekFood()
	{ return food; }

	public final Food takeFood(final int amount)
	{
		if (food == null || amount <= 0)
			return null;

		final int amountToTake = Math.min(amount, food.getAmount());

		food.setAmount(food.getAmount() - amountToTake);

		if (food.getAmount() <= 0)
			food = null;

		return new Food(amountToTake);
	}

	/*
	 * GETTERS & SETTERS
	 */

	public final void setHomePheromones(final double homePheromones)
	{ this.homePheromones = Math.max(0, Math.min(homePheromones, MAX_PHEROMONES)); }

	public final void setFoodPheromones(final double foodPheromones)
	{ this.foodPheromones = Math.max(0, Math.min(foodPheromones, MAX_PHEROMONES)); }

	public final double getFoodPheromones()
	{ return foodPheromones; }

	public final double getHomePheromones()
	{ return homePheromones; }

	public final boolean isCovered()
	{ return !isOpen; }

	public final boolean isOpen()
	{ return isOpen; }

	public final int getColonySuitabilityScore()
	{
		int score = 0;

		if (hasFood())
			score += 5;

		if (isCovered())
			score += 3;

		if (isOpen())
			score -= 2;

		return score;
	}
}
