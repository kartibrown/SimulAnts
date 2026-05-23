package com.kartibrown.simulants.ant;

import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;

import com.kartibrown.simulants.Position;
import com.kartibrown.simulants.world.Colony;
import com.kartibrown.simulants.world.World;

public abstract class Ant {
    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    protected final int id;

    protected String name;

    protected Position pos;

    protected int energy, health, damage;

    protected final SplittableRandom rng;

    public Ant(final String name, final Position pos, final SplittableRandom rng) {
        // getAndIncrement so that queen gets id 0 and first worker gets id 1
        this.id = NEXT_ID.getAndIncrement();

        this.name = name;
        this.energy = this.health = 100;
        this.damage = 0;

        this.pos = pos;

        this.rng = rng;
    }

    public abstract void update(final World world);

    public abstract void move(final World world);

    public final void goTo(final Colony colony, final World world) {
        goTo(colony.getPosition(), world);
    }

    public final void goTo(final QueenAnt qAnt, final World world) {
        goTo(qAnt.getPosition(), world);
    }

    public final void goTo(final Position target, final World world) {
        final int dx = Integer.compare(target.getX(), this.pos.getX());
        final int dy = Integer.compare(target.getY(), this.pos.getY());

        this.pos.setX(this.pos.getX() + dx);
        this.pos.setY(this.pos.getY() + dy);
    }

    /*
     * GETTERS & SETTERS
     */

    public final boolean isNear(final Ant ant, final int radius) {
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
        this.energy = energy;
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
        this.health = health;
    }

    protected boolean isTierd() {
        return getEnergy() <= 20;
    }

    protected boolean isRested() {
        return getEnergy() > 95;
    }
}
