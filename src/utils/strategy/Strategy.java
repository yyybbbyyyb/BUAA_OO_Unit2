package utils.strategy;

import constants.ElevatorState;
import entity.Elevator;

public interface Strategy {
    ElevatorState getNextState(Elevator elevator);
}
