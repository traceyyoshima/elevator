package org.example;

/**
 * ScenarioConstraints is a record that defines the constraints for the elevator system simulation.
 * Maximum values are set to ensure the simulation runs within reasonable limits.
 * @param floorCount the number of floors in the building
 * @param elevatorCount the number of elevators in the building
 * @param elevatorCapacity the maximum number of passengers an elevator can carry
 * @param costPerFloor the cost of moving one floor. using an int at the cost of accuracy for quick demonstration purposes.
 * @param costPerStop the cost of stopping at a floor. using an int at the cost of accuracy for quick demonstration purposes.
 * @param requestsPerTimeSlice the total number of requests per time slice
 */
public record ScenarioConstraints(int floorCount,
                                  int elevatorCount,
                                  int elevatorCapacity,
                                  int costPerFloor,
                                  int costPerStop,
                                  int requestsPerTimeSlice) {

    public ScenarioConstraints {
        if (floorCount <= 0 || floorCount > 150) {
            throw new IllegalArgumentException("Number of floors must be greater than 0 and less than 150");
        }
        if (elevatorCount <= 0 || elevatorCount > 150) {
            throw new IllegalArgumentException("Number of elevators must be greater than 0 and less than 150");
        }
        if (elevatorCapacity <= 0 || elevatorCapacity > 10) {
            throw new IllegalArgumentException("Elevator capacity must be greater than 0 and less than 10");
        }
        if (costPerFloor <= 0 || costPerFloor > 10) {
            throw new IllegalArgumentException("Cost per floor must be greater than 0 and less than 10");
        }
        if (costPerStop <= 0 || costPerStop > 20) {
            throw new IllegalArgumentException("Cost per stop must be greater than 0 and less than 20");
        }
        if (costPerFloor >= costPerStop) {
            throw new IllegalArgumentException("Cost per floor must be less than cost per stop");
        }
        if (requestsPerTimeSlice <= 0 || requestsPerTimeSlice > 100) {
            throw new IllegalArgumentException("Number of requests per time slice must be greater than 0 and less than 100");
        }
    }

    @Override
    public String toString() {
        return "ScenarioConstraints {" +
                " floorCount=" + floorCount +
                ", elevatorCount=" + elevatorCount +
                ", elevatorCapacity=" + elevatorCapacity +
                ", requestsPerTimeSlice=" + requestsPerTimeSlice +
                " }";
    }
}
