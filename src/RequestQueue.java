import com.oocourse.elevator1.PersonRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class RequestQueue {
    private final int elevatorId;

    private final HashMap<Integer, HashSet<PersonRequest>> requestQueueByFromFloor;

    private final HashMap<Integer, HashSet<PersonRequest>> requestQueueByToFloor;

    private int currentRequestNum;

    private boolean isEnd;

    public RequestQueue(int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentRequestNum = 0;
        requestQueueByFromFloor = new HashMap<>();
        requestQueueByToFloor = new HashMap<>();
        this.isEnd = false;
        for (int i = 1; i <= Constants.MAX_FLOOR; i++) {
            requestQueueByFromFloor.put(i, new HashSet<>());
            requestQueueByToFloor.put(i, new HashSet<>());
        }
    }

    public synchronized void addRequest(PersonRequest request) {
        requestQueueByFromFloor.get(request.getFromFloor()).add(request);
        requestQueueByToFloor.get(request.getToFloor()).add(request);
        currentRequestNum++;
        notifyAll();
    }

    public synchronized PersonRequest gerOneRequestAndRemove(int floor, IndexType indexType) {
        if (isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (isEmpty()) {
            return null;
        }
        PersonRequest request = null;
        HashSet<PersonRequest> requestSet = null;
        if (indexType == IndexType.BY_FROM) {
            requestSet = requestQueueByFromFloor.get(floor);
        } else if (indexType == IndexType.BY_TO) {
            requestSet = requestQueueByToFloor.get(floor);
        }
        if (requestSet != null && !requestSet.isEmpty()) {
            Iterator<PersonRequest> iterator = requestSet.iterator();
            request = iterator.next();
        }
        if (request != null) {
            delRequest(request);
        }
        notifyAll();
        return request;
    }

    public synchronized void delRequest(PersonRequest request) {
        Iterator<PersonRequest> fromFloorIterator = requestQueueByFromFloor.get(request.getFromFloor()).iterator();
        while (fromFloorIterator.hasNext()) {
            PersonRequest req = fromFloorIterator.next();
            if (req.equals(request)) {
                fromFloorIterator.remove();
                break;
            }
        }

        Iterator<PersonRequest> toFloorIterator = requestQueueByToFloor.get(request.getToFloor()).iterator();
        while (toFloorIterator.hasNext()) {
            PersonRequest req = toFloorIterator.next();
            if (req.equals(request)) {
                toFloorIterator.remove();
                break;
            }
        }
        currentRequestNum--;
    }

    public synchronized void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public synchronized boolean isEnd() {
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        return currentRequestNum == 0;
    }

    public synchronized HashSet<PersonRequest> getFromFloor(int floor) {
        return requestQueueByFromFloor.get(floor);
    }

    public synchronized HashSet<PersonRequest> getToFloor(int floor) {
        return requestQueueByToFloor.get(floor);
    }

    public synchronized int getCurrentRequestNum() {
        return currentRequestNum;
    }
}
