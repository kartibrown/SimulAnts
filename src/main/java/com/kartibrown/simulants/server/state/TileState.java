package com.kartibrown.simulants.server.state;

public record TileState(
        int x,
        int y,
        int food,
        double homePheromones,
        double foodPheromones
)
{
}
