const canvas = document.getElementById("world");
const ctx = canvas.getContext("2d");
const antRenderLimitInput = document.getElementById("antRenderLimit");
const antRenderCount = document.getElementById("antRenderCount");

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}

resizeCanvas();
window.addEventListener("resize", resizeCanvas);

let cameraX = 0;
let cameraY = 0;

const TILE_SIZE = 20;
const MAX_PHEROMONES = 100;

const renderAnts = new Map();
const ANT_RENDER_PADDING = TILE_SIZE;
let latestWorldState = null;
let antRenderLimit = Number(antRenderLimitInput?.value ?? 1000);

if (antRenderLimitInput) {
    antRenderLimitInput.addEventListener("input", () => {
        antRenderLimit = Number(antRenderLimitInput.value);
        updateAntRenderCount();
    });
}

function renderWorld(worldState) {
    latestWorldState = worldState;
}

function renderLoop() {
    requestAnimationFrame(renderLoop);

    if (!latestWorldState) {
        return;
    }

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = "#171717";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.save();
    ctx.scale(zoom, zoom);

    renderTiles(latestWorldState);

    let renderedAnts = 0;

    for (const ant of latestWorldState.ants) {
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
        renderAnt.renderX += (ant.x - renderAnt.renderX) * 0.16;
        renderAnt.renderY += (ant.y - renderAnt.renderY) * 0.16;

        const screenX = (renderAnt.renderX * TILE_SIZE) - cameraX;
        const screenY = (renderAnt.renderY * TILE_SIZE) - cameraY;

            const isVisible =
            screenX + TILE_SIZE >= -ANT_RENDER_PADDING &&
            screenX <= (canvas.width / zoom) + ANT_RENDER_PADDING &&
            screenY + TILE_SIZE >= -ANT_RENDER_PADDING &&
            screenY <= (canvas.height / zoom) + ANT_RENDER_PADDING;

        if (!isVisible) {
            continue;
        }

        if (renderedAnts >= antRenderLimit) {
            continue;
        }

        ctx.fillStyle = ant.type === "QUEEN" ? "#ca1010" : "#b64747";

        ctx.fillRect(
            screenX,
            screenY,
            TILE_SIZE,
            TILE_SIZE
        );

        renderedAnts++;
    }
    
    ctx.restore();

    updateAntRenderCount();
}

function renderTiles(worldState) {
    if (!worldState.tiles) {
        return;
    }

    for (const tile of worldState.tiles) {
        const screenX = (tile.x * TILE_SIZE) - cameraX;
        const screenY = (tile.y * TILE_SIZE) - cameraY;

        const isVisible =
            screenX + TILE_SIZE >= 0 &&
            screenX <= (canvas.width / zoom) &&
            screenY + TILE_SIZE >= 0 &&
            screenY <= (canvas.height / zoom);

        if (!isVisible) {
            continue;
        }

        if (tile.homePheromones > 0) {
            const alpha = Math.min(tile.homePheromones / MAX_PHEROMONES, 1) * 0.45;
            ctx.fillStyle = `rgba(54, 126, 255, ${alpha})`;
            ctx.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
        }

        if (tile.foodPheromones > 0) {
            const alpha = Math.sqrt(Math.min(tile.foodPheromones / MAX_PHEROMONES, 1)) * 0.55;
            ctx.fillStyle = `rgba(65, 210, 92, ${alpha})`;
            ctx.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
        }

        if (tile.food > 0) {
            const radius = Math.min(3 + tile.food, TILE_SIZE / 2 - 2);
            ctx.fillStyle = "#f0a02f";
            ctx.beginPath();
            ctx.arc(
                screenX + TILE_SIZE / 2,
                screenY + TILE_SIZE / 2,
                radius,
                0,
                Math.PI * 2
            );
            ctx.fill();
        }
    }
}

function updateAntRenderCount() {
    if (!antRenderCount) {
        return;
    }

    const totalAnts = latestWorldState?.ants?.length ?? 0;
    antRenderCount.textContent = `${antRenderLimit}/${totalAnts}`;
}

renderLoop();
