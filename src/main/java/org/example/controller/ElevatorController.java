package org.example.controller;

import org.example.model.Direction;
import org.example.model.Elevator;
import org.example.model.MoveRequest;
import org.example.service.ElevatorService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.example.controller.ScenarioController.INTERVAL_SLEEP_TIME_MS;

/**
 * Process floor move requests, and calculate the best elevator to serve the request.
 */
public class ElevatorController extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElevatorController.class);

    private final ElevatorService elevatorService;
    private final BlockingQueue<MoveRequest> controllerQueue;
    private final List<Elevator> elevators;
    private final AtomicBoolean isScenarioRunning;

    public ElevatorController(List<Elevator> elevators,
                              int costPerFloor,
                              int costPerStop,
                              AtomicBoolean isScenarioRunning) {
        this.elevators = elevators;
        this.isScenarioRunning = isScenarioRunning;
        this.elevatorService = new ElevatorService(costPerFloor, costPerStop);
        this.controllerQueue = new LinkedBlockingQueue<>();
    }

    /**
     * Adds all non-no-op requests to the controller queue.
     * @param requests the list of requests to add.
     */
    public void queueRequests(List<MoveRequest> requests) {
        controllerQueue.addAll(requests.stream().filter(request -> request.getDirection() != Direction.NONE).toList());
    }

    public boolean isDone() {
        if (!controllerQueue.isEmpty()) {
            return false;
        }

        for (Elevator elevator : elevators) {
            if (!elevator.getUpQueue().isEmpty() || !elevator.getDownQueue().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Assign an elevator to a move request.
     * @param request the request to assign.
     */
    private synchronized boolean assignElevator(@NotNull MoveRequest request) {
        Optional<Elevator> bestElevator = elevatorService.findBestElevator(elevators, request);
        if (bestElevator.isEmpty()) {
            return false;
        }

        Elevator elevator = bestElevator.get();
        elevator.queueRequest(request);
        return true;
    }

    @Override
    public void run() {
        LOGGER.info("ElevatorController started");
        while (isScenarioRunning.get() || !controllerQueue.isEmpty()) {
            MoveRequest request = controllerQueue.peek();
            if (request != null) {
                if (request.getDirection() == Direction.NONE) {
                    LOGGER.debug("No-op move request, skipping.");
                    controllerQueue.poll();
                    continue;
                }

                if (assignElevator(request)) {
                    LOGGER.info("Elevator assigned to request {}", request);
                    controllerQueue.poll();
                } else {
                    LOGGER.info("No elevator is available, waiting for one to become available.");
                }
            }
            try {
                //noinspection BusyWait
                Thread.sleep(INTERVAL_SLEEP_TIME_MS/2);
            } catch (InterruptedException e) {
                LOGGER.error("ElevatorController interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
