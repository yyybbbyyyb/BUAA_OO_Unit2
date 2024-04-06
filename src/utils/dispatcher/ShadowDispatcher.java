package utils.dispatcher;

import config.Elevators;
import constants.Constants;
import entity.Elevator;
import entity.Passenger;
import entity.ShadowElevator;

import java.util.ArrayList;

public class ShadowDispatcher implements Dispatcher {

    private final ArrayList<ShadowElevator> shadowElevators = new ArrayList<>();

    @Override
    public int getElevatorId(Passenger passenger) {
        cleanShadowElevators();
        getInfoFromElevators();
        int elevatorId = -1;
        for (int i = 0; i < shadowElevators.size(); i++) {
            ShadowElevator shadowElevator = shadowElevators.get(i);
            shadowElevator.calculateCostTime(passenger);
        }
        for (int i = 0; i < shadowElevators.size(); i++) {
            if (elevatorId == -1 &&
                    shadowElevators.get(i).getCostTime() < Constants.MAX_COST_TIME) {
                elevatorId = shadowElevators.get(i).getElevatorId();
            } else {
                if (shadowElevators.get(i).getCostTime() < Constants.MAX_COST_TIME
                ) {
                    if (shadowElevators.get(i).getCostTime() <
                            shadowElevators.get(elevatorId).getCostTime()) {
                        elevatorId = shadowElevators.get(i).getElevatorId();
                    }
                }
            }
        }
        return elevatorId;
    }

    private void cleanShadowElevators() {
        shadowElevators.clear();
    }

    private void getInfoFromElevators() {
        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            Elevator elevator = Elevators.getElevator(i);
            shadowElevators.add(new ShadowElevator(elevator));
        }
    }
}
