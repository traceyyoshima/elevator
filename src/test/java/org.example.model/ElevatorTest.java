package org.example.model;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class ElevatorTest {

    @Test
    void moveUp() {
        Elevator elevator = new Elevator(1, 10, new AtomicBoolean(false));
        MoveRequest request = new MoveRequest(1, 5);
        elevator.queueRequest(request);
        while (elevator.move() != Direction.NONE) {
            elevator.move();
        }
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDown() {
        Elevator elevator = topFloorElevator();
        MoveRequest request = new MoveRequest(5, 1);
        elevator.queueRequest(request);
        while (elevator.move() != Direction.NONE) {
            elevator.move();
        }
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(0);
    }

    @Test
    void moveUpDown() {
        Elevator elevator = new Elevator(1, 10, new AtomicBoolean(false));
        MoveRequest request = new MoveRequest(5, 1);
        elevator.queueRequest(request);
        Direction direction = elevator.move();
        while (direction != Direction.NONE) {
            direction = elevator.move();
        }
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(0);

        direction = elevator.move();
        while (direction != Direction.NONE) {
            direction = elevator.move();
        }

        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(0);
    }

    @Test
    void moveUpUp() {
        Elevator elevator = new Elevator(1, 10, new AtomicBoolean(false));
        MoveRequest request = new MoveRequest(5, 10);
        elevator.queueRequest(request);
        Direction direction = elevator.move();
        while (direction != Direction.NONE) {
            direction = elevator.move();
        }
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(1);

        direction = elevator.move();
        while (direction != Direction.NONE) {
            direction = elevator.move();
        }

        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDownUp() {
        Elevator elevator = topFloorElevator();
        MoveRequest request = new MoveRequest(5, 10);
        elevator.queueRequest(request);
        Direction direction = elevator.move();
        while (direction != Direction.NONE) {
            direction = elevator.move();
        }
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(0);

        direction = elevator.move();
        while (direction != Direction.NONE) {
            direction = elevator.move();
        }

        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDownDown() {
        Elevator elevator = new Elevator(1, 10, new AtomicBoolean(false));
        MoveRequest request = new MoveRequest(5, 1);
        elevator.queueRequest(request);
        Direction direction = elevator.move();
        while (direction != Direction.NONE) {
            direction = elevator.move();
        }
        assertThat(elevator.getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(1);

        direction = elevator.move();
        while (direction != Direction.NONE) {
            direction = elevator.move();
        }

        assertThat(elevator.getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevator.getDownQueue().size()).isEqualTo(0);
    }

    private Elevator topFloorElevator() {
        Elevator elevator = new Elevator(1, 10, new AtomicBoolean(false));
        MoveRequest request = new MoveRequest(1, 10);
        elevator.queueRequest(request);
        while (elevator.move() != Direction.NONE) {
            elevator.move();
        }
        return elevator;
    }
}
