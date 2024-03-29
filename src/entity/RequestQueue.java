package entity;

import constants.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestQueue {
    private final List<Passenger> passengers;

    private final HashMap<Integer, ArrayList<Passenger>> requestQueueByFromFloor;

    private final HashMap<Integer, ArrayList<Passenger>> requestQueueByToFloor;

    private boolean isEnd;

    public RequestQueue() {
        passengers = new ArrayList<>();
        requestQueueByFromFloor = new HashMap<>();
        requestQueueByToFloor = new HashMap<>();
        this.isEnd = false;
        for (int i = 1; i <= Constants.MAX_FLOOR; i++) {
            requestQueueByFromFloor.put(i, new ArrayList<>());
            requestQueueByToFloor.put(i, new ArrayList<>());
        }
    }

    public synchronized void addPassenger(Passenger passenger) {
        passengers.add(passenger);
        requestQueueByFromFloor.get(passenger.getFrom()).add(passenger);
        requestQueueByToFloor.get(passenger.getTo()).add(passenger);
        notifyAll();
    }

    public synchronized void delPassenger(Passenger passenger) {
        passengers.remove(passenger);
        requestQueueByFromFloor.get(passenger.getFrom()).remove(passenger);
        requestQueueByToFloor.get(passenger.getTo()).remove(passenger);
    }

    public synchronized void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        return passengers.isEmpty();
    }

    public synchronized ArrayList<Passenger> getFromFloor(int floor) {
        return requestQueueByFromFloor.get(floor);
    }

    public synchronized ArrayList<Passenger> getToFloor(int floor) {
        return requestQueueByToFloor.get(floor);
    }

    public synchronized boolean hasNoFromFloorReq(int floor) {
        if (requestQueueByFromFloor.get(floor) == null) {
            return true;
        }
        return requestQueueByFromFloor.get(floor).isEmpty();
    }
}
