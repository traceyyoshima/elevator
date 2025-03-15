package org.example;

/**
 * ScenarioConstraints is a record that defines the constraints for the elevator system simulation.
 * Maximum values are set to ensure the simulation runs within reasonable limits.
 * @param floorCount
 * @param elevatorCount
 * @param elevatorCapacity
 * @param requestsPerTimeSlice
 */
public record ScenarioConstraints(int floorCount,
                                  int elevatorCount,
                                  int elevatorCapacity,
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
