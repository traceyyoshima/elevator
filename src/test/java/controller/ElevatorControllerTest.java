package controller;

import org.example.controller.ElevatorController;
import org.example.model.Direction;
import org.example.model.MoveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class ElevatorControllerTest {

    private final int topFloor = 10;
    private ElevatorController elevatorController;

    @BeforeEach
    void setUp() {
        elevatorController = new ElevatorController(1, 10, new AtomicBoolean(false));
    }

    @Test
    void moveUp() {
        MoveRequest request = new MoveRequest(1, 5);
        elevatorController.queueRequest(request);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevatorController.getElevator().getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDown() {
        ElevatorController elevatorController = topFloorElevator();
        MoveRequest request = new MoveRequest(5, 1);
        elevatorController.queueRequest(request);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.currentFloor());

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevatorController.getElevator().getDownQueue().size()).isEqualTo(0);
    }

    @Test
    void moveUpDown() {
        MoveRequest request = new MoveRequest(5, 1);
        elevatorController.queueRequest(request);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevatorController.getElevator().getUpQueue().size()).isEqualTo(0);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevatorController.getElevator().getDownQueue().size()).isEqualTo(0);
    }

    @Test
    void moveUpUp() {
        MoveRequest request = new MoveRequest(5, 10);
        elevatorController.queueRequest(request);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevatorController.getElevator().getUpQueue().size()).isEqualTo(1);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevatorController.getElevator().getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDownUp() {
        ElevatorController elevatorController = topFloorElevator();
        MoveRequest request = new MoveRequest(5, topFloor);
        elevatorController.queueRequest(request);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevatorController.getElevator().getDownQueue().size()).isEqualTo(0);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevatorController.getElevator().getUpQueue().size()).isEqualTo(0);
    }

    @Test
    void moveDownDown() {
        MoveRequest request = new MoveRequest(5, 1);
        elevatorController.queueRequest(request);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.currentFloor());
        assertThat(elevatorController.getElevator().getDownQueue().size()).isEqualTo(1);

        moveElevator(elevatorController);
        assertThat(elevatorController.getElevator().getCurrentFloor()).isEqualTo(request.targetFloor());
        assertThat(elevatorController.getElevator().getDownQueue().size()).isEqualTo(0);
    }

    private void moveElevator(ElevatorController elevatorController) {
        int count = 1;
        Direction direction = elevatorController.move();
        while (direction != Direction.NONE) {
            if (count > topFloor) {
                throw new IllegalStateException("Elevator is stuck");
            }

            direction = elevatorController.move();
            count++;
        }
    }

    private ElevatorController topFloorElevator() {
        MoveRequest request = new MoveRequest(1, topFloor);
        elevatorController.queueRequest(request);
        moveElevator(elevatorController);
        return elevatorController;
    }
}
