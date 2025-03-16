package org.example.controller;

import org.example.ScenarioConstraints;
import org.example.model.Elevator;
import org.example.model.MoveRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reads the scenario input file and creates a list of {@link MoveRequest} objects mapped to hours.
 * Prime-time hours will contain more MoveRequest(s) to simulate busy hours.
 * Each hour is divided into N slices of time to allow the elevator to process requests.
 * An elevator will move M number of times per time slice.
 */
public class ScenarioController implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioController.class);
    private static final int INTERVAL_COUNT = 5;

    public static final short INTERVAL_SLEEP_TIME_MS = 100;

    private final ScenarioInput scenarioInput;
    private final List<Elevator> elevators;
    private final ElevatorController elevatorController;
    private final AtomicBoolean isScenarioRunning;

    public ScenarioController() {
        this.scenarioInput = readInput();
        this.isScenarioRunning = new AtomicBoolean(false);
        // Adjusting the elevator count will show the effect on wait time during prime-time hours.
        List<Elevator> elevators = new ArrayList<>(scenarioInput.constraints.elevatorCount());
        for (int i = 0; i < scenarioInput.constraints.elevatorCount(); i++) {
            Elevator elevator = new Elevator(i, scenarioInput.constraints.floorCount(), isScenarioRunning);
            elevators.add(elevator);
        }

        this.elevators = elevators.stream().toList();
        this.elevatorController = new ElevatorController(
                this.elevators,
                scenarioInput.constraints.costPerFloor(),
                scenarioInput.constraints.costPerStop(),
                isScenarioRunning);
    }

    /**
     * Parse scenario input file and create a list of PickupRequest objects.
     * @return simulated pickup requests.
     */
    private ScenarioInput readInput() {
        // TreeMap is used to maintain the order of time slices.
        Map<Integer, List<MoveRequest>> moveRequests = new TreeMap<>();
        ScenarioConstraints constraints;
        String inputRegex = "\\s*,\\s*";
        try (FileReader fileReader = new FileReader(Path.of("src/main/resources/scenario.txt").toFile())) {
            BufferedReader br = new BufferedReader(fileReader);

            List<MoveRequest> requests;
            int currentTimeSlice = -1;

            br.readLine(); // Skip the header line.

            String line = br.readLine();
            if (line == null) {
                throw new UnsupportedOperationException("No data found in the file.");
            }
            String[] inputValues = line.trim().split(inputRegex);
            Integer[] parsed = convertToIntArray(inputValues);
            constraints = new ScenarioConstraints(parsed[0], parsed[1], parsed[2], parsed[3], parsed[4], parsed[5]);
            line = br.readLine();
            while (line != null) {
                inputValues = line.trim().split(inputRegex);
                if (inputValues.length != 3) {
                    throw new UnsupportedOperationException("Invalid input format. Expected 3 parameters.");
                }
                int timeSlice = Integer.parseInt(inputValues[0]);
                int floor = Integer.parseInt(inputValues[1]);
                int destination = Integer.parseInt(inputValues[2]);
                if (currentTimeSlice != timeSlice) {
                    currentTimeSlice = timeSlice;
                }
                requests = moveRequests.computeIfAbsent(currentTimeSlice, _ -> new ArrayList<>());
                requests.add(new MoveRequest(floor, destination));
                line = br.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading scenario file", e);
        }

        return new ScenarioInput(constraints, moveRequests);
    }

    /**
     * Converts an array of strings to an array of integers.
     * @param inputValues the input string array.
     * @return the converted integer array.
     */
    private Integer[] convertToIntArray(String[] inputValues) {
        Integer[] parsed = new Integer[inputValues.length];
        for (int i = 0; i < inputValues.length; i++) {
            try {
                parsed[i] = Integer.parseInt(inputValues[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format in input values", e);
            }
        }
        return parsed;
    }

    /**
     * Represents the input for the scenario, including constraints and move requests.
     */
    private record ScenarioInput(@NotNull ScenarioConstraints constraints,
                                 @NotNull Map<Integer, List<MoveRequest>> moveRequests) {
    }

    /**
     * Execute the scenario.
     */
    public void execute() {
        LOGGER.debug("Scenario constraints: {}", scenarioInput.constraints);

        isScenarioRunning.set(true);
        elevatorController.start();
        for (Elevator elevator : elevators) {
            elevator.start();
        }

        for (List<MoveRequest> value : scenarioInput.moveRequests().values()) {
            int size = value.size();
            int interval = (int) Math.ceil((double) size / INTERVAL_COUNT);

            for (int i = 0; i < INTERVAL_COUNT; i++) {
                int start = i * interval;
                int end = Math.min(start + interval, size);

                if (start >= size) {
                    break;
                }

                List<MoveRequest> requestsInTimeSlice = value.subList(i, end);
                elevatorController.queueRequests(requestsInTimeSlice);

                try {
                    Thread.sleep(INTERVAL_SLEEP_TIME_MS);
                } catch (InterruptedException e) {
                    LOGGER.error("ScenarioController interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Shutdown the controller, joins all threads.
     */
    public void shutdown() {
        isScenarioRunning.set(false);
        try {
            elevatorController.join();
            for (Elevator elevator : elevators) {
                elevator.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        execute();
        while (!elevatorController.isDone()) {
            try {
                //noinspection BusyWait
                Thread.sleep(INTERVAL_SLEEP_TIME_MS * 2);
            } catch (InterruptedException e) {
                LOGGER.error("ScenarioController interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
        shutdown();
    }
}
