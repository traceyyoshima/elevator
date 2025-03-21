package org.example.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;

/**
 * Represents an elevator in the system.
 * Assumes there is no capacity limit.
 * <p>
 * I focused on the simulation aspect, because information on elevator modeling is available online.
 * I.E., Hallway Buttons, Hallway Displays, Cabin Buttons, Cabin Displays, are not modeled.
 */
@Data
public class Elevator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Elevator.class);

    private final int elevatorId;
    private final TreeSet<Integer> upQueue;
    private final TreeSet<Integer> downQueue;

    private int currentFloor;
    private Direction direction;

    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 1;
        this.direction = Direction.NONE;
        this.upQueue = new TreeSet<>();
        this.downQueue = new TreeSet<>();
    }
}
