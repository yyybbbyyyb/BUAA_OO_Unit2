package Config;

import entity.Elevator;

import java.util.concurrent.ConcurrentHashMap;


public class Elevators {
    private static final ConcurrentHashMap<Integer, Elevator> elevators = new ConcurrentHashMap<>();

    public static void addElevator(Elevator elevator) {
        elevators.put(elevator.getElevatorId(), elevator);
    }

    public static Elevator getElevator(int id) {
        return elevators.get(id);
    }

    public static ConcurrentHashMap<Integer, Elevator> getElevators() {
        return elevators;
    }
}