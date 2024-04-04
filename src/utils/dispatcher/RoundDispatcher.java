package utils.dispatcher;

import Config.Elevators;
import entity.Elevator;
import entity.Passenger;

import java.util.ArrayList;
import java.util.Random;

public class RoundDispatcher implements Dispatcher {
    private int elevatorId = 1;

    @Override
    public int getElevatorId(Passenger passenger) {
        ArrayList<Elevator> nonResetElevators = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Elevator elevator = Elevators.getElevator(i);
            if (!elevator.isReset()) {
                nonResetElevators.add(elevator);
            }
        }
        if (!nonResetElevators.isEmpty()) {
            // 随机选择一个不处于重置状态的电梯
            Random random = new Random();
            int randomIndex = random.nextInt(nonResetElevators.size());
            return nonResetElevators.get(randomIndex).getElevatorId();
        } else {
            System.out.println("!");
            return 1;
        }
    }
}
