package org.example.controller;

import org.example.ScenarioConstraints;
import org.example.model.MoveRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;

/**
 * TODO: add description.
 */
public class ScenarioController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioController.class);

    /**
     * Parse scenario input file and create a list of PickupRequest objects.
     * @return simulated pickup requests.
     */
    @SuppressWarnings("SameParameterValue")
    private ScenarioInput readInput(String fileName) {
        if (fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }

        // TreeMap is used to maintain the order of time slices.
        Map<Integer, List<MoveRequest>> moveRequests = new TreeMap<>();
        ScenarioConstraints constraints;
        String inputRegex = "\\s*,\\s*";
        try (FileReader fileReader = new FileReader(Path.of(fileName).toFile())) {
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
            constraints = new ScenarioConstraints(parsed[0], parsed[1], parsed[2], parsed[3]);
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
        ScenarioInput scenarioInput = readInput("src/main/resources/scenario.txt");
        LOGGER.debug("Scenario constraints: {}", scenarioInput.constraints);
    }

    /**
     * Shutdown the controller, joins all threads.
     */
    public void shutdown() {

    }
}
