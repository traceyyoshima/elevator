package org.example.controller;

import org.example.model.Direction;
import org.example.model.MoveRequest;
import org.example.service.ElevatorControllerService;
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
 * Process move requests and control interactions with the request queue.
 */
public class ElevatorRequestController extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElevatorRequestController.class);

    private final ElevatorControllerService elevatorControllerService;
    private final BlockingQueue<MoveRequest> controllerQueue;
    private final List<ElevatorController> elevatorControllers;
    private final AtomicBoolean isScenarioRunning;

    public ElevatorRequestController(@NotNull List<ElevatorController> elevatorControllers,
                                     int costPerFloor,
                                     int costPerStop,
                                     @NotNull AtomicBoolean isScenarioRunning) {
        this.elevatorControllers = elevatorControllers;
        this.isScenarioRunning = isScenarioRunning;
        this.elevatorControllerService = new ElevatorControllerService(costPerFloor, costPerStop);
        this.controllerQueue = new LinkedBlockingQueue<>();
    }

    /**
     * Adds all non-no-op requests to the controller queue.
     * @param requests the list of requests to add.
     */
    public void queueRequests(@NotNull List<MoveRequest> requests) {
        controllerQueue.addAll(requests.stream().filter(request -> request.getDirection() != Direction.NONE).toList());
    }

    /**
     * @return true if all requests have been processed, false otherwise.
     */
    public boolean isDone() {
        if (!controllerQueue.isEmpty()) {
            return false;
        }

        for (ElevatorController elevatorController : elevatorControllers) {
            if (!elevatorController.getElevator().getUpQueue().isEmpty() || !elevatorController.getElevator().getDownQueue().isEmpty()) {
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
        Optional<ElevatorController> bestElevatorController = elevatorControllerService.findBestElevator(elevatorControllers, request);
        if (bestElevatorController.isEmpty()) {
            return false;
        }

        ElevatorController elevatorController = bestElevatorController.get();
        elevatorController.queueRequest(request);
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
