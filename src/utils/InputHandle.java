package utils;

import config.Elevators;
import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ResetRequest;

import constants.Constants;
import entity.Passenger;
import entity.RequestQueue;

import java.io.IOException;
import java.util.HashMap;

public class InputHandle extends Thread {

    private static InputHandle instance;

    private final RequestQueue globalReq = new RequestQueue();

    private final HashMap<Integer, RequestQueue> elevatorReqs = new HashMap<>();

    private final Counter counter = new Counter();

    public static InputHandle getInstance() {
        if (instance == null) {
            instance = new InputHandle();
        }
        return instance;
    }

    public InputHandle() {
        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            elevatorReqs.put(i, new RequestQueue());
        }
    }

    public void addPassenger(Passenger passenger, Boolean isNewPassenger) {
        globalReq.addPassenger(passenger, true, -1);
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
                synchronized (counter) {
                    if (counter.getCount() == 0) {
                        globalReq.setEnd(true);
                        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
                            elevatorReqs.get(i).setEnd(true);
                        }
                        break;
                    } else {
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
                    Elevators.getElevator(elevatorId).initReset(resetRequest.getCapacity(),
                            resetRequest.getSpeed());
                }
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RequestQueue getGlobalReq() {
        return globalReq;
    }

    public RequestQueue getElevatorReq(int elevatorId) {
        return elevatorReqs.get(elevatorId);
    }

    public Counter getCounter() {
        return counter;
    }

}
