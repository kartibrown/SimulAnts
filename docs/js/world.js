const TILE_SIZE = 20;

function renderWorld(worldState) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (const ant of worldState.ants) {
        ctx.fillStyle = ant.type === "QUEEN" ? "#ca1010" : "#b64747";

        ctx.fillRect(
            ant.x * TILE_SIZE,
            ant.y * TILE_SIZE,
            TILE_SIZE,
            TILE_SIZE
        );
    }
}