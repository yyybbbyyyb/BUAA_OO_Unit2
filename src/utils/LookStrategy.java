package utils;

import constants.Constants;
import constants.ElevatorState;
import entity.Elevator;
import entity.Passenger;
import entity.RequestQueue;

import java.util.ArrayList;

public class LookStrategy implements Strategy {

    private final RequestQueue requestQueue;

    public LookStrategy(int elevatorId) {
        this.requestQueue = InputHandler.getInstance().getRequestQueue(elevatorId);
    }

    @Override
    public ElevatorState getNextState(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        int passengerNum = elevator.getPassengerNum();
        boolean direction = elevator.getDirection();
        ArrayList<Passenger> passengers = elevator.getPassengers();
        boolean isReset = elevator.isReset();
        int MAX_REQUEST_NUM = elevator.getMaxRequestNum();
        if (isReset) {
            return ElevatorState.RESET;
        }
        if (canOpenElevatorForOut(currentFloor, passengers)
            || canOpenElevatorForIn(currentFloor, passengerNum, direction, MAX_REQUEST_NUM)) {
            return ElevatorState.OPEN;
        }
        if (passengerNum != 0) {
            return ElevatorState.MOVE;
        } else {
            if (requestQueue.isEmpty()) {
                if (requestQueue.isEnd()) {
                    return ElevatorState.OVER;
                } else {
                    return ElevatorState.WAITING;
                }
            } else {
                if (hasReqInDirection(currentFloor, direction)) {
                    return ElevatorState.MOVE;
                } else {
                    return ElevatorState.REVERSE;
                }
            }
        }
    }

    private boolean canOpenElevatorForOut(int currentFloor, ArrayList<Passenger> passengers) {
        for (Passenger passenger : passengers) {
            if (passenger.getTo() == currentFloor) {
                return true;
            }
        }
        return false;
    }

    private boolean canOpenElevatorForIn(int currentFloor, int currentRequestNum,
                                         boolean direction, int MAX_REQUEST_NUM) {
        if (currentRequestNum == MAX_REQUEST_NUM) {
            return false;
        }
        synchronized (requestQueue) {
            if (requestQueue.hasNoFromFloorReq(currentFloor)) {
                return false;
            }
            for (Passenger passenger : requestQueue.getFromFloor(currentFloor)) {
                if (passenger.isSameDirection(currentFloor, direction)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean hasReqInDirection(int currentFloor, boolean direction) {
        int endFloor = direction ? Constants.MAX_FLOOR : Constants.INIT_FLOOR;
        int step = direction ? -1 : 1;

        synchronized (requestQueue) {
            for (int i = endFloor; i != currentFloor; i += step) {
                if (!requestQueue.hasNoFromFloorReq(i)) {
                    return true;
                }
            }
            return false;
        }
    }
}
