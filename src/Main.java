import config.Elevators;
import com.oocourse.elevator2.TimableOutput;
import constants.Constants;
import entity.Elevator;
import utils.Dispatch;
import utils.InputHandle;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        // 单例模式初始化
        InputHandle.getInstance();

        // 初始化电梯
        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            Elevator elevator = new Elevator(i);
            Elevators.addElevator(elevator);
        }

        // 启动电梯
        for (int i = 1; i <= Constants.ELEVATOR_NUM; i++) {
            Elevators.getElevator(i).start();
        }

        // 启动调度
        Dispatch dispatch = new Dispatch();
        dispatch.start();

        // 启动输入处理
        InputHandle.getInstance().start();
    }
}
