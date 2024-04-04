package utils.dispatcher;

import entity.Passenger;

public class ShadowDispatcher implements Dispatcher {
    @Override
    public int getElevatorId(Passenger passenger) {
        return 0;
    }

}
