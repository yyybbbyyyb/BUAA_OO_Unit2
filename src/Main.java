import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        TimableOutput.initStartTimestamp();

        InputHandler.getInstance();

        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            Elevator elevator = new Elevator(i);
            elevator.start();
        }

        InputHandler.getInstance().start();
    }
}
