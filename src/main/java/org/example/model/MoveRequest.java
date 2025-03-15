package org.example.model;

/**
 * Represents a request to pick up a passenger from the current floor, and a request to move to a target floor.
 */
public record MoveRequest(int currentFloor, int targetFloor) {

    /**
     * Floor values should be non-negative.
     * A no-op request is represented by a request with the same current and target floor.
     *
     * @param currentFloor the current floor of the request.
     * @param targetFloor  the target floor after a pickup.
     */
    public MoveRequest {
        if (currentFloor < 0 || targetFloor < 0) {
            throw new IllegalArgumentException("Floors must be non-negative");
        }
    }

    /**
     * Returns the direction of the request based on the current and target floors.
     * @return the direction of the request.
     */
    public Direction getDirection() {
        if (currentFloor == targetFloor) {
            // No-op request, no direction needed.
            return Direction.NONE;
        }
        return currentFloor < targetFloor ? Direction.UP : Direction.DOWN;
    }


    @Override
    public String toString() {
        return "MoveRequest { currentFloor=" + currentFloor + " targetFloor=" + targetFloor + "}";
    }
}
