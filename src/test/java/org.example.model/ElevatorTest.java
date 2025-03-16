package org.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class ElevatorTest {

    private final int topFloor = 10;
    private Elevator elevator;

    @BeforeEach
    void setUp() {
        elevator = new Elevator(1, 10, new AtomicBoolean(false));
    }

    @Test
    void moveUp() {
        MoveRequest request = new MoveRequest(1, 5);
        elevator.queueRequest(request);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDown() {
        Elevator elevator = topFloorElevator();
        MoveRequest request = new MoveRequest(5, 1);
        elevator.queueRequest(request);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(0);
    }

    @Test
    void moveUpDown() {
        MoveRequest request = new MoveRequest(5, 1);
        elevator.queueRequest(request);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(0);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(0);
    }

    @Test
    void moveUpUp() {
        MoveRequest request = new MoveRequest(5, 10);
        elevator.queueRequest(request);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(1);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDownUp() {
        Elevator elevator = topFloorElevator();
        MoveRequest request = new MoveRequest(5, topFloor);
        elevator.queueRequest(request);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(0);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDownDown() {
        MoveRequest request = new MoveRequest(5, 1);
        elevator.queueRequest(request);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(1);

        moveElevator(elevator);
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(0);
    }

    private void moveElevator(Elevator elevator) {
        int count = 1;
        Direction direction = elevator.move();
        while (direction != Direction.NONE) {
            if (count > topFloor) {
                throw new IllegalStateException("Elevator is stuck");
            }

            direction = elevator.move();
            count++;
        }
    }

    private Elevator topFloorElevator() {
        MoveRequest request = new MoveRequest(1, topFloor);
        elevator.queueRequest(request);
        moveElevator(elevator);
        return elevator;
    }
}
