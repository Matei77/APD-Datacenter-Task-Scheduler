**Name: Ionescu Matei-Ștefan**  
**Group: 333CAb**

# APD Homework #2 - Datacenter Task Scheduler

This program simulates a datacenter that receives multiple tasks and allocates each of them, based on a scheduling
algorithm, to a host represented as a thread.

## MyDispatcher
The dispatcher is responsible for allocating tasks to hosts. It implements 4 scheduling algorithms:

#### Round Robin
- This algorithm will send each host a task, in order, starting from the first host to the last and then repeating. 

#### Shortest Queue
- This algorithm will send each received task to the host with the shortest queue of tasks. 

#### Size Interval Task Assignment  
- This algorithm will use three hosts, and it will assign tasks to them based on the type of the task. Short tasks will
go the first host, medium to the second and long to the third.

#### Least Work Left
- This algorithm will send each received task to the host with the least work left.

## MyHost
The host is represented as a class that extends *Thread*. Some of the more important fields are:
- **queued_tasks** - This is a `PriorityBlockingQueue<Task>` that keeps the tasks sent by the dispatcher before they are
executed and compares elements based on their priority and, in case of ties, on start time. (The second comparison is 
needed because the PriorityBlockingQueue does not guarantee the ordering of elements with equal priority).

- **running_task** - This is an `AtomicReference<Task>` that keeps a reference to the currently running task. It is used
in order to avoid possible race conditions when getting the value of the running task in one thread and setting it in
another.

- **work_left** - this is an `AtomicLong` that keeps track of the work left on the host. It is used to avoid race
conditions, when updating the value of the *work_left*.

When the dispatcher adds a task to a host it calls the `addTask()` method. This will update the queue and the
*work_left* value. If the running task is preemptible and a task with a higher priority was added to the queue, the
running task will be interrupted.

The host thread is running until `shutdown()` is called. At that point, in the queue will be added a *poison* value,
which has the objective of notifying the thread that there are no other tasks coming and that the thread can stop
waiting for tasks. All tasks that are already in the queue will be finished, before the host ends its execution.

To simulate working on a task, the `executeTask()` function will be called, each time a task is taken from the queue.
This function will *sleep* for half a second at a time in order to get a more accurate *work_left* value, which is
updated after every *sleep*. When this function is interrupted by a new task with a higher priority, the running task
will update its time left for execution, and it will be added back to the queue.


## Notes
- The program passed all the tests on the checker, getting a score of 120/120p