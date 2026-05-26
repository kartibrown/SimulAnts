const canvas = document.getElementById("world");
const ctx = canvas.getContext("2d");

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}

resizeCanvas();
window.addEventListener("resize", resizeCanvas);

let cameraX = 0;
let cameraY = 0;

const TILE_SIZE = 20;

const renderAnts = new Map();
const ANT_RENDER_PADDING = TILE_SIZE;
let latestWorldState = null;

function renderWorld(worldState) {
    latestWorldState = worldState;
}

function renderLoop() {
    requestAnimationFrame(renderLoop);

    if (!latestWorldState) {
        return;
    }

    ctx.clearRect(0, 0, canvas.width, canvas.height);

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
        renderAnt.renderX += (ant.x - renderAnt.renderX) * 0.10;
        renderAnt.renderY += (ant.y - renderAnt.renderY) * 0.10;

        const screenX = (renderAnt.renderX * TILE_SIZE) - cameraX;
        const screenY = (renderAnt.renderY * TILE_SIZE) - cameraY;

        const isVisible =
            screenX + TILE_SIZE >= -ANT_RENDER_PADDING &&
            screenX <= canvas.width + ANT_RENDER_PADDING &&
            screenY + TILE_SIZE >= -ANT_RENDER_PADDING &&
            screenY <= canvas.height + ANT_RENDER_PADDING;

        if (!isVisible) {
            continue;
        }

        ctx.fillStyle = ant.type === "QUEEN" ? "#ca1010" : "#b64747";

        ctx.fillRect(
            screenX,
            screenY,
            TILE_SIZE,
            TILE_SIZE
        );
    }
}

renderLoop();