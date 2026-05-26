

const stopButton = document.getElementById('stopButton');
const menuScreen = document.getElementById("menuScreen");
const simulationScreen = document.getElementById("simulationScreen");
const startButton = document.getElementById("startButton");
const darkModeToggle = document.getElementById("darkModeButton");

let socket = null;
let heartbeatTimer = null;

const HEARTBEAT_INTERVAL = 10000; // 10 seconds
const ACTIVE_MSG = "HEARTBEAT";
const STOP_MSG = "STOP";

darkModeToggle.addEventListener("click", () => {
    console.log("Toggling dark mode...");
    document.documentElement.classList.toggle("white-mode");
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
            socket.send(JSON.stringify({
                type: ACTIVE_MSG
            }));
        }, HEARTBEAT_INTERVAL);
    };

    socket.onmessage = (event) => {
        worldState = JSON.parse(event.data);
        renderWorld(worldState);
    };

    document.addEventListener("visibilitychange", () => {
        if (document.hidden) {
            console.log("Tab hidden, pausing simulation...");
            socket.send("PAUSE");
        } else {
            console.log("Tab visible, resuming simulation...");
            socket.send("RESUME");
        }
    });
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

        socket.close();
    }

    socket = null;
}