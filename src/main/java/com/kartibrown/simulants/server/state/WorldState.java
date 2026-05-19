package com.kartibrown.simulants.server.state;

import java.util.List;

public record WorldState(
        List<AntState> ants
)
{
}
