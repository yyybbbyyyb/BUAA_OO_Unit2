package utils.dispatcher;

import config.Elevators;
import constants.Constants;
import entity.Elevator;
import entity.InfoElevator;
import entity.Passenger;

import java.util.ArrayList;

public class EstimateDispatcher implements Dispatcher {

    @Override
    public int getElevatorId(Passenger passenger) {
        int elevatorId = -1;
        int maxEstimate = -1;
        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            InfoElevator infoElevator = Elevators.getElevator(i).getInfoElevator();
            int estimate =  infoElevator.getCost(passenger);
            int finalEstimate = infoElevator.getCost(passenger);
            while (finalEstimate != estimate) {
                estimate = finalEstimate;
                finalEstimate = infoElevator.getCost(passenger);
            }
            if (maxEstimate == -1) {
                maxEstimate = finalEstimate;
                elevatorId = infoElevator.getElevatorId();
            } else {
                if (finalEstimate > maxEstimate) {
                    maxEstimate = finalEstimate;
                    elevatorId = infoElevator.getElevatorId();
                }
            }
        }
        return elevatorId;
    }
}
