import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;
import java.util.HashMap;

public class InputHandler extends Thread {

    private static InputHandler instance;

    HashMap<Integer, RequestQueue> requestQueuesMap;

    public static InputHandler getInstance() {
        if (instance == null) {
            instance = new InputHandler();
        }
        return instance;
    }

    private InputHandler() {
        requestQueuesMap = new HashMap<>();
        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            requestQueuesMap.put(i, new RequestQueue(i));
        }
    }

    public void addRequest(PersonRequest request) {
        requestQueuesMap.get(request.getElevatorId()).addRequest(request);
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            PersonRequest request = elevatorInput.nextPersonRequest();
            if (request == null) {
                for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
                    requestQueuesMap.get(i).setEnd(true);
                }
                break;
            } else {
                addRequest(request);
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RequestQueue getRequestQueue(int elevatorId) {
        return requestQueuesMap.get(elevatorId);
    }

}