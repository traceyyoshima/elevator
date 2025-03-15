package org.example.controller;

import org.example.model.MoveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.example.controller.ScenarioController.BASE_SLEEP_TIME_MS;

/**
 * Process floor move requests, and calculate the best elevator to serve the request.
 */
public class ElevatorController extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElevatorController.class);

    private final BlockingQueue<MoveRequest> controllerQueue;
    private final AtomicBoolean isScenarioRunning;

    public ElevatorController(AtomicBoolean isScenarioRunning) {
        this.isScenarioRunning = isScenarioRunning;
        this.controllerQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        LOGGER.info("ElevatorController started");
        while (isScenarioRunning.get() || !controllerQueue.isEmpty()) {
            try {
                MoveRequest request = controllerQueue.poll();
                if (request != null) {
                    LOGGER.info("Processing request {}", request);
                    // process request
                }

                //noinspection BusyWait
                Thread.sleep(BASE_SLEEP_TIME_MS / 5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("ElevatorController interrupted", e);
            }
        }
    }
}
