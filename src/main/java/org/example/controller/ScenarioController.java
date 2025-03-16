package org.example.controller;

import lombok.Value;
import org.example.ScenarioConstraints;
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
 * Simulate the scenario.
 */
@Value
public class ScenarioController implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioController.class);
    private static final int INTERVAL_COUNT = 5;

    public static final short INTERVAL_SLEEP_TIME_MS = 100;

    ScenarioInput scenarioInput;
    List<ElevatorController> elevatorControllers;
    ElevatorRequestController elevatorRequestController;
    AtomicBoolean isScenarioRunning;

    public ScenarioController() {
        this.scenarioInput = readInput();
        this.isScenarioRunning = new AtomicBoolean(false);
        // Adjusting the elevator count will show the effect on wait time during prime-time hours.
        List<ElevatorController> elevatorControllers = new ArrayList<>(scenarioInput.constraints.elevatorCount());
        for (int i = 0; i < scenarioInput.constraints.elevatorCount(); i++) {
            ElevatorController elevatorController = new ElevatorController(i, scenarioInput.constraints.floorCount(), isScenarioRunning);
            elevatorControllers.add(elevatorController);
        }

        this.elevatorControllers = elevatorControllers.stream().toList();
        this.elevatorRequestController = new ElevatorRequestController(
                this.elevatorControllers,
                scenarioInput.constraints.costPerFloor(),
                scenarioInput.constraints.costPerStop(),
                isScenarioRunning);
    }

    /**
     * Parse scenario input file and map the into {@link ScenarioInput}.
     *<p>
     * The scenario input file should be in the following format:
     * # header
     * # constraints
     * # timeSlice, floor, destination
     *<p>
     * Each time slice represents an hour, and the current floor and destination of a request.
     * No-op requests (current floor and destination are equal to 0) are randomly spread throughout each hour to simulate a real-world scenario.
     *
     * @return {@link ScenarioInput}.
     */
    private @NotNull ScenarioInput readInput() {
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

                // No-op requests are not filtered out during parsing, so that the intervals will have a random distribution.
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
    private @NotNull Integer[] convertToIntArray(@NotNull String[] inputValues) {
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

    private record ScenarioInput(@NotNull ScenarioConstraints constraints,
                                 @NotNull Map<Integer, List<MoveRequest>> moveRequests) {
    }

    /**
     * Execute the scenario.
     */
    public void execute() {
        LOGGER.debug("Scenario constraints: {}", scenarioInput.constraints);

        isScenarioRunning.set(true);
        elevatorRequestController.start();
        for (ElevatorController elevatorController : elevatorControllers) {
            elevatorController.start();
        }

        for (List<MoveRequest> value : scenarioInput.moveRequests().values()) {
            int size = value.size();
            int interval = (int) Math.ceil((double) size / INTERVAL_COUNT);

            // Each hour is split into intervals to simulate ebs and flows of requests.
            for (int i = 0; i < INTERVAL_COUNT; i++) {
                int start = i * interval;
                int end = Math.min(start + interval, size);

                if (start >= size) {
                    break;
                }

                List<MoveRequest> requestsInTimeSlice = value.subList(i, end);
                elevatorRequestController.queueRequests(requestsInTimeSlice);

                try {
                    // Each interval sleep represents an hour, the interval sleep time is a subset of the hour.
                    // The sleeps could be adjusted to be more realistic, but this is a simulation for demonstration purposes.
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
            elevatorRequestController.join();
            for (ElevatorController elevatorController : elevatorControllers) {
                elevatorController.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        execute();
        while (!elevatorRequestController.isDone()) {
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
