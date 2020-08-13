package de.toem.impulse.extension.yakindu.producer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.yakindu.sct.simulation.core.engine.scheduling.ITimeTaskScheduler;
import org.yakindu.sct.simulation.core.engine.scheduling.ITimeTaskScheduler.TimeTask.Priority;

public class Scheduler implements ITimeTaskScheduler {

    private boolean eventDriven = false;
    private long current;
    private long scheduleCount;
    private Map<String, TimeTask> tasks = new HashMap<>();
    
    private static Field priorityField, nameField, periodField, scheduleOrderField;
    static {
        try {
            priorityField = TimeTask.class.getDeclaredField("priority");
            nameField = TimeTask.class.getDeclaredField("name");
            periodField = TimeTask.class.getDeclaredField("period");
            scheduleOrderField = TimeTask.class.getDeclaredField("scheduleOrder");
            priorityField.setAccessible(true);
            nameField.setAccessible(true);
            periodField.setAccessible(true);
            nameField.setAccessible(true);
        } catch (NoSuchFieldException e) {
        }
    }
    public Scheduler(boolean eventDriven,long start) {
        this.current = start;
        this.eventDriven = eventDriven;
    }

    private TimeTask.Priority getPriority(TimeTask task) {
        try {
            return (Priority) priorityField.get(task);
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
        return Priority.NORMAL;
    }

    private String getName(TimeTask task) {
        try {
            return (String) nameField.get(task);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }

    private long getPeriod(TimeTask task) {
        try {
            return periodField.getLong(task);
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
        return -1;
    }

    private void setPeriod(TimeTask task, long period) {
        try {
            periodField.setLong(task, period);
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
    }

    private void setScheduleOrder(TimeTask task, long scheduleOrder) {
        try {
            scheduleOrderField.setLong(task, scheduleOrder);
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
    }

    private long getNextExecutionTime(TimeTask task) {
        return task.getNextExecutionTime();
    }

    private void setNextExecutionTime(TimeTask task, long executionTime) {
        task.setNextExecutionTime(executionTime);
    }

    private boolean isCanceled(TimeTask task) {
        return task.isCanceled();
    }

    private void cancel(TimeTask task) {
        task.cancel();
    }

    @Override
    public synchronized void scheduleTimeTask(TimeTask task, boolean periodical, long duration) {
//        Utils.log("scheduleTimeTask",getName(task),periodical,duration);
        setNextExecutionTime(task, current + (duration >= 0 ? duration : 1));
        setScheduleOrder(task, scheduleCount++);
        setPeriod(task, periodical ? duration >= 0 ? duration : 1 : -1);
        tasks.put(getName(task), task);
    }

    @Override
    public synchronized void start() {
//        Utils.log("start");

    }

    @Override
    public synchronized void step() {
//        Utils.log("step");

    }

    @Override
    public synchronized void suspend() {
//        Utils.log("suspend");
    }

    @Override
    public synchronized void resume() {
//        Utils.log("resume");
    }

    @Override
    public synchronized void terminate() {
//        Utils.log("terminate");
    }

    @Override
    public void timeLeap(long duration) {
//        Utils.log("timeLeap");
        //for (TimeTask t : tasks.values())
        //    Utils.log("",getName(t),getNextExecutionTime(t));
        boolean foundEvent = false;
        long untilTime = current + (duration >= 0 ? duration : -duration);
        boolean untilTimeAdjusted = false;
        
        try {

            // handle events
            while (true) {
                TimeTask task = getNextScheduledTask();
                if (task == null)
                    return;
                if (isCanceled(task))
                    synchronized (this) {
                        tasks.remove(getName(task));
                    }
                else {
                    long excecutionTime = getNextExecutionTime(task);

                    // if eventDriven take first event as until time
                    if (eventDriven && !untilTimeAdjusted && excecutionTime >= current && excecutionTime < untilTime) {
                        untilTime = excecutionTime;
                        untilTimeAdjusted = true;
                    }

                    if (excecutionTime > untilTime)
                        return;
                    current = excecutionTime;
                    foundEvent = true;
                    task.run();
                    if (getPeriod(task) > 0 && !isCanceled(task))
                        setNextExecutionTime(task, current + getPeriod(task));
                    else
                        synchronized (this) {
                            tasks.remove(getName(task));
                        }
                }
            }
        } finally {
            if (!foundEvent)
                current = untilTime;
        }
    }

    @Override
    public synchronized void unscheduleTimeTask(String name) {
//        Utils.log("unscheduleTimeTask");
        tasks.remove(name);
    }

    @Override
    public synchronized long getCurrentTime() {
        return current;
    }

    private synchronized TimeTask getNextScheduledTask() {
        long next = Long.MAX_VALUE;
        TimeTask task = null;
        for (TimeTask t : tasks.values())
            if (getNextExecutionTime(t) < next && getNextExecutionTime(t) >= current) {
                next = getNextExecutionTime(t);
                task = t;
            }
        return task;
    }
} 