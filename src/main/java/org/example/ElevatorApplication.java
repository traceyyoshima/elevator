package org.example;

import org.example.controller.ScenarioController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The elevator simulation could be represented as microservices, but it seems overkill for an exercise.
 */
public class ElevatorApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElevatorApplication.class);

    public static void main(String[] args) {
        LOGGER.info("********************************************************************");
        LOGGER.info("Starting elevator simulation");

        ScenarioController scenarioController = new ScenarioController();
        scenarioController.run();

        LOGGER.info("Elevator simulation finished");
        LOGGER.info("********************************************************************");
    }
}
