package com.kartibrown.simulants.server.state;

import com.kartibrown.simulants.Position;

import java.util.List;
import java.util.Set;

public record WorldState(
        List<AntState> ants
)
{
}
