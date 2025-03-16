package org.example.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.example.controller.ScenarioController.INTERVAL_SLEEP_TIME_MS;

/**
 * Represents an elevator in the system.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class Elevator extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Elevator.class);

    int elevatorId;
    int topFloor;

    @NonFinal
    int currentFloor;

    @NonFinal
    Direction direction;

    TreeSet<Integer> upQueue;
    TreeSet<Integer> downQueue;

    AtomicBoolean isScenarioRunning;

    public Elevator(int elevatorId, int topFloor, AtomicBoolean isScenarioRunning) {
        this.elevatorId = elevatorId;
        this.topFloor = topFloor;
        this.currentFloor = 1;
        this.direction = Direction.NONE;
        this.upQueue = new TreeSet<>();
        this.downQueue = new TreeSet<>();
        this.isScenarioRunning = isScenarioRunning;
    }

    /**
     * Adds a request to the elevator's queue.
     * @param request the request to add.
     */
    public void queueRequest(MoveRequest request) {
        if (currentFloor < request.currentFloor()) {
            upQueue.add(request.currentFloor());
            direction = Direction.UP;
        } else if (currentFloor > request.currentFloor()) {
            downQueue.add(request.currentFloor());
            direction = Direction.DOWN;
        }

        if (request.getDirection() == Direction.UP) {
            upQueue.add(request.targetFloor());
        } else if (request.getDirection() == Direction.DOWN) {
            downQueue.add(request.targetFloor());
        }
    }

    /**
     * Moves the elevator if applicable, unloads occupants, and updates the direction.
     */
    public Direction move() {
        if (direction == Direction.UP) {
            if (currentFloor < upQueue.first() && currentFloor < topFloor) {
                LOGGER.info("    Elevator id {}: Moving UP to floor [{}]", elevatorId, currentFloor + 1);
                currentFloor++;
            } else {
                upQueue.remove(currentFloor);
                direction = Direction.NONE;
            }
        } else if (direction == Direction.DOWN) {
            if (currentFloor > downQueue.last() && currentFloor > 1) {
                LOGGER.info("    Elevator id {}: Moving DOWN to floor [{}]", elevatorId, currentFloor - 1);
                currentFloor--;
            } else {
                downQueue.remove(currentFloor);
                direction = Direction.NONE;
            }
        } else {
            upQueue.remove(currentFloor);
            downQueue.remove(currentFloor);

            if (!upQueue.isEmpty() && !downQueue.isEmpty()) {
                if (upQueue.size() > downQueue.size()) {
                    direction = upQueue.first() > currentFloor ? Direction.UP : Direction.DOWN;
                } else {
                    direction = downQueue.last() < currentFloor ? Direction.DOWN : Direction.UP;
                }
            } else if (!upQueue.isEmpty()) {
                direction = upQueue.first() > currentFloor ? Direction.UP : Direction.DOWN;
            } else if (!downQueue.isEmpty()) {
                direction = downQueue.last() < currentFloor ? Direction.DOWN : Direction.UP;
            } else {
                if (currentFloor != 1) {
                    LOGGER.info("    Elevator id: Returning to first floor.");
                    queueRequest(new MoveRequest(currentFloor, 1));
                }
            }
            if (direction == Direction.UP) {
                currentFloor++;
            } else if (direction == Direction.DOWN) {
                currentFloor--;
            }
        }
        return direction;
    }

    @Override
    public void run() {
        LOGGER.info("Elevator id {}: started", elevatorId);
        while (isScenarioRunning.get() || !upQueue.isEmpty() || !downQueue.isEmpty()) {
            move();

            try {
                //noinspection BusyWait
                Thread.sleep(INTERVAL_SLEEP_TIME_MS/8);
            } catch (InterruptedException e) {
                LOGGER.error("Elevator id {}: interrupted", elevatorId, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
