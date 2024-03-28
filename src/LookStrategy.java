public class LookStrategy implements Strategy {

    private final int elevatorId;

    private final RequestQueue inputRequestQueue;

    public LookStrategy(int elevatorId) {
        this.elevatorId = elevatorId;
        this.inputRequestQueue = InputHandler.getInstance().getRequestQueue(elevatorId);
    }


    @Override
    public ElevatorState getNextState(Elevator elevator) {
        int currentFloor = elevator.getCurrentFloor();
        int currentRequestNum = elevator.getCurrentRequestNum();
        boolean currentDirection = elevator.getDirection();
        RequestQueue elevatorRequestQueue = elevator.getRequestQueue();
        if (canOpenElevatorForOut(currentFloor, elevatorRequestQueue)
            || canOpenElevatorForIn(currentFloor, currentRequestNum) ) {
            return ElevatorState.OPEN;
        }
        if (currentRequestNum != 0) {
            if (hasNoFollowDirection(currentFloor, currentDirection, elevatorRequestQueue)) {
                return ElevatorState.REVERSE;
            }
            return ElevatorState.MOVE;
        } else {
            if (inputRequestQueue.getCurrentRequestNum() == 0) {
                if (InputHandler.getInstance().getRequestQueue(elevatorId).isEnd()) {
                    return ElevatorState.OVER;
                } else {
                    return ElevatorState.WAITING;
                }
            } else {
                if (hasReqInInputQueueInDirection(currentFloor, currentDirection)) {
                    return ElevatorState.MOVE;
                } else {
                    return ElevatorState.REVERSE;
                }
            }
        }
    }

    private boolean canOpenElevatorForOut(int currentFloor, RequestQueue requestQueue) {
        return !requestQueue.getToFloor(currentFloor).isEmpty();
    }

    private boolean canOpenElevatorForIn(int currentFloor, int currentRequestNum) {
        if (currentRequestNum == Constants.MAX_REQUEST_NUM) {
            return false;
        }
        return !inputRequestQueue.getFromFloor(currentFloor).isEmpty();
    }

    private boolean hasReqInInputQueueInDirection(int currentFloor, boolean direction) {
        int endFloor = direction ? Constants.MAX_FLOOR : Constants.INIT_FLOOR;
        int step = direction ? 1 : -1;

        for (int i = currentFloor; i != endFloor + step; i += step) {
            if (!inputRequestQueue.getFromFloor(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNoFollowDirection(int currentFloor, boolean direction, RequestQueue requestQueue) {
        int endFloor = direction ? Constants.MAX_FLOOR : Constants.INIT_FLOOR;
        int step = direction ? 1 : -1;

        for (int i = currentFloor; i != endFloor + step; i += step) {
            if (!requestQueue.getToFloor(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
