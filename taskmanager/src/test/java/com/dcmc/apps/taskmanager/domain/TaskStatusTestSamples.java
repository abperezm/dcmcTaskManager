package com.dcmc.apps.taskmanager.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TaskStatusTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static TaskStatus getTaskStatusSample1() {
        return new TaskStatus().id(1L).name("name1");
    }

    public static TaskStatus getTaskStatusSample2() {
        return new TaskStatus().id(2L).name("name2");
    }

    public static TaskStatus getTaskStatusRandomSampleGenerator() {
        return new TaskStatus().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}
