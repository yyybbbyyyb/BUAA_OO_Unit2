package entity;

import constants.ElevatorState;

import java.util.ArrayList;
import java.util.List;

public class ShadowElevator {
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

    private final ElevatorState elevatorState;

    private double costTime = 0;

    public ShadowElevator(Elevator elevator) {
        this.elevatorId = elevator.getElevatorId();
        currentFloor = elevator.getCurrentFloor();
        direction = elevator.getDirection();

        isReset = elevator.isReset();
        elevatorReq = elevator.cloneElevatorReq();
        passengers = elevator.clonePassengers();

        elevatorState = elevator.getElevatorState();
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

    private double fullCost = 600;

    public void calculateCostTime(Passenger passenger) {
        double time = 0;
        int desFloor = passenger.getFrom();
        time += dealCurrentFloor(passenger);
        if (desFloor == currentFloor) {
            this.costTime = time;
            return;
        }
        int step = direction ? 1 : -1;
        if (!passenger.isFront(currentFloor, direction)) {
            int reverseFloor = getReverseFloor();
            for (int floor = currentFloor; floor != reverseFloor; floor += step) {
                time += dealNormalFloor();
            }
            if (needOpen()) {
                time += 0.4;
                OpenAndClose();
            }
            direction = !direction;
            step = -step;
            for (int floor = reverseFloor; floor != desFloor; floor += step) {
                time += dealNormalFloor();
            }
            if (getPassengerNum() == maxRequestNum) {
                time += fullCost;
            }
            this.costTime = time;
        } else {
            for (int floor = currentFloor; floor != desFloor; floor += step) {
                time += dealNormalFloor();
            }
            if (getPassengerNum() == maxRequestNum) {
                time += fullCost;
            }
            this.costTime = time;
        }
    }

    private final double resetOpenCost = 0.2 + 1.2;
    private final double resetNoOpenCost = 0.4 + 1.2;
    private final double resetFinishOpenCost = 0.6;
    private final double moveCost = moveTime / 2;
    private final double openCost = 0.2;            //TODO:这种设定考虑是否应用量子电梯？总之不要关开

    private double dealCurrentFloor(Passenger passenger) {
        double cost = 0;
        if (isReset) {
            if (elevatorState == ElevatorState.OPEN) {
                cost += resetOpenCost;
            } else {
                if (!passengers.isEmpty()) {
                    cost += resetNoOpenCost;
                } else {
                    cost += resetFinishOpenCost;
                }
            }
            passengers.clear();
        } else {
            if (elevatorState == ElevatorState.MOVE) {
                cost += moveCost;
                if (direction) {
                    currentFloor++;
                } else {
                    currentFloor--;
                }
            } else if (elevatorState == ElevatorState.OPEN) {
                cost += openCost;
                OpenAndClose();
            } else if (elevatorState == ElevatorState.WAITING ||
                    elevatorState == ElevatorState.REVERSE) {
                if (!passenger.isFront(currentFloor, direction)) {
                    direction = !direction;
                }
            }
        }
        return cost;
    }

    private double dealNormalFloor() {
        double cost = 0;
        if (needOpen()) {
            cost += 0.4;
            OpenAndClose();
        }
        cost += moveTime;
        if (direction) {
            currentFloor++;
        } else {
            currentFloor--;
        }
        return cost;
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

    private boolean needOpen() {
        for (Passenger passenger : passengers) {
            if (passenger.getTo() == currentFloor) {
                return true;
            }
        }
        for (Passenger req : elevatorReq) {
            if (req.getFrom() == currentFloor && req.isSameDirection(currentFloor, direction)
                    && getPassengerNum() < maxRequestNum) {
                return true;
            }
        }
        return false;
    }

    private void OpenAndClose() {
        passengers.removeIf(passenger -> passenger.getTo() == currentFloor);
        ArrayList<Passenger> requests = new ArrayList<>();
        for (Passenger req : elevatorReq) {
            if (req.getFrom() == currentFloor) {
                requests.add(req);
            }
        }
        List<Passenger> requestsToRemove = new ArrayList<>();
        for (Passenger req : requests) {
            if (req.isSameDirection(currentFloor, direction)) {
                passengers.add(req);
                requestsToRemove.add(req);
                if (getPassengerNum() == maxRequestNum) {
                    break;
                }
            }
        }
        for (Passenger req : requestsToRemove) {
            elevatorReq.remove(req);
        }
    }

    private int getPassengerNum() {
        return passengers.size();
    }

    public double getCostTime() {
        return costTime;
    }

    public int getElevatorId() {
        return elevatorId;
    }
}
