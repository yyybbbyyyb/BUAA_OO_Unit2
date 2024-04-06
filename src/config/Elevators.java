package config;

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
}