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

public class WorkerAnt extends Ant {
    private static final int CLEAN_RADIUS = 3;
    private static final int MAX_CARRY = 10;
    private static final int FOOD_PHEROMONE_FOLLOW_CHANCE = 80;
    private static final int WORK_ENERGY_COST_CHANCE = 15;
    private static final int HUNGER_DECAY_CHANCE = 10;
    private static final int STARVING_ENERGY_COST_CHANCE = 35;
    private static final int FULL_HUNGER_ENERGY_GAIN_CHANCE = 20;
    private static final int MIN_DISTANCE_FROM_HOME_TO_DROP_FOOD_PHEROMONES = 4;
    private static final int FULL_FOOD_PHEROMONE_DISTANCE_FROM_HOME = 25;
    private static final double MIN_FOOD_PHEROMONE_DROP_NEAR_HOME = 24.0;
    private static final double FOOD_PHEROMONE_UPHILL_MARGIN = 0.25;
    private static final double MIN_FOOD_PHEROMONE_TO_FOLLOW = 1.0;
    private static final double FOOD_PHEROMONE_DROP_PER_FOOD = 8.0;

    protected final List<Item> inventory;

    private Task task;
    private Position previousFoodPheromonePosition;

    public WorkerAnt(final String name, final Position pos, final SplittableRandom rng) {
        super(name, pos, rng);

        task = Task.REST;
        inventory = new ArrayList<>();
        this.damage = 2;
    }

    public WorkerAnt(final String name, final int x, final int y, final SplittableRandom rng) {
        super(name, new Position(x, y), rng);

        task = Task.REST;
        inventory = new ArrayList<>();
        this.damage = 2;
    }

    @Override
    public void update(final World world) {
        final Task startingTask = task;

        task.perform(this, world);

        updateHunger();

        if (startingTask != Task.REST && rng.nextInt(100) < WORK_ENERGY_COST_CHANCE)
            setEnergy(energy - 1);
    }

    private void updateHunger() {
        if (hunger > 80 && rng.nextInt(100) < FULL_HUNGER_ENERGY_GAIN_CHANCE) {
            setEnergy(energy + 1);
        } else if (hunger < 20 && rng.nextInt(100) < STARVING_ENERGY_COST_CHANCE) {
            setEnergy(energy - 1);
        }

        if (hunger <= 0) {
            setHealth(health - rng.nextInt(2));
        }

        if (rng.nextInt(100) < HUNGER_DECAY_CHANCE) {
            setHunger(hunger - 1);
        }
    }

    protected final void storeFoodAt(final Colony colony) {
        colony.storeAll(inventory);

        inventory.clear();
    }

    protected final void pickUpFoodFrom(final Tile tile) {
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

    protected final void eatFoodFrom(final Tile tile) {
        if (!tile.hasFood() || isHungerFull())
            return;

        final Food eatenFood = tile.takeFood(1);

        if (eatenFood == null)
            return;

        setHunger(getHunger() + 30);
    }

    public final void clean(final QueenAnt qAnt) {
        qAnt.setDirtiness(qAnt.getDirtiness() - 5);
    }

    /*
     * GETTERS & SETTERS
     */

    private final int getCurrentInventoryWeight() {
        int total = 0;

        for (final Item item : inventory)
            total += item.getWeight();

        return total;
    }

    private final int getRemainingInventoryCapacity() {
        return MAX_CARRY - getCurrentInventoryWeight();
    }

    private Task getTask() {
        return task;
    }

    private void setTask(final Task task) {
        this.task = task;
    }

    private boolean canCarryMore() {
        return getRemainingInventoryCapacity() > 0;
    }

    /*
     * TASKS
     */

    private enum Task {
        EAT {
            @Override
            void perform(final WorkerAnt ant, final World world) {
                if (ant.isHungerFull()) {
                    ant.setTask(Task.REST);
                } else {
                    if (world.colony.getStoredFood() > 0) {
                        world.colony.consumeFood(1);
                        ant.setHunger(ant.getHunger() + 30);
                    } else {
                        ant.setTask(Task.FIND_FOOD);
                    }
                }
            }
        },
        FIND_FOOD {
            @Override
            final void perform(final WorkerAnt ant, final World world) {
                final boolean colonyHasFood = world.colony.getStoredFood() > 0;
                final boolean emergencyForage = ant.isHungry() && !colonyHasFood;
                final boolean carryingFood = ant.getCurrentInventoryWeight() > 0;

                if (ant.isHungry() && colonyHasFood) {
                    ant.setTask(Task.RETURN_HOME);
                    return;
                }

                if (carryingFood && (ant.isHungry() || ant.isTired())) {
                    ant.setTask(Task.RETURN_HOME);
                    return;
                }

                if (ant.isExhausted()) {
                    ant.setTask(Task.RETURN_HOME);
                    return;
                }

                if (ant.isTired() && !emergencyForage) {
                    ant.setTask(Task.RETURN_HOME);
                    return;
                }

                if (!emergencyForage && world.getQueen().isDirty()) {
                    ant.setTask(Task.CLEAN_QUEEN);
                    world.log(world.getQueen().getName() + " needs cleaning!");
                    return;
                }

                final int x = ant.getPosition().getX();
                final int y = ant.getPosition().getY();

                final Tile currentTile = world.getGrid()[x][y];

                if (currentTile.hasFood()) {
                    currentTile.addFoodPheromones(currentTile.getFoodAmount() * FOOD_PHEROMONE_DROP_PER_FOOD);

                    if (ant.isHungry()) {
                        ant.eatFoodFrom(currentTile);

                        if (!ant.isHungry()) {
                            ant.pickUpFoodFrom(currentTile);

                            if (ant.getCurrentInventoryWeight() > 0) {
                                ant.setTask(Task.RETURN_HOME);
                            }
                        }
                    } else {
                        ant.pickUpFoodFrom(currentTile);

                        if (ant.getCurrentInventoryWeight() > 0) {
                            ant.setTask(Task.RETURN_HOME);
                        }
                    }
                    return;
                }

                if (!ant.followFoodPheromones(world)) {
                    ant.previousFoodPheromonePosition = null;
                    ant.move(world);
                }
            }
        },
        RETURN_HOME {
            @Override
            final void perform(final WorkerAnt ant, final World world) {
                if (ant.getCurrentInventoryWeight() > 0) {
                    ant.dropFoodPheromones(world);
                }

                if (ant.getPosition().equals(world.colony.getPosition())) {
                    if (ant.isHungry()) {
                        if (world.colony.getStoredFood() > 0) {
                            ant.setTask(Task.EAT);
                        } else {
                            ant.setTask(Task.REST);
                        }
                    } else {
                        ant.setTask(Task.REST);
                    }
                } else {
                    ant.goTo(world.colony, world);
                }
            }
        },
        CLEAN_QUEEN {
            @Override
            final void perform(final WorkerAnt ant, final World world) {
                if (ant.isTired()) {
                    ant.setTask(Task.REST);
                    return;
                }

                final QueenAnt qAnt = world.getQueen();

                if (qAnt.getDirtiness() <= 0) {
                    world.log(ant.getName() + " has finished cleaning " + qAnt.getName());
                    qAnt.setDirtiness(0);
                    ant.setTask(REST);
                    return;
                }

                if (ant.isNear(qAnt, CLEAN_RADIUS) && qAnt.getDirtiness() > 0) {
                    ant.clean(world.getQueen());
                } else {
                    ant.goTo(world.getQueen(), world);
                }
            }
        },
        REST {
            @Override
            final void perform(final WorkerAnt ant, final World world) {
                if (ant.getCurrentInventoryWeight() > 0) {
                    ant.storeFoodAt(world.colony);
                    world.log(ant.getName() + " stored food. Colony food: " + world.colony.getStoredFood());
                }

                if (ant.isHungry()) {
                    if (world.colony.getStoredFood() > 0) {
                        ant.setTask(Task.EAT);
                    } else if (!ant.isRested()) {
                        ant.setEnergy(ant.getEnergy() + 6);
                    } else {
                        ant.setTask(Task.FIND_FOOD);
                    }
                    return;
                }

                if (ant.isRested()) {
                    ant.setTask(Task.FIND_FOOD);
                } else {
                    ant.setEnergy(ant.getEnergy() + 2);
                }
            }
        };

        abstract void perform(final WorkerAnt ant, final World world);
    }

    private boolean followFoodPheromones(final World world) {
        if (rng.nextInt(100) >= FOOD_PHEROMONE_FOLLOW_CHANCE)
            return false;

        final Position currentPosition = new Position(getPosition().getX(), getPosition().getY());
        Position bestPosition = null;
        double bestPheromones = Math.max(
                MIN_FOOD_PHEROMONE_TO_FOLLOW,
                world.getTile(getPosition()).getFoodPheromones() + FOOD_PHEROMONE_UPHILL_MARGIN
        );

        final int x = getPosition().getX();
        final int y = getPosition().getY();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;

                final int nextX = x + dx;
                final int nextY = y + dy;

                if (nextX < 0 || nextX >= world.getSizeX() || nextY < 0 || nextY >= world.getSizeY())
                    continue;

                if (previousFoodPheromonePosition != null
                        && nextX == previousFoodPheromonePosition.getX()
                        && nextY == previousFoodPheromonePosition.getY()) {
                    continue;
                }

                final double pheromones = world.getTile(nextX, nextY).getFoodPheromones();

                if (pheromones > bestPheromones) {
                    bestPheromones = pheromones;
                    bestPosition = new Position(nextX, nextY);
                }
            }
        }

        if (bestPosition == null)
            return false;

        previousFoodPheromonePosition = currentPosition;
        goTo(bestPosition, world);
        return true;
    }

    private void dropFoodPheromones(final World world) {
        if (!world.colony.hasPosition())
            return;

        final int distanceFromHome = getDistanceFrom(world.colony.getPosition());
        final double baseAmount = Math.max(1, getCurrentInventoryAmount()) * FOOD_PHEROMONE_DROP_PER_FOOD;

        if (distanceFromHome < MIN_DISTANCE_FROM_HOME_TO_DROP_FOOD_PHEROMONES
                && baseAmount < MIN_FOOD_PHEROMONE_DROP_NEAR_HOME) {
            return;
        }

        final double distanceScale = Math.min(
                1.0,
                Math.max(
                        0.35,
                        (double) distanceFromHome / FULL_FOOD_PHEROMONE_DISTANCE_FROM_HOME
                )
        );
        final double amount = baseAmount * distanceScale;

        world.getTile(getPosition()).addFoodPheromones(amount);
    }

    private int getDistanceFrom(final Position position) {
        return Math.max(
                Math.abs(getPosition().getX() - position.getX()),
                Math.abs(getPosition().getY() - position.getY())
        );
    }

    private int getCurrentInventoryAmount() {
        int total = 0;

        for (final Item item : inventory)
            total += item.getAmount();

        return total;
    }
}
