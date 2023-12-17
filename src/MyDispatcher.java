/* Implement this class. */

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyDispatcher extends Dispatcher {

    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
    }
    int round_robin_index = 0;

    @Override
    public synchronized void addTask(Task task) {
        switch(algorithm) {
            case ROUND_ROBIN -> addRoundRobinTask(task);
            case SHORTEST_QUEUE -> addShortestQueueTask(task);
            case SIZE_INTERVAL_TASK_ASSIGNMENT -> addSizeIntervalTask(task);
            case LEAST_WORK_LEFT -> addLeastWorkLeft(task);
        }
    }

    private void addRoundRobinTask(Task task) {
        // send the task to the host, based on the index of the last host a task was sent to
        hosts.get(++round_robin_index % hosts.size()).addTask(task);
    }

    private void addShortestQueueTask(Task task) {
        // send the task to the host with the shortest queue
        Host designated_host = Collections.min(hosts, Comparator.comparingInt(Host::getQueueSize));
        designated_host.addTask(task);
    }

    private void addSizeIntervalTask(Task task) {
        // send the task to the host based on the task type
        switch (task.getType()) {
            case SHORT -> hosts.get(0).addTask(task);
            case MEDIUM -> hosts.get(1).addTask(task);
            case LONG -> hosts.get(2).addTask(task);
        }
    }

    private void addLeastWorkLeft(Task task) {
        // send the task to the host based on the work left
        Host designated_host = Collections.min(hosts, Comparator.comparingLong(Host::getWorkLeft));
        designated_host.addTask(task);
    }
}
