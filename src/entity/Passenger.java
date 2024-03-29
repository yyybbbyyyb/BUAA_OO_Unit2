package entity;

import com.oocourse.elevator1.PersonRequest;

public class Passenger {
    private final int id;
    private final int from;
    private final int to;
    private final int byElevatorId;

    public Passenger(int id, int from, int to, int byElevatorId) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.byElevatorId = byElevatorId;
    }

    public int getId() {
        return id;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getByElevatorId() {
        return byElevatorId;
    }

    public boolean isSameDirection(int currentFloor, boolean direction) {
        return (to - currentFloor > 0 && direction) || (to - currentFloor < 0 && !direction);
    }

    public static Passenger reqToPassenger(PersonRequest request) {
        return new Passenger(request.getPersonId(), request.getFromFloor(),
                request.getToFloor(), request.getElevatorId());
    }
}
