package utils;

import config.Elevators;
import entity.Elevator;
import entity.Passenger;
import entity.RequestQueue;
import utils.dispatcher.Dispatcher;
import utils.dispatcher.EstimateDispatcher;
import utils.dispatcher.RoundDispatcher;

import java.util.ArrayList;

public class Dispatch extends Thread {

    private final Dispatcher roundDispatcher = new RoundDispatcher();

    private final Dispatcher estimateDispatcher = new EstimateDispatcher();

    private final RequestQueue globalReq = InputHandle.getInstance().getGlobalReq();

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
        ArrayList<Passenger> removeList = new ArrayList<>();
        ArrayList<Passenger> passengers = globalReq.getPassengers();
        for (Passenger passenger: passengers) {
            int elevatorId = estimateDispatcher.getElevatorId(passenger);    //可能是个RESET的电梯，但是没关系
            if (elevatorId == -1) {
                elevatorId = roundDispatcher.getElevatorId(passenger);
            }
            InputHandle.getInstance().getElevatorReq(elevatorId).addPassenger(passenger,
                    false, elevatorId);
            Elevators.getElevator(elevatorId).getInfoElevator().setElevatorReq(
                    InputHandle.getInstance().getElevatorReq(elevatorId).getPassengers());
            removeList.add(passenger);
        }

        for (Passenger passenger: removeList) {
            globalReq.delPassenger(passenger);
        }

    }
}
