package utils;

import Config.Elevators;
import com.oocourse.elevator2.TimableOutput;
import entity.Elevator;
import entity.Passenger;
import entity.RequestQueue;
import utils.dispatcher.Dispatcher;
import utils.dispatcher.RoundDispatcher;

import java.util.ArrayList;

public class Dispatch extends Thread {

    private final Dispatcher dispatcher = new RoundDispatcher();

    private final RequestQueue globalReq = InputHandler.getInstance().getGlobalRequestQueue();

    private boolean allElevatorsInReset() {
        for (int i = 1; i <= 6; i++) {
            Elevator elevator = Elevators.getElevator(i);
            if (!elevator.isReset()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        while (!globalReq.isEnd()) {
            if (globalReq.isEmpty()) {
                synchronized (globalReq) {
                    try {
                        globalReq.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            dispatchPassenger();
        }
    }

    public void dispatchPassenger() {
        synchronized (this) {
            while (allElevatorsInReset()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        synchronized (globalReq) {
            ArrayList<Passenger> removeList = new ArrayList<>();
            for (Passenger passenger: globalReq.getPassengers()) {
                int elevatorId = dispatcher.getElevatorId(passenger);
                InputHandler.getInstance().getRequestQueue(elevatorId).addPassenger(passenger);
                passenger.setServed(true);
                passenger.setByElevatorId(elevatorId);
                TimableOutput.println(String.format("RECEIVE-%d-%d", passenger.getId(), elevatorId));
                removeList.add(passenger);
            }
            for (Passenger passenger: removeList) {
                globalReq.delPassenger(passenger);
            }
        }
    }
}
