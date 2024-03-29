package utils;

import constants.ElevatorState;
import entity.Elevator;

public interface Strategy {
    ElevatorState getNextState(Elevator elevator);
}
