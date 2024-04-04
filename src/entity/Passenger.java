package entity;

import com.oocourse.elevator2.PersonRequest;

public class Passenger {
    private final int id;
    private int from;
    private final int to;
    private int byElevatorId;
    private boolean isServed;

    public Passenger(int id, int from, int to) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.byElevatorId = -1;
        this.isServed = false;
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

    public void setFrom(int from) {
        this.from = from;
    }

    public void setByElevatorId(int byElevatorId) {
        this.byElevatorId = byElevatorId;
    }

    public void setServed(boolean isServed) {
        this.isServed = isServed;
    }

    public boolean isSameDirection(int currentFloor, boolean direction) {
        return (to - currentFloor > 0 && direction) || (to - currentFloor < 0 && !direction);
    }

    public static Passenger reqToPassenger(PersonRequest request) {
        return new Passenger(request.getPersonId(), request.getFromFloor(),
                request.getToFloor());
    }
}
