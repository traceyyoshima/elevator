package org.example.controller;

import org.example.model.MoveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.example.controller.ScenarioController.BASE_SLEEP_TIME_MS;

/**
 * A controller for handling requests from a specific floor.
 * This class manages the requests for a specific floor and processes them in a separate thread.
 * <p>
 * Items will not be removed from the queue until an occupant enters an elevator.
 * Occupants will only enter the elevator until the capacity is reached.
 * A new request will be added to the elevator controller after the elevator leaves if the queue is not empty.
 */
public class FloorRequestController extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(FloorRequestController.class);

    private final BlockingQueue<MoveRequest> controllerQueue;
    private final int floorNumber;
    private final AtomicBoolean isScenarioRunning;

    public FloorRequestController(int floorNumber, AtomicBoolean isScenarioRunning) {
        this.floorNumber = floorNumber;
        this.isScenarioRunning = isScenarioRunning;
        this.controllerQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        LOGGER.info("FloorRequestController started for floor {}", floorNumber);
        while (isScenarioRunning.get() || !controllerQueue.isEmpty()) {
            try {
                MoveRequest request = controllerQueue.poll();
                if (request != null) {
                    LOGGER.info("Processing request {} for floor {}", request, floorNumber);
                    // process request
                }

                //noinspection BusyWait
                Thread.sleep(BASE_SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("FloorRequestController interrupted", e);
            }
        }
    }
}
