import com.oocourse.elevator1.TimableOutput;
import constants.Constants;
import entity.Elevator;
import utils.InputHandler;

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
