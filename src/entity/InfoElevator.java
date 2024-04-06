package entity;

import java.util.ArrayList;

public class InfoElevator {
    private final int elevatorId;
    private int currentFloor;
    private boolean direction;

    private final ArrayList<Passenger> elevatorReq;
    private final ArrayList<Passenger> passengers;

    private final boolean isReset;
    private int maxRequestNum;
    private double moveTime;
    private final int resetMaxRequsetNum;
    private final double resetMoveTime;

    public InfoElevator(Elevator elevator) {
        this.elevatorId = elevator.getElevatorId();
        currentFloor = elevator.getCurrentFloor();
        direction = elevator.getDirection();

        isReset = elevator.isReset();
        elevatorReq = elevator.cloneElevatorReq();
        passengers = elevator.clonePassengers();

        resetMaxRequsetNum = elevator.getResetMaxRequestNum();
        resetMoveTime = elevator.getResetMoveTime();
        maxRequestNum = elevator.getMaxRequestNum();
        moveTime = elevator.getMoveTime();
        if (isReset) {
            if (maxRequestNum != resetMaxRequsetNum) {
                maxRequestNum = resetMaxRequsetNum;
            }
            if (moveTime != resetMoveTime) {
                moveTime = resetMoveTime;
            }
        }
    }

    public int getCost(Passenger passenger) {
        double estimate =  ((33 - getDistance(passenger) - passengers.size() * 2.5 -
                elevatorReq.size() * 2.5 + maxRequestNum * 1.3 - moveTime * 17)
                / (getDistance(passenger) * moveTime));
        return (int) estimate;
    }

    private int getDistance(Passenger passenger) {
        if (currentFloor == passenger.getFrom()) {
            return 0;
        }
        if (passenger.isFront(currentFloor, direction)) {
            return passenger.getFrom() - currentFloor;
        } else {
            int reverseFloor = getReverseFloor();
            return Math.abs(reverseFloor - currentFloor) +
                    Math.abs(passenger.getFrom() - reverseFloor);
        }
    }

    private int getReverseFloor() {
        int desFloor = currentFloor;
        if (direction) {
            for (Passenger passenger : passengers) {
                if (passenger.getTo() > desFloor) {
                    desFloor = passenger.getTo();
                }
            }
            for (Passenger req : elevatorReq) {
                if (req.getFrom() > desFloor) {
                    desFloor = req.getFrom();
                }
            }
        } else {
            for (Passenger passenger : passengers) {
                if (passenger.getTo() < desFloor) {
                    desFloor = passenger.getTo();
                }
            }
            for (Passenger req : elevatorReq) {
                if (req.getFrom() < desFloor) {
                    desFloor = req.getFrom();
                }
            }
        }
        return desFloor;
    }

    public int getElevatorId() {
        return elevatorId;
    }
}
