

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

    // For debugging, "wss://api.simulants.kartibrown.com" for production
    socket = new WebSocket("ws://localhost:8080/ws");

    socket.onopen = () => {
        console.log("Connected!");

        heartbeatTimer = setInterval(() => {
            socket.send(JSON.stringify({
                type: ACTIVE_MSG
            }));
        }, HEARTBEAT_INTERVAL);
    };

    socket.onmessage = (event) => {
        console.log("Got world state:", event.data);
        renderWorld(JSON.parse(event.data));
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

        socket.close();
    }

    socket = null;
}