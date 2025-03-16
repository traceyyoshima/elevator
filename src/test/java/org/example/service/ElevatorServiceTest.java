package org.example.service;

import org.example.model.Direction;
import org.example.model.Elevator;
import org.example.model.MoveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class ElevatorServiceTest {

    private final ElevatorService elevatorService = new ElevatorService(1, 1);
    private Elevator elevator;

    @BeforeEach
    void setUp() {
        elevator = new Elevator(1, 10, new AtomicBoolean(false));
    }

    @Test
    void differentDirectionIsInvalid() {
        MoveRequest request = new MoveRequest(1, 5);
        elevator.queueRequest(request);
        elevator.move();
        assertThat(elevator.getDirection()).isEqualTo(Direction.UP);

        request = new MoveRequest(5, 1);
        elevator.queueRequest(request);
        assertThat(elevatorService.isValidElevator(elevator, request)).isEqualTo(false);
    }

    @Test
    void sameDirectionOutOfPathIsInvalid() {
        MoveRequest request = new MoveRequest(1, 5);
        elevator.queueRequest(request);
        elevator.move();
        assertThat(elevator.getDirection()).isEqualTo(Direction.UP);

        elevator.queueRequest(request);
        assertThat(elevatorService.isValidElevator(elevator, request)).isEqualTo(false);
    }

    @Test
    void noDirectionIsValid() {
        MoveRequest request = new MoveRequest(1, 5);
        assertThat(elevatorService.isValidElevator(elevator, request)).isEqualTo(true);
    }

    @Test
    void sameDirectionInPathIsValid() {
        MoveRequest request = new MoveRequest(1, 5);
        elevator.queueRequest(request);
        elevator.move();

        assertThat(elevator.getDirection()).isEqualTo(Direction.UP);

        request = new MoveRequest(5, 10);
        assertThat(elevatorService.isValidElevator(elevator, request)).isEqualTo(true);
    }
}
