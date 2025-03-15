package org.example.model;

import lombok.Value;

/**
 * TODO: add description.
 */
@Value
public class Elevator {
    int currentFloor;
    int targetFloor;
    int capacity;
    Direction direction;
}
