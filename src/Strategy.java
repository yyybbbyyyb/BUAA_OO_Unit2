public interface Strategy {
    ElevatorState getNextState(Elevator elevator);
}
