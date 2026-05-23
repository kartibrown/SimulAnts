const canvas = document.getElementById("world");
const ctx = canvas.getContext("2d");

canvas.width = 800;
canvas.height = 600;

const TILE_SIZE = 20;

const renderAnts = new Map();

function renderWorld(worldState) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (const ant of worldState.ants) {
        const id = ant.id;

        let renderAnt = renderAnts.get(id);

        if (!renderAnt) {
            renderAnt = {
                renderX: ant.x,
                renderY: ant.y,
            };
            renderAnts.set(id, renderAnt);
        }

        // lerp the render position towards the actual position for smooth movement
        renderAnt.renderX += (ant.x - renderAnt.renderX) * 0.15;
        renderAnt.renderY += (ant.y - renderAnt.renderY) * 0.15;

        ctx.fillStyle = ant.type === "QUEEN" ? "#ca1010" : "#b64747";

        ctx.fillRect(
            renderAnt.renderX * TILE_SIZE,
            renderAnt.renderY * TILE_SIZE,
            TILE_SIZE,
            TILE_SIZE
        );
    }
}