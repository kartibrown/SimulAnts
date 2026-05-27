

const stopButton = document.getElementById('stopButton');
const menuScreen = document.getElementById("menuScreen");
const simulationScreen = document.getElementById("simulationScreen");
const startButton = document.getElementById("startButton");
const darkModeToggle = document.getElementById("darkModeButton");

let socket = null;
let heartbeatTimer = null;
let dragging = false;

const HEARTBEAT_INTERVAL = 10000; // 10 seconds
const ACTIVE_MSG = "HEARTBEAT";
const RESUME_MSG = "RESUME";
const PAUSE_MSG = "PAUSE";
const STOP_MSG = "STOP";

darkModeToggle.addEventListener("click", () => {
    console.log("Toggling dark mode...");
    document.documentElement.classList.toggle("white-mode");
});

document.addEventListener("visibilitychange", () => {
    if (socket === null || socket.readyState !== WebSocket.OPEN) {
        return;
    }

    if (document.hidden) {
        console.log("Tab hidden, pausing simulation...");
        socket.send(PAUSE_MSG);
    } else {
        console.log("Tab visible, resuming simulation...");
        socket.send(RESUME_MSG);
    }
});

document.addEventListener("mousedown", (event) => {
    if (event.target.closest("#renderControls")) {
        return;
    }

    dragging = true;
});

document.addEventListener("mouseup", () => {
    dragging = false;
});

document.addEventListener("mousemove", (event) => {
    if (!dragging) return;

    cameraX -= event.movementX;
    cameraY -= event.movementY;
});

startButton.addEventListener("click", () => {
    menuScreen.classList.add("hidden");
    simulationScreen.classList.remove("hidden");

    startSimulation();
});

stopButton.addEventListener('click', () => {
    menuScreen.classList.remove("hidden");
    simulationScreen.classList.add("hidden");

    stopSimulation();
});

function startSimulation() {
    console.log("Starting simulation...");

    if (socket !== null) {
        stopSimulation();
    }

    const WS_URLS = {
        local: "ws://localhost:8080/ws",
        production: "wss://api.kartibrown.com/ws"
    };

    const wsUrl = window.location.hostname === "localhost"
        || window.location.hostname === "127.0.0.1"
        ? WS_URLS.local
        : WS_URLS.production;

    console.log("Connecting to:", wsUrl);

    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
        console.log("Connected!");

        heartbeatTimer = setInterval(() => {
            if (socket === null || socket.readyState !== WebSocket.OPEN) {
                return;
            }

            socket.send(JSON.stringify({
                type: ACTIVE_MSG
            }));
        }, HEARTBEAT_INTERVAL);
    };

    socket.onmessage = (event) => {
        worldState = JSON.parse(event.data);
        renderWorld(worldState);
    };

    socket.onclose = (event) => {
        console.log("WebSocket closed:", event.code, event.reason);

        if (heartbeatTimer !== null) {
            clearInterval(heartbeatTimer);
            heartbeatTimer = null;
        }
    };
}

function stopSimulation() {
    console.log("Stopping simulation...");

    if (heartbeatTimer !== null) {
        clearInterval(heartbeatTimer);
        heartbeatTimer = null;
    }

    if (socket !== null && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({
            type: STOP_MSG
        }));
    }

    if (socket !== null && socket.readyState !== WebSocket.CLOSED) {
        socket.close();
    }

    socket = null;
}
