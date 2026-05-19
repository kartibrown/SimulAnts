const darkModeToggle = document.getElementById("darkModeButton");

darkModeToggle.addEventListener("click", () => {
    console.log("Toggling dark mode...");
    document.documentElement.classList.toggle("white-mode");
});

const menuScreen = document.getElementById("menuScreen");
const simulationScreen = document.getElementById("simulationScreen");
const startButton = document.getElementById("startButton");

startButton.addEventListener("click", () => {
    menuScreen.classList.add("hidden");
    simulationScreen.classList.remove("hidden");

    startSimulation();
});

const stopButton = document.getElementById('stopButton');

stopButton.addEventListener('click', () => {
    menuScreen.classList.remove("hidden");
    simulationScreen.classList.add("hidden");

    stopSimulation();
});

function startSimulation() {
    console.log("Starting simulation...");
    // Here you would add the logic to start your simulation
}

function stopSimulation() {
    console.log("Stopping simulation...");
    // Here you would add the logic to stop your simulation
}