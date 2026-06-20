package com.kartibrown.simulants.world;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.kartibrown.simulants.Position;
import com.kartibrown.simulants.ant.Ant;
import com.kartibrown.simulants.ant.QueenAnt;
import com.kartibrown.simulants.ant.WorkerAnt;
import com.kartibrown.simulants.item.Food;
import com.kartibrown.simulants.server.state.AntState;
import com.kartibrown.simulants.server.state.TileState;
import com.kartibrown.simulants.server.state.WorldState;

public final class World {
    private final StringBuilder logBuffer;

    public final Colony colony;
    private final QueenAnt queen;
    private final List<WorkerAnt> ants;
    private int foodSpawnTimer;
    private int baseFoodSpawnCooldown;
    private static final int DEFAULT_BASE_FOOD_SPAWN_COOLDOWN = 160;
    private static final int EARLY_BASE_FOOD_SPAWN_COOLDOWN = 35;
    private static final double MIN_VISIBLE_PHEROMONES = 0.5;

    private final Tile[][] grid;
    private final int sizeX, sizeY;

    private final SplittableRandom rng;

    private long tick = 0;

    public World() {
        logBuffer = new StringBuilder();

        rng = new SplittableRandom();
        colony = new Colony();

        sizeX = 100;
        sizeY = 80;
        grid = new Tile[sizeX][sizeY];

        for (int x = 0; x < grid.length; x++)
            for (int y = 0; y < grid[x].length; y++) {
                grid[x][y] = new Tile(rng.split());
            }

        queen = new QueenAnt("The Queen",
                getCenterX() / 2, getCenterY() / 2,
                rng.split(), 60);

        ants = new ArrayList<>();

        foodSpawnTimer = 0;
        baseFoodSpawnCooldown = EARLY_BASE_FOOD_SPAWN_COOLDOWN;
    }

    public synchronized void update() {
        updateWorld();
        queen.update(this);

        for (final WorkerAnt ant : ants)
            ant.update(this);

        if (tick % 20 == 0)
            log("Food stored: " + colony.getStoredFood() + " | Workers: " + ants.size());
        // LOGGING
        if (!logBuffer.isEmpty() && tick % 20 == 0) {
            System.out.println(logBuffer);
            logBuffer.setLength(0);
        }

        tick++;
    }

    private void updateWorld() {
        if (colony.hasPosition()) {
            updatePheromones();
            getTile(colony.getPosition()).setHomePheromones(Tile.MAX_PHEROMONES);
        }

        if (foodSpawnTimer <= 0) {
            addFoodToWorld();
            foodSpawnTimer = baseFoodSpawnCooldown;
        } else {
            foodSpawnTimer--;
        }

        // When the colony is bigger make food spawn a little less
        if (ants.size() > 10) {
            baseFoodSpawnCooldown = DEFAULT_BASE_FOOD_SPAWN_COOLDOWN;
        }
    }

    private void updatePheromones() {
        double[][] nextHome = new double[sizeX][sizeY];
        double[][] nextFood = new double[sizeX][sizeY];

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                final Tile tile = getTile(x, y);

                double homeValue = tile.getHomePheromones();
                double foodValue = tile.getFoodPheromones();

                homeValue *= 0.995;
                foodValue *= 0.992;

                nextHome[x][y] += homeValue * 0.90;
                nextFood[x][y] += foodValue * 0.985;

                for (final Position p : getNeighbourTiles(x, y)) {
                    nextHome[p.getX()][p.getY()] += homeValue * 0.025;
                    nextFood[p.getX()][p.getY()] += foodValue * 0.00375;
                }
            }
        }

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                getTile(x, y).setHomePheromones(nextHome[x][y]);
                getTile(x, y).setFoodPheromones(nextFood[x][y]);
            }
        }
    }

    private void addFoodToWorld() {
        final int x = rng.nextInt(sizeX);
        final int y = rng.nextInt(sizeY);

        grid[x][y].addFood(new Food(rng.nextInt(1, 5)));
    }

    // For backend so backend don't get too much info ;-;
    public synchronized WorldState toState() {
        List<AntState> ants = new ArrayList<>();
        List<TileState> tiles = new ArrayList<>();

        ants.add(new AntState(
                this.queen.getId(),
                this.queen.getName(),
                "QUEEN",
                this.queen.getPosition().getX(),
                this.queen.getPosition().getY()
        ));

        for (WorkerAnt ant : this.ants) {
            ants.add(new AntState(
                    ant.getId(),
                    ant.getName(),
                    "WORKER",
                    ant.getPosition().getX(),
                    ant.getPosition().getY()
                ));
        }

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                final Tile tile = getTile(x, y);

                if (tile.getFoodAmount() <= 0
                        && tile.getHomePheromones() < MIN_VISIBLE_PHEROMONES
                        && tile.getFoodPheromones() < MIN_VISIBLE_PHEROMONES) {
                    continue;
                }

                tiles.add(new TileState(
                        x,
                        y,
                        tile.getFoodAmount(),
                        tile.getHomePheromones(),
                        tile.getFoodPheromones()
                ));
            }
        }

        return new WorldState(sizeX, sizeY, ants, tiles);
    }

    /*
     * GETTERS & SETTERS
     */

    /**
     * Gets the tiles that is north, east, south and west of the position
     * @param pos Position
     * @return North, east, south and west tiles of the position<br>
     * does not return tiles if they are outside the world
     */
    public List<Position> getNeighbourTiles(final Position pos) {
        return getNeighbourTiles(pos.getX(), pos.getY());
    }

    /**
     * Gets the tiles that is north, east, south and west of the position
     * @param x x position
     * @param y y position
     * @return North, east, south and west tiles of the position
     */
    public List<Position> getNeighbourTiles(final int x, final int y) {
        final List<Position> neighbours = new ArrayList<>();

        addIfInsideWorld(neighbours, x + 1, y);
        addIfInsideWorld(neighbours, x - 1, y);
        addIfInsideWorld(neighbours, x, y + 1);
        addIfInsideWorld(neighbours, x, y - 1);

        return neighbours;
    }

    private void addIfInsideWorld(final List<Position> list, final int x, final int y) {
        if (x >= 0 && x < sizeX && y >= 0 && y < sizeY)
            list.add(new Position(x, y));
    }

    public Tile getTile(final int x, final int y) {
        return grid[x][y];
    }

    public Tile getTile(final Position pos) {
        return getTile(pos.getX(), pos.getY());
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public QueenAnt getQueen() {
        return queen;
    }

    public List<WorkerAnt> getAnts() {
        return ants;
    }

    public void spawnWorkerFrom(final QueenAnt qAnt) {
        ants.add(qAnt.spawnWorker("Worker: " + (ants.size() + 1), this));
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getCenterX() {
        return grid.length / 2;
    }

    public int getCenterY() {
        return grid[0].length / 2;
    }

    public void log(final Object object) {
        logBuffer.append(object).append("\n");
    }

    public void log(final String message) {
        logBuffer.append(message).append('\n');
    }
}
