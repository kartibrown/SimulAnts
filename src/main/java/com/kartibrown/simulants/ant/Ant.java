package com.kartibrown.simulants.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;

import com.kartibrown.simulants.Position;
import com.kartibrown.simulants.world.Colony;
import com.kartibrown.simulants.world.World;

public abstract class Ant {
    private static final int RANDOM_MOVE_CHANCE = 45;
    private static final int GO_CORRECT_MOVE_CHANCE = 90;

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    protected final int id;

    protected String name;

    protected Position pos;

    protected int energy, health, damage;
    private int moveXDirection, moveYDirection;

    /**
     * Represents how hungry the ant is.<br>
     * <p>
     * 0   = starving<br>
     * 100 = fully saturated
     */
    protected int hunger;

    protected final SplittableRandom rng;

    public Ant(final String name, final Position pos, final SplittableRandom rng) {
        // getAndIncrement so that queen gets id 0 and first worker gets id 1
        this.id = NEXT_ID.getAndIncrement();

        this.name = name;
        this.energy = this.health = 100;
        this.hunger = 100;
        this.damage = 0;

        this.pos = pos;
        this.moveXDirection = rng.nextInt(-1, 2);
        this.moveYDirection = rng.nextInt(-1, 2);

        this.rng = rng;
    }

    public abstract void update(final World world);


    public abstract boolean canMove(final World world);

    public final void move(final World world) {
        //if (rng.nextInt(100) >= RANDOM_MOVE_CHANCE)
        //    return;

        if ((moveXDirection == 0 && moveYDirection == 0) || rng.nextInt(100) < 25) {
            moveXDirection = rng.nextInt(-1, 2);
            moveYDirection = rng.nextInt(-1, 2);
        }

        if (rng.nextInt(100) < 15) {
            moveXDirection = rng.nextInt(-1, 2);
        }

        if (rng.nextInt(100) < 15) {
            moveYDirection = rng.nextInt(-1, 2);
        }

        pos.setX(pos.getX() + moveXDirection);
        pos.setY(pos.getY() + moveYDirection);

        pos.setX(Math.clamp(pos.getX(), 0, world.getSizeX() - 1));
        pos.setY(Math.clamp(pos.getY(), 0, world.getSizeY() - 1));

        if (pos.getX() == 0 || pos.getX() == world.getSizeX() - 1) {
            moveXDirection *= -1;
        }

        if (pos.getY() == 0 || pos.getY() == world.getSizeY() - 1) {
            moveYDirection *= -1;
        }
    }

    /**
     * Goes straight towards a target
     * @param target the target position the ant is gonna walk straight towards
     */
    public final void goTo(final Position target, final World world) {
        int dx = Integer.compare(target.getX(), this.pos.getX());
        int dy = Integer.compare(target.getY(), this.pos.getY());

        this.pos.setX(this.pos.getX() + dx);
        this.pos.setY(this.pos.getY() + dy);
    }

    /**
     * Goes to a target position and chooses randomly what tiles gets closer to that target<br>
     * <br>
     * If Ant can't find any tiles that closes the distance towards target it will
     * go straight towards the target once
     * @param queen goes to the queen's position
     */
    public final void goToRandomly(final QueenAnt queen, final World world){
        goToRandomly(queen.getPosition(), world);
    }

    /**
     * Goes to a target position and chooses randomly what tiles gets closer to that target<br>
     * <br>
     * If Ant can't find any tiles that closes the distance towards target it will
     * go straight towards the target once
     * @param colony goes to the colony's position
     */
    public final void goToRandomly(final Colony colony, final World world) {
        goToRandomly(colony.getPosition(), world);
    }

    /**
     * Goes to a target position and chooses randomly what tiles gets closer to that target<br>
     * <br>
     * If Ant can't find any tiles that closes the distance towards target it will
     * go straight towards the target once
     * @param target the target position the ant is gonna walk towards
     */
    public final void goToRandomly(final Position target, final World world) {
        //if (rng.nextInt(100) >= DIRECT_MOVE_CHANCE)
        //  return;

        //-TEST

        if (getDistanceFrom(target) <= 1) {
            // if target is one block away then go into the target
            this.pos.setX(target.getX());
            this.pos.setY(target.getY());
            return;
        }

        // find all moves that closes the distance to target
        final List<Position> bestMoves = getMovesReducingDistance(target, world);

        // if not empty (could not find any best moves)
        // and rng because realistic moving
        if (!bestMoves.isEmpty() && rng.nextInt(100) <= GO_CORRECT_MOVE_CHANCE) {

            // choose one of those moves
            final Position move = bestMoves.get(rng.nextInt(bestMoves.size()));

            this.pos.setX(move.getX());
            this.pos.setY(move.getY());
        } else {
            // JUST RANDOMLY
            final List<Position> neighbours = world.getNeighbourTiles(this.pos);
            final Position move = neighbours.get(rng.nextInt(neighbours.size()));

            this.pos.setX(move.getX());
            this.pos.setY(move.getY());
        }
    }

    /*
     * GETTERS & SETTERS
     */

    /**
     *
     * @param target the target to get the distance to
     * @param world  in what world
     * @return returns a List of Position which decreases the distance to target
     */
    protected final List<Position> getMovesReducingDistance(final Position target, final World world) {
        final List<Position> neighbourTiles = world.getNeighbourTiles(getPosition());

        final int baseDistance = getDistanceFrom(target);

        final List<Position> moves = new ArrayList<>();

        for (final Position neighbour : neighbourTiles)
            if (getDistanceFrom(neighbour, target) < baseDistance)
                moves.add(neighbour);

        return moves;
    }

    /**
     * This is the manhattan-distance, not the fly way distance
     * @return the manhattan-distance between two positions
     */
    protected final int getDistanceFrom(final Position pos1, final Position pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY());
    }

    /**
     * This is the manhattan-distance, not the fly way distance
     * @param position which position you want to get the distance to
     * @return returns the distance between this objects position and the positions you pass
     */
    protected final int getDistanceFrom(final Position position) {
        return Math.abs(getPosition().getX() - position.getX()) +
                Math.abs(getPosition().getY() - position.getY());
    }

    protected final boolean isNear(final Ant ant, final int radius) {
        int dx = Math.abs(this.pos.getX() - ant.getPosition().getX());
        int dy = Math.abs(this.pos.getY() - ant.getPosition().getY());

        return dx <= radius && dy <= radius;
    }

    public final int getId() {
        return this.id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final int getEnergy() {
        return energy;
    }

    public final void setEnergy(final int energy) {
        this.energy = Math.clamp(energy, 0, 100);
    }

    public final void setPosition(final Position pos) {
        this.pos = pos;
    }

    public final Position getPosition() {
        return pos;
    }

    public final int getHealth() {
        return health;
    }

    public final void setHealth(final int health) {
        this.health = Math.clamp(health, 0, 100);
    }

    public final int getHunger() {
        return hunger;
    }

    public final void setHunger(final int hunger) {
        this.hunger = Math.clamp(hunger, 0, 100);
    }

    public final boolean isHungry() {
        return hunger < 20;
    }

    public final boolean isHungerFull() {
        return hunger > 95;
    }

    protected boolean isTired() {
        return getEnergy() <= 20;
    }

    protected boolean isExhausted() {
        return getEnergy() <= 0;
    }

    protected boolean isRested() {
        return getEnergy() > 95;
    }
}
