package org.example.service;

import lombok.AllArgsConstructor;
import org.example.model.Direction;
import org.example.model.Elevator;
import org.example.model.MoveRequest;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ElevatorService {

    private final int costPerFloor;
    private final int costPerStop;

    /**
     * Find the best elevator for a move request.
     * @param elevators the list of elevators to choose from.
     * @param request the request to find an elevator for.
     * @return the best elevator for the request, or an empty optional if no elevator is available.
     */
    public Optional<Elevator> findBestElevator(@NotNull List<Elevator> elevators, @NotNull MoveRequest request) {
        double lowestCost = Double.MAX_VALUE;
        Integer bestIndex = null;

        for (int i = 0; i < elevators.size(); i++) {
            Elevator elevator = elevators.get(i);
            if (isValidElevator(elevator, request)) {
                int floorsAway = Math.abs(elevator.getCurrentFloor() - request.currentFloor());
                double cost = (double) floorsAway * costPerFloor + costPerStop;
                // Distribute the requests across the elevators.
                switch (elevator.getDirection()) {
                    case UP -> cost += elevator.getUpQueue().size() * costPerFloor;
                    case DOWN -> cost += elevator.getDownQueue().size() * costPerFloor;
                }

                if (cost < lowestCost) {
                    lowestCost = cost;
                    bestIndex = i;
                }
            }
        }

        return bestIndex == null ? Optional.empty() : Optional.of(elevators.get(bestIndex));
    }

    /**
     * Check if an elevator is valid for a move request.
     * @param elevator the elevator to check.
     * @param moveRequest the move request to check.
     * @return true if the elevator is valid, false otherwise.
     */
    public boolean isValidElevator(@NotNull Elevator elevator, @NotNull MoveRequest moveRequest) {
        if (moveRequest.getDirection() == Direction.NONE) {
            throw new IllegalArgumentException("No-op move requests are not supported");
        }

        return elevator.getDirection() == Direction.NONE ||
                (moveRequest.getDirection() == Direction.UP && elevator.getCurrentFloor() <= moveRequest.currentFloor()) ||
                (moveRequest.getDirection() == Direction.DOWN && elevator.getCurrentFloor() >= moveRequest.currentFloor());
    }
}
