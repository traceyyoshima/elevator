package org.example.service;

import org.example.controller.ElevatorController;
import org.example.model.Direction;
import org.example.model.MoveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class ElevatorControllerServiceTest {

    private final ElevatorControllerService elevatorControllerService = new ElevatorControllerService(1, 1);
    private ElevatorController elevatorController;

    @BeforeEach
    void setUp() {
        elevatorController = new ElevatorController(1, 10, new AtomicBoolean(false));
    }

    @Test
    void differentDirectionIsInvalid() {
        MoveRequest request = new MoveRequest(1, 5);
        elevatorController.queueRequest(request);
        elevatorController.move();
        assertThat(elevatorController.getElevator().getDirection()).isEqualTo(Direction.UP);

        request = new MoveRequest(5, 1);
        elevatorController.queueRequest(request);
        assertThat(elevatorControllerService.isValidElevator(elevatorController, request)).isEqualTo(false);
    }

    @Test
    void sameDirectionOutOfPathIsInvalid() {
        MoveRequest request = new MoveRequest(1, 5);
        elevatorController.queueRequest(request);
        elevatorController.move();
        assertThat(elevatorController.getElevator().getDirection()).isEqualTo(Direction.UP);

        elevatorController.queueRequest(request);
        assertThat(elevatorControllerService.isValidElevator(elevatorController, request)).isEqualTo(false);
    }

    @Test
    void noDirectionIsValid() {
        MoveRequest request = new MoveRequest(1, 5);
        assertThat(elevatorControllerService.isValidElevator(elevatorController, request)).isEqualTo(true);
    }

    @Test
    void sameDirectionInPathIsValid() {
        MoveRequest request = new MoveRequest(1, 5);
        elevatorController.queueRequest(request);
        elevatorController.move();

        assertThat(elevatorController.getElevator().getDirection()).isEqualTo(Direction.UP);

        request = new MoveRequest(5, 10);
        assertThat(elevatorControllerService.isValidElevator(elevatorController, request)).isEqualTo(true);
    }
}
