import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Elevator extends Thread {
    private final int elevatorId;
    private int currentFloor;
    private boolean direction;        //二元性电梯只能向上向下，故起始方向向上，为true
    private final RequestQueue requestQueue;
    private final ArrayList<Passenger> passengers;
    private final Strategy strategy;

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

    private void move() {
        try {
            Thread.sleep((long) (Constants.MOVE_TIME * 1000));
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
                        if (getPassengerNum() == Constants.MAX_REQUEST_NUM) {
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
        outPassenger();
        try {
            Thread.sleep((long) (Constants.OPEN_TIME * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ElevatorState state = strategy.getNextState(this);
        if (state == ElevatorState.REVERSE) {
            direction = !direction;
        }

        inPassenger();
        try {
            Thread.sleep((long) (Constants.CLOSE_TIME * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("CLOSE-%d-%d", currentFloor, elevatorId));
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
