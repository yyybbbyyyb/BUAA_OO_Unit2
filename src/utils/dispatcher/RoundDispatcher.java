package utils.dispatcher;

import entity.Passenger;

public class RoundDispatcher implements Dispatcher {
    private int elevatorId = 1;

    private int getNextElevatorId() {
        elevatorId++;
        if (elevatorId > 6) {
            elevatorId = 1;
        }
        return elevatorId;
    }

    @Override
    public int getElevatorId(Passenger passenger) {
        return getNextElevatorId();
    }
}
