package org.example.controller;

import lombok.Getter;
import org.example.model.Direction;
import org.example.model.Elevator;
import org.example.model.MoveRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.example.controller.ScenarioController.INTERVAL_SLEEP_TIME_MS;

/**
 * Controls the elevator's movement and request queue.
 * <p>
 * The elevator moves to the requested floor and updates its direction accordingly.
 * It also handles the unloading of occupants and manages the request queue.
 * <p>
 * Note:
 *     It would be more accurate to have FloorRequestControllers to enable queues for each floor.
 *     The Elevator would need a capacity, and the ElevatorService would need to account for elevator capacity before
 *     assigning an Elevator. The ElevatorRequestController would only request as many MoveRequests from a FloorRequestController
 *     as the elevator can handle.
 */
public class ElevatorController extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElevatorController.class);

    private final int topFloor;
    private final AtomicBoolean isScenarioRunning;

    @Getter
    private final Elevator elevator;

    public ElevatorController(int elevatorId,
                              int topFloor,
                              @NotNull AtomicBoolean isScenarioRunning) {
        this.elevator = new Elevator(elevatorId);
        this.topFloor = topFloor;
        this.isScenarioRunning = isScenarioRunning;
    }

    /**
     * Adds a request to the elevator's queue and updates the direction.
     * Method assumes requests are validated beforehand.
     * @param request the request to add.
     */
    public void queueRequest(@NotNull MoveRequest request) {
        // Send the elevator to the requests current floor.
        if (elevator.getCurrentFloor() < request.currentFloor()) {
            elevator.getUpQueue().add(request.currentFloor());
            elevator.setDirection(Direction.UP);
        } else if (elevator.getCurrentFloor() > request.currentFloor()) {
            elevator.getDownQueue().add(request.currentFloor());
            elevator.setDirection(Direction.DOWN);
        }

        // Adds the target floor to the queue.
        if (request.getDirection() == Direction.UP) {
            elevator.getUpQueue().add(request.targetFloor());
        } else if (request.getDirection() == Direction.DOWN) {
            elevator.getDownQueue().add(request.targetFloor());
        }
    }

    /**
     * Moves the elevator if applicable, unloads occupants, and updates the direction.
     */
    public @NotNull Direction move() {
        if (elevator.getDirection() == Direction.UP) {
            if (elevator.getCurrentFloor() < elevator.getUpQueue().first() && elevator.getCurrentFloor() < topFloor) {
                LOGGER.info("    Elevator id {}: Moving UP to floor [{}]", elevator.getElevatorId(), elevator.getCurrentFloor() + 1);
                elevator.setCurrentFloor(elevator.getCurrentFloor() + 1);
            } else {
                elevator.getUpQueue().remove(elevator.getCurrentFloor());
                elevator.setDirection(Direction.NONE);
            }
        } else if (elevator.getDirection() == Direction.DOWN) {
            if (elevator.getCurrentFloor() > elevator.getDownQueue().last() && elevator.getCurrentFloor() > 1) {
                LOGGER.info("    Elevator id {}: Moving DOWN to floor [{}]", elevator.getElevatorId(), elevator.getCurrentFloor() - 1);
                elevator.setCurrentFloor(elevator.getCurrentFloor() - 1);
            } else {
                elevator.getDownQueue().remove(elevator.getCurrentFloor());
                elevator.setDirection(Direction.NONE);
            }
        } else {
            elevator.getUpQueue().remove(elevator.getCurrentFloor());
            elevator.getDownQueue().remove(elevator.getCurrentFloor());

            if (!elevator.getUpQueue().isEmpty() && !elevator.getDownQueue().isEmpty()) {
                Direction direction;
                if (elevator.getUpQueue().size() > elevator.getDownQueue().size()) {
                    direction = elevator.getUpQueue().first() > elevator.getCurrentFloor() ? Direction.UP : Direction.DOWN;
                } else {
                    direction = elevator.getDownQueue().last() < elevator.getCurrentFloor() ? Direction.DOWN : Direction.UP;
                }
                elevator.setDirection(direction);
            } else if (!elevator.getUpQueue().isEmpty()) {
                Direction direction = elevator.getUpQueue().first() > elevator.getCurrentFloor() ? Direction.UP : Direction.DOWN;
                elevator.setDirection(direction);
            } else if (!elevator.getDownQueue().isEmpty()) {
                Direction direction = elevator.getDownQueue().last() < elevator.getCurrentFloor() ? Direction.DOWN : Direction.UP;
                elevator.setDirection(direction);
            } else {
                if (elevator.getCurrentFloor() != 1) {
                    LOGGER.info("    Elevator id: Returning to first floor.");
                    queueRequest(new MoveRequest(elevator.getCurrentFloor(), 1));
                }
            }
            if (elevator.getDirection() == Direction.UP) {
                elevator.setCurrentFloor(elevator.getCurrentFloor() + 1);
            } else if (elevator.getDirection() == Direction.DOWN) {
                elevator.setCurrentFloor(elevator.getCurrentFloor() - 1);
            }
        }
        return elevator.getDirection();
    }

    @Override
    public void run() {
        LOGGER.info("Elevator id {}: started", elevator.getElevatorId());
        while (isScenarioRunning.get() || !elevator.getUpQueue().isEmpty() || !elevator.getDownQueue().isEmpty()) {
            move();

            try {
                // Each interval sleep represents an hour, the interval sleep time is a subset of the hour.
                // The sleeps could be adjusted to be more realistic, but this is a simulation for demonstration purposes.

                //noinspection BusyWait
                Thread.sleep(INTERVAL_SLEEP_TIME_MS/8);
            } catch (InterruptedException e) {
                LOGGER.error("Elevator id {}: interrupted", elevator.getElevatorId(), e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
