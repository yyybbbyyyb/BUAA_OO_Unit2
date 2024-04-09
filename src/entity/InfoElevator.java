package entity;

import java.util.ArrayList;

public class InfoElevator {
    private final int elevatorId;
    private int currentFloor;
    private boolean direction;

    private ArrayList<Passenger> elevatorReq;
    private ArrayList<Passenger> passengers;

    private boolean isReset;               //未参与计算评价的电梯信息
    private int maxRequestNum;
    private double moveTime;

    public InfoElevator(Elevator elevator) {
        this.elevatorId = elevator.getElevatorId();
        elevatorReq = new ArrayList<>();
        passengers = new ArrayList<>();
    }

    public synchronized int getCost(Passenger passenger) {
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

    public synchronized void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public synchronized void setDirection(boolean direction) {
        this.direction = direction;
    }

    public synchronized void setElevatorReq(ArrayList<Passenger> elevatorReq) {
        this.elevatorReq.clear();
        this.elevatorReq.addAll(elevatorReq);
    }

    public synchronized void setPassengers(ArrayList<Passenger> passengers) {
        this.passengers.clear();
        this.passengers.addAll(passengers);
    }

    public synchronized void setReset(boolean reset) {
        isReset = reset;
    }

    public synchronized void setMaxRequestNum(int maxRequestNum) {
        this.maxRequestNum = maxRequestNum;
    }

    public synchronized void setMoveTime(double moveTime) {
        this.moveTime = moveTime;
    }

}
