package utils.dispatcher;

import config.Elevators;
import constants.Constants;
import entity.Elevator;
import entity.InfoElevator;
import entity.Passenger;

import java.util.ArrayList;

public class EstimateDispatcher implements Dispatcher {

    private final ArrayList<InfoElevator> infoElevators = new ArrayList<>();

    @Override
    public int getElevatorId(Passenger passenger) {
        cleanInfoElevators();
        getInfoFromElevators();
        int elevatorId = -1;
        int maxEstimate = -1;
        for (int i = 0; i < infoElevators.size(); i++) {
            InfoElevator infoElevator = infoElevators.get(i);
            int estimate =  infoElevator.getCost(passenger);
            if (estimate < 0) {
                continue;
            } else {
                if (maxEstimate == -1) {
                    maxEstimate = estimate;
                    elevatorId = infoElevator.getElevatorId();
                } else {
                    if (estimate > maxEstimate) {
                        maxEstimate = estimate;
                        elevatorId = infoElevator.getElevatorId();
                    }
                }
            }
        }
        return elevatorId;
    }

    private void cleanInfoElevators() {
        infoElevators.clear();
    }

    private void getInfoFromElevators() {
        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            Elevator elevator = Elevators.getElevator(i);
            infoElevators.add(new InfoElevator(elevator));
        }
    }
}
