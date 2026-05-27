package com.kartibrown.simulants.server.state;

import java.util.List;

public record WorldState(
        int sizeX,
        int sizeY,
        List<AntState> ants,
        List<TileState> tiles
)
{
}
