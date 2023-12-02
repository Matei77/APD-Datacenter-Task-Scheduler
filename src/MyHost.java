/* Implement this class. */

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class MyHost extends Host {
    private final PriorityBlockingQueue<Task> queued_tasks = new PriorityBlockingQueue<>(10,
            Comparator.comparingInt(Task::getPriority).reversed().thenComparingInt(Task::getStart));
    private static final Task POISON_VALUE = new Task(-1, -1, -1, TaskType.SHORT, -1, false);
    private Task running_task;
    private final AtomicLong work_left = new AtomicLong();
    private boolean exit = false;

    private void executeTask() {
        long start_sleep_time = 0;
        long time_left = running_task.getLeft();

        try {
            // sleep for one second at a time in order to get a more accurate work_left value
            while (time_left > 0) {
                // sleep for one second if time_left if greater than one second or for time_left milliseconds otherwise
                long sleeping_time = (time_left < 1000) ? time_left : 1000;

                // get the current time in case there will be an interrupt and sleep will not finish
                start_sleep_time = System.currentTimeMillis();
                sleep(sleeping_time);

                // update the time_left on the current task and the work_left on all tasks
                time_left -= sleeping_time;
                work_left.addAndGet(-sleeping_time);
            }

            // finish the task
            running_task.finish();
            running_task = null;

        } catch (InterruptedException e) {
            // task was interrupted

            // sleeping time is the time that the task was running for before being interrupted
            long end_sleep_time = System.currentTimeMillis();
            long sleeping_time = end_sleep_time - start_sleep_time;

            // update the work_left and the time left on the running task
            work_left.addAndGet(-sleeping_time);
            running_task.setLeft(time_left - sleeping_time);

            // add the task back to the queue
            queued_tasks.add(running_task);
            running_task = null;
        }
    }

    @Override
    public void run() {
        while (!exit) {
            try {
                // get the next task from the priority queue. If there is no task in the queue, it will block and wait
                // for one
                running_task = queued_tasks.take();

                // if the poison value is received, end the execution of the thread
                if (running_task == POISON_VALUE) {
                    exit = true;

                // otherwise, execute the task
                } else {
                    executeTask();
                }

            } catch (InterruptedException e) {
                queued_tasks.add(POISON_VALUE);
            }
        }
    }

    @Override
    public void addTask(Task task) {
        // add the task to the bocking priority queue
        queued_tasks.add(task);

        // update work left atomically
        work_left.addAndGet(task.getDuration());

        // if the running task is preemptible and a task with a higher priority was added to the queue, interrupt the
        // running task
        if (running_task != null && running_task.isPreemptible() && running_task.getPriority() < task.getPriority()) {
            interrupt();
        }
    }

    @Override
    public int getQueueSize() {
        // return the size of the queue, including the running task if there is one
        return queued_tasks.size() + ((running_task != null) ? 1 : 0);
    }

    @Override
    public long getWorkLeft() {
        return work_left.longValue();
    }

    @Override
    public void shutdown() {
        // add a value to the queue that will end the execution of the thread
        queued_tasks.add(POISON_VALUE);
    }
}
