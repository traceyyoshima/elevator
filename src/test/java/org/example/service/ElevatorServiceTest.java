package org.example.service;

import org.example.model.Direction;
import org.example.model.Elevator;
import org.example.model.MoveRequest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class ElevatorServiceTest {

    @Test
    void isIdle() {
        Elevator elevator = new Elevator(1, 10, new AtomicBoolean(false));
        MoveRequest request = new MoveRequest(1, 5);
        ElevatorService elevatorService = new ElevatorService(1, 1);
        assertThat(elevatorService.isValidElevator(elevator, request)).isEqualTo(true);
    }

    @Test
    void isSameDirection() {
        Elevator elevator = new Elevator(1, 10, new AtomicBoolean(false));
        MoveRequest request = new MoveRequest(1, 5);
        elevator.queueRequest(request);
        elevator.move();

        assertThat(elevator.getDirection()).isEqualTo(Direction.UP);

        request = new MoveRequest(5, 10);
        ElevatorService elevatorService = new ElevatorService(1, 1);
        assertThat(elevatorService.isValidElevator(elevator, request)).isEqualTo(true);
    }

    @Test
    void isDifferentDirection() {
        Elevator elevator = new Elevator(1, 10, new AtomicBoolean(false));
        MoveRequest request = new MoveRequest(1, 5);
        elevator.queueRequest(request);
        elevator.move();

        assertThat(elevator.getDirection()).isEqualTo(Direction.UP);

        request = new MoveRequest(5, 1);
        ElevatorService elevatorService = new ElevatorService(1, 1);
        assertThat(elevatorService.isValidElevator(elevator, request)).isEqualTo(false);
    }
}
