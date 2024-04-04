package entity;

import com.oocourse.elevator2.TimableOutput;
import constants.Constants;
import constants.ElevatorState;
import utils.InputHandler;
import utils.LookStrategy;
import utils.Strategy;

import java.util.ArrayList;
import java.util.List;

public class Elevator extends Thread {
    private final int elevatorId;
    private int currentFloor;
    private boolean direction;                   //二元性电梯只能向上向下，故起始方向向上，为true
    private final RequestQueue requestQueue;
    private final ArrayList<Passenger> passengers;
    private final Strategy strategy;

    private boolean isReset = false;
    private int MAX_REQUEST_NUM = 6;
    private double MOVE_TIME = 0.4;

    private int RESET_MAX_REQUEST_NUM;
    private double RESET_MOVE_TIME;


    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        currentFloor = Constants.INIT_FLOOR;
        direction = true;
        passengers = new ArrayList<>();
        strategy = new LookStrategy(elevatorId);
        requestQueue = InputHandler.getInstance().getRequestQueue(elevatorId);
    }

    @Override
    public void run() {
        while (true) {
            ElevatorState nextElevatorState = strategy.getNextState(this);
            if (nextElevatorState == ElevatorState.OVER) {
                break;
            } else if (nextElevatorState == ElevatorState.RESET) {
                reset();
            } else if (nextElevatorState == ElevatorState.MOVE) {
                move();
            } else if (nextElevatorState == ElevatorState.REVERSE) {
                direction = !direction;
            } else if (nextElevatorState == ElevatorState.WAITING) {
                synchronized (requestQueue) {
                    try {
                        requestQueue.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (nextElevatorState == ElevatorState.OPEN) {
                openAndClose();
            }
        }
    }

    public void initReset(int MAX_REQUEST_NUM, double MOVE_TIME) {
        synchronized (requestQueue) {
            this.RESET_MAX_REQUEST_NUM = MAX_REQUEST_NUM;
            this.RESET_MOVE_TIME = MOVE_TIME;
            isReset = true;
            requestQueue.notifyAll();
        }
    }

    private synchronized void reset() {
        if (!passengers.isEmpty()) {
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
                    InputHandler.getInstance().getCounter().decrement(1);

                } else {
                    passenger.setFrom(currentFloor);
                    passenger.setByElevatorId(-1);
                    passenger.setServed(false);
                    InputHandler.getInstance().addPassenger(passenger, false);
                }
            }
            TimableOutput.println(String.format("CLOSE-%d-%d", currentFloor, elevatorId));
            for (Passenger passenger : passengersToRemove) {
                passengers.remove(passenger);
            }
        }
        MAX_REQUEST_NUM = RESET_MAX_REQUEST_NUM;
        MOVE_TIME = RESET_MOVE_TIME;
        TimableOutput.println(String.format("RESET_BEGIN-%d", elevatorId));
        List<Passenger> passengersToRemove2 = new ArrayList<>();
        for (Passenger passenger : requestQueue.getPassengers()) {
            passenger.setByElevatorId(-1);
            passenger.setServed(false);
            passengersToRemove2.add(passenger);
            InputHandler.getInstance().addPassenger(passenger, false);
        }
        for (Passenger passenger : passengersToRemove2) {
            requestQueue.delPassenger(passenger);
        }
        try {
            Thread.sleep((long) (Constants.RESET_TIME * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("RESET_END-%d", elevatorId));
        isReset = false;
        notifyAll();
    }

    private void move() {
        //TODO:弹射起步？


        try {
            Thread.sleep((long) (MOVE_TIME * 1000));
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
        if (passengers.isEmpty()) {
            return;
        }
        List<Passenger> passengersToRemove = new ArrayList<>();
        for (Passenger passenger : passengers) {
            if (passenger.getTo() == currentFloor) {
                TimableOutput.println(String.format("OUT-%d-%d-%d", passenger.getId(),
                        currentFloor, elevatorId));
                passengersToRemove.add(passenger);
                InputHandler.getInstance().getCounter().decrement(1);
            }
        }
        for (Passenger passenger : passengersToRemove) {
            passengers.remove(passenger);
        }
    }

    private void inPassenger() {
        synchronized (requestQueue) {
            if (!requestQueue.hasNoFromFloorReq(currentFloor)) {
                ArrayList<Passenger> requests = requestQueue.getFromFloor(currentFloor);
                List<Passenger> requestsToRemove = new ArrayList<>();
                for (Passenger req : requests) {
                    if (req.isSameDirection(currentFloor, direction)) {
                        passengers.add(req);
                        TimableOutput.println(String.format("IN-%d-%d-%d", req.getId(),
                                currentFloor, elevatorId));
                        requestsToRemove.add(req);
                        if (getPassengerNum() == MAX_REQUEST_NUM) {
                            break;
                        }
                    }
                }
                for (Passenger req : requestsToRemove) {
                    requestQueue.delPassenger(req);
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

    public synchronized boolean isReset() {
        return isReset;
    }

    public void setMAX_REQUEST_NUM(int MAX_REQUEST_NUM) {
        this.MAX_REQUEST_NUM = MAX_REQUEST_NUM;
    }

    public void setMOVE_TIME(double MOVE_TIME) {
        this.MOVE_TIME = MOVE_TIME;
    }

    public int getMaxRequestNum() {
        return MAX_REQUEST_NUM;
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

    public ArrayList<Passenger> getPassengers() {
        return passengers;
    }

}
