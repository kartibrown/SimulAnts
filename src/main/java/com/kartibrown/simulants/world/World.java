package com.kartibrown.simulants.world;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.kartibrown.simulants.Position;
import com.kartibrown.simulants.ant.QueenAnt;
import com.kartibrown.simulants.ant.WorkerAnt;
import com.kartibrown.simulants.item.Food;
import com.kartibrown.simulants.server.state.AntState;
import com.kartibrown.simulants.server.state.WorldState;

public final class World {
    public final Colony colony;
    private final QueenAnt queen;
    private final List<WorkerAnt> ants;
    private int foodSpawnTimer;
    private final int baseFoodSpawnCooldown;

    private final Tile[][] grid;
    private final int sizeX, sizeY;

    private final SplittableRandom rng;

    private final ScheduledExecutorService scheduler;
    private static final int TPS = 20;
    private static final long TICK_MS = 1000 / TPS; // 50 ms

    private volatile boolean loop;

    public World() {
        rng = new SplittableRandom();
        colony = new Colony();

        sizeX = 40;
        sizeY = 30;
        grid = new Tile[sizeX][sizeY];

        for (int x = 0; x < grid.length; x++)
            for (int y = 0; y < grid[x].length; y++)
                grid[x][y] = new Tile(rng.split());

        queen = new QueenAnt("The Queen",
                getCenterX() / 2, getCenterY() / 2,
                rng.split(), 60);

        ants = new ArrayList<>();

        foodSpawnTimer = 0;
        baseFoodSpawnCooldown = 200;

        // Better for fixed rate than Thread.sleep()
        scheduler = Executors.newSingleThreadScheduledExecutor();

        loop = true;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::update, 0,
                TICK_MS, TimeUnit.MILLISECONDS); // 20 TPS
    }

    private void update() {
        if(!loop){
            scheduler.shutdown();
            return;
        }

        updateWorld();
        queen.update(this);

        for (final WorkerAnt ant : ants)
            ant.update(this);
    }

    public void stop() {
        loop = false;
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
    }

    private void updatePheromones() {
        double[][] nextHome = new double[sizeX][sizeY];

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                final Tile tile = getTile(x, y);

                double value = tile.getHomePheromones();

                value *= 0.995;

                nextHome[x][y] += value * 0.90;

                for (final Position p : getNeighbours(x, y)) {
                    nextHome[p.getX()][p.getY()] += value * 0.025;
                }
            }
        }

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                getTile(x, y).setHomePheromones(nextHome[x][y]);
            }
        }
    }

    private List<Position> getNeighbours(final int x, final int y) {
        final List<Position> neighbours = new ArrayList<>();

        addIfInside(neighbours, x + 1, y);
        addIfInside(neighbours, x - 1, y);
        addIfInside(neighbours, x, y + 1);
        addIfInside(neighbours, x, y - 1);

        return neighbours;
    }

    private void addIfInside(final List<Position> list, final int x, final int y) {
        if (x >= 0 && x < sizeX && y >= 0 && y < sizeY)
            list.add(new Position(x, y));
    }

    private void addFoodToWorld() {
        final int x = rng.nextInt(sizeX);
        final int y = rng.nextInt(sizeY);

        grid[x][y].addFood(new Food(rng.nextInt(5)));
    }

    // For backend so backend don't get too much info ;-;
    public synchronized WorldState toState() {
        //                                          + 1 for the queen ofc!
        AntState[] ants = new AntState[this.ants.size() + 1];
        ants[0] = new AntState(
                this.queen.getId(),
                this.queen.getName(),
                "QUEEN",
                this.queen.getPosition().getX(),
                this.queen.getPosition().getY()
        );

        for(int i = 1; i < ants.length; i++){
            ants[i] = new AntState(
                    this.ants.get(i).getId(),
                    this.ants.get(i).getName(),
                    "WORKER",
                    this.ants.get(i).getPosition().getX(),
                    this.ants.get(i).getPosition().getY()
            );
        }

        return new WorldState(ants);
    }

    /*
     * GETTERS & SETTERS
     */

    public Tile getTile(final int x, final int y) {
        return getTile(new Position(x, y));
    }

    public Tile getTile(final Position pos) {
        return grid[pos.getX()][pos.getY()];
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
        ants.add(qAnt.spawnWorker("Worker: " + (ants.size() + 1)));
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
}
