import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

public class Elevator extends Thread {
    private final int elevatorId;

    private int currentFloor;

    private boolean direction;        //二元性电梯只能向上向下，故起始方向向上，为true

    private final RequestQueue requestQueue;

    private final Strategy strategy;

    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        currentFloor = Constants.INIT_FLOOR;
        direction = true;
        requestQueue = new RequestQueue(elevatorId);
        strategy = new LookStrategy(elevatorId);
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
                synchronized (InputHandler.getInstance().getRequestQueue(elevatorId)) {
                    try {
                        InputHandler.getInstance().getRequestQueue(elevatorId).wait();
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

    private void openAndClose() {
        TimableOutput.println(String.format("OPEN-%d-%d", currentFloor, elevatorId));
        try {
            Thread.sleep((long) (Constants.OPEN_TIME * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            if (requestQueue.isEmpty()) {
                break;
            }
            PersonRequest req = requestQueue.gerOneRequestAndRemove(currentFloor, IndexType.BY_TO);
            if (req == null) {
                break;
            }
            TimableOutput.println(String.format("OUT-%d-%d-%d", req.getPersonId(),
                        currentFloor, elevatorId));
        }
        while (true) {
            if (InputHandler.getInstance().getRequestQueue(elevatorId).isEmpty()) {
                break;
            }
            PersonRequest req = InputHandler.getInstance().getRequestQueue(elevatorId).
                    gerOneRequestAndRemove(currentFloor, IndexType.BY_FROM);
            if (req == null) {
                break;
            }
            if (requestQueue.getCurrentRequestNum() == Constants.MAX_REQUEST_NUM) {
                break;
            }
            requestQueue.addRequest(req);
            TimableOutput.println(String.format("IN-%d-%d-%d", req.getPersonId(),
                    currentFloor, elevatorId));
        }
        try {
            Thread.sleep((long) (Constants.CLOSE_TIME * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TimableOutput.println(String.format("CLOSE-%d-%d", currentFloor, elevatorId));
    }


    public int getElevatorId() {
        return elevatorId;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getCurrentRequestNum() {
        return requestQueue.getCurrentRequestNum();
    }

    public boolean getDirection() {
        return direction;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

}
