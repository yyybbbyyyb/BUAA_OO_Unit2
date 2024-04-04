package utils;

import Config.Elevators;
import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ResetRequest;

import constants.Constants;
import entity.Elevator;
import entity.Passenger;
import entity.RequestQueue;

import java.io.IOException;
import java.util.HashMap;

public class InputHandler extends Thread {

    private static InputHandler instance;

    private final RequestQueue globalReq = new RequestQueue();

    private final HashMap<Integer, RequestQueue> elevatorReq = new HashMap<>();

    private final Counter counter = new Counter();

    public static InputHandler getInstance() {
        if (instance == null) {
            instance = new InputHandler();
        }
        return instance;
    }

    public InputHandler() {
        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            elevatorReq.put(i, new RequestQueue());
        }
    }

    public synchronized void addPassenger(Passenger passenger, Boolean isNewPassenger) {
        globalReq.addPassenger(passenger);
        if (isNewPassenger) {
            counter.increment(1);
        }
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                if (counter.getCount() == 0) {
                    globalReq.setEnd(true);
                    for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
                        elevatorReq.get(i).setEnd(true);
                    }
                    break;
                } else {
                    synchronized (counter) {
                        try {
                            counter.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } else {
                if (request instanceof PersonRequest) {
                    Passenger passenger = Passenger.reqToPassenger((PersonRequest) request);
                    addPassenger(passenger, true);
                } else if (request instanceof ResetRequest) {
                    int elevatorId = ((ResetRequest) request).getElevatorId();
                    ResetRequest resetRequest = (ResetRequest) request;
                    Elevators.getElevator(elevatorId).initReset(resetRequest.getCapacity(), resetRequest.getSpeed());
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RequestQueue getGlobalRequestQueue() {
        return globalReq;
    }

    public RequestQueue getRequestQueue(int elevatorId) {
        return elevatorReq.get(elevatorId);
    }

    public Counter getCounter() {
        return counter;
    }

}
