package entity;

import com.oocourse.elevator2.TimableOutput;
import constants.Constants;
import constants.ElevatorState;
import utils.InputHandle;
import utils.strategy.LookStrategy;
import utils.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;

public class Elevator extends Thread {
    private final int elevatorId;
    private int currentFloor;
    private boolean direction;                      //二元性电梯只能向上向下，故起始方向向上，为true
    private final Strategy strategy;

    private final RequestQueue elevatorReq;
    private final ArrayList<Passenger> passengers;

    private boolean isReset = false;
    private int maxRequestNum = 6;
    private double moveTime = 0.4;

    private int resetMaxRequestNum;
    private double resetMoveTime;

    private ElevatorState elevatorState;

    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        currentFloor = Constants.INIT_FLOOR;
        direction = true;
        strategy = new LookStrategy(elevatorId);
        elevatorState = ElevatorState.WAITING;

        passengers = new ArrayList<>();
        elevatorReq = InputHandle.getInstance().getElevatorReq(elevatorId);
    }

    @Override
    public void run() {
        while (true) {
            ElevatorState nextElevatorState = strategy.getNextState(this);
            elevatorState = nextElevatorState;
            if (nextElevatorState == ElevatorState.OVER) {
                break;
            } else if (nextElevatorState == ElevatorState.RESET) {
                reset();
            } else if (nextElevatorState == ElevatorState.MOVE) {
                move();
            } else if (nextElevatorState == ElevatorState.REVERSE) {
                direction = !direction;
            } else if (nextElevatorState == ElevatorState.WAITING) {
                synchronized (elevatorReq) {
                    try {
                        elevatorReq.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (nextElevatorState == ElevatorState.OPEN) {
                openAndClose();
            }
        }
    }

    public void initReset(int maxRequestNum, double moveTime) {
        synchronized (elevatorReq) {
            this.resetMaxRequestNum = maxRequestNum;
            this.resetMoveTime = moveTime;
            isReset = true;
            elevatorReq.notifyAll();
        }
    }

    private void reset() {
        outPassenger(isReset);
        maxRequestNum = resetMaxRequestNum;
        moveTime = resetMoveTime;
        synchronized (elevatorReq) {
            TimableOutput.println(String.format("RESET_BEGIN-%d", elevatorId));
            List<Passenger> passengersToRemove = new ArrayList<>();
            for (Passenger passenger : elevatorReq.getPassengers()) {
                passenger.setByElevatorId(-1);
                passengersToRemove.add(passenger);
                InputHandle.getInstance().addPassenger(passenger, false);
            }
            for (Passenger passenger : passengersToRemove) {
                elevatorReq.delPassenger(passenger);
            }
            try {
                Thread.sleep((long) (Constants.RESET_TIME * 1000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            TimableOutput.println(String.format("RESET_END-%d", elevatorId));
            isReset = false;
        }
    }

    private void move() {
        try {
            Thread.sleep((long) (moveTime * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (direction) {
            currentFloor++;
        } else {
            currentFloor--;
        }
        TimableOutput.println(String.format("ARRIVE-%d-%d", currentFloor, elevatorId));
    }

    private void outPassenger() {
        synchronized (passengers) {
            if (passengers.isEmpty()) {
                return;
            }
            List<Passenger> passengersToRemove = new ArrayList<>();
            for (Passenger passenger : passengers) {
                if (passenger.getTo() == currentFloor) {
                    TimableOutput.println(String.format("OUT-%d-%d-%d", passenger.getId(),
                            currentFloor, elevatorId));
                    passengersToRemove.add(passenger);
                    InputHandle.getInstance().getCounter().decrement(1);
                }
            }
            for (Passenger passenger : passengersToRemove) {
                passengers.remove(passenger);
            }
        }
    }

    private void outPassenger(Boolean isReset) {
        synchronized (passengers) {
            if (!passengers.isEmpty()) {
                setElevatorState(ElevatorState.OPEN);
                TimableOutput.println(String.format("OPEN-%d-%d", currentFloor, elevatorId));
                try {
                    Thread.sleep((long) (Constants.OPEN_CLOSE_TIME * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                List<Passenger> passengersToRemove = new ArrayList<>();
                for (Passenger passenger : passengers) {
                    TimableOutput.println(String.format("OUT-%d-%d-%d", passenger.getId(),
                            currentFloor, elevatorId));
                    passengersToRemove.add(passenger);
                    if (passenger.getTo() == currentFloor) {
                        InputHandle.getInstance().getCounter().decrement(1);

                    } else {
                        passenger.setFrom(currentFloor);
                        passenger.setByElevatorId(-1);
                        InputHandle.getInstance().addPassenger(passenger, false);
                    }
                }
                TimableOutput.println(String.format("CLOSE-%d-%d", currentFloor, elevatorId));
                for (Passenger passenger : passengersToRemove) {
                    passengers.remove(passenger);
                }
                setElevatorState(ElevatorState.RESET);
            }
        }
    }

    private void inPassenger() {
        synchronized (elevatorReq) {
            if (!elevatorReq.hasNoFromFloorReq(currentFloor)) {
                ArrayList<Passenger> requests = elevatorReq.getFromFloor(currentFloor);
                List<Passenger> requestsToRemove = new ArrayList<>();
                synchronized (passengers) {
                    for (Passenger req : requests) {
                        if (req.isSameDirection(currentFloor, direction)) {
                            passengers.add(req);
                            TimableOutput.println(String.format("IN-%d-%d-%d", req.getId(),
                                    currentFloor, elevatorId));
                            requestsToRemove.add(req);
                            if (getPassengerNum() == maxRequestNum) {
                                break;
                            }
                        }
                    }
                }
                for (Passenger req : requestsToRemove) {
                    elevatorReq.delPassenger(req);
                }
            }
        }
    }

    private void openAndClose() {
        TimableOutput.println(String.format("OPEN-%d-%d", currentFloor, elevatorId));
        try {
            Thread.sleep((long) (Constants.OPEN_CLOSE_TIME * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        outPassenger();
        ElevatorState state = strategy.getNextState(this);
        if (state == ElevatorState.REVERSE) {
            direction = !direction;
        }
        inPassenger();
        TimableOutput.println(String.format("CLOSE-%d-%d", currentFloor, elevatorId));
    }

    public boolean isReset() {
        return isReset;
    }

    public int getMaxRequestNum() {
        return maxRequestNum;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getPassengerNum() {
        return passengers.size();
    }

    public boolean getDirection() {
        return direction;
    }

    public ArrayList<Passenger> clonePassengers() {
        return new ArrayList<>(this.passengers);
    }

    public ArrayList<Passenger> cloneElevatorReq() {
        ArrayList<Passenger> passengers = elevatorReq.getPassengers();
        return new ArrayList<>(passengers);
    }

    public double getMoveTime() {
        return moveTime;
    }

    public ElevatorState getElevatorState() {
        return elevatorState;
    }

    public synchronized void setElevatorState(ElevatorState elevatorState) {
        this.elevatorState = elevatorState;
    }

    public int getResetMaxRequestNum() {
        return resetMaxRequestNum;
    }

    public double getResetMoveTime() {
        return resetMoveTime;
    }
}
