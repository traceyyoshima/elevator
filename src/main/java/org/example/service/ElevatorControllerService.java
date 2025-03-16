package org.example.service;

import lombok.AllArgsConstructor;
import org.example.controller.ElevatorController;
import org.example.model.Direction;
import org.example.model.MoveRequest;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static org.example.model.Direction.DOWN;
import static org.example.model.Direction.UP;

/**
 * Service to find the best elevator for a move request.
 */
@AllArgsConstructor
public class ElevatorControllerService {

    private final int costPerFloor;
    private final int costPerStop;

    /**
     * Find the best elevator for a move request.
     * @param elevators the list of elevators to choose from.
     * @param request the request to find an elevator for.
     * @return the best elevator for the request, or an empty optional if no elevator is available.
     */
    public Optional<ElevatorController> findBestElevator(@NotNull List<ElevatorController> elevators, @NotNull MoveRequest request) {
        double lowestCost = Double.MAX_VALUE;
        Integer bestIndex = null;

        for (int i = 0; i < elevators.size(); i++) {
            ElevatorController elevatorController = elevators.get(i);
            if (isValidElevator(elevatorController, request)) {
                double cost = getCost(request, elevatorController);
                if (cost < lowestCost) {
                    lowestCost = cost;
                    bestIndex = i;
                }
            }
        }

        return bestIndex == null ? Optional.empty() : Optional.of(elevators.get(bestIndex));
    }

    /**
     * Calculate the cost of a move request for an elevator.
     * @param request the move request to calculate the cost for.
     * @param elevatorController the elevator to calculate the cost for.
     * @return the cost of the move request for the elevator.
     */
    private double getCost(@NotNull MoveRequest request, ElevatorController elevatorController) {
        int floorsAway = Math.abs(elevatorController.getElevator().getCurrentFloor() - request.currentFloor());
        double cost = (double) floorsAway * costPerFloor + costPerStop;
        // Distribute the requests across the elevators.
        switch (elevatorController.getElevator().getDirection()) {
            case UP -> cost += elevatorController.getElevator().getUpQueue().size() * costPerFloor;
            case DOWN -> cost += elevatorController.getElevator().getDownQueue().size() * costPerFloor;
        }
        return cost;
    }

    /**
     * Check if an elevator is valid for a move request.
     * @param elevatorController the elevator to check.
     * @param moveRequest the move request to check.
     * @return true if the elevator is valid, false otherwise.
     */
    public boolean isValidElevator(@NotNull ElevatorController elevatorController, @NotNull MoveRequest moveRequest) {
        if (moveRequest.getDirection() == Direction.NONE) {
            throw new IllegalArgumentException("No-op move requests are not supported");
        }

        return elevatorController.getElevator().getDirection() == Direction.NONE ||
                (moveRequest.getDirection() == UP && elevatorController.getElevator().getCurrentFloor() <= moveRequest.currentFloor()) ||
                (moveRequest.getDirection() == DOWN && elevatorController.getElevator().getCurrentFloor() >= moveRequest.currentFloor());
    }
}
