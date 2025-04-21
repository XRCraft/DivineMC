package com.ishland.flowsched.structs;

import org.bxteam.divinemc.server.chunk.PriorityHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;

@SuppressWarnings("unchecked")
public class DynamicPriorityQueue<E> {
    private final AtomicIntegerArray taskCount;
    public final ConcurrentLinkedQueue<E>[] priorities;
    private final ConcurrentHashMap<E, Integer> priorityMap = new ConcurrentHashMap<>();

    public DynamicPriorityQueue() {
        this.taskCount = new AtomicIntegerArray(PriorityHandler.MAX_PRIORITY + 1);
        this.priorities = new ConcurrentLinkedQueue[PriorityHandler.MAX_PRIORITY + 1];
        for (int i = 0; i < (PriorityHandler.MAX_PRIORITY + 1); i++) {
            this.priorities[i] = new ConcurrentLinkedQueue<>();
        }
    }

    public void enqueue(E element, int priority) {
        if (this.priorityMap.putIfAbsent(element, priority) != null)
            throw new IllegalArgumentException("Element already in queue");

        this.priorities[priority].add(element);
        this.taskCount.incrementAndGet(priority);
    }

    public boolean changePriority(E element, int newPriority) {
        Integer currentPriority = this.priorityMap.get(element);
        if (currentPriority == null || currentPriority == newPriority) {
            return false;
        }

        int currentIndex = currentPriority;
        boolean removedFromQueue = this.priorities[currentIndex].remove(element);
        if (!removedFromQueue) {
            return false;
        }

        this.taskCount.decrementAndGet(currentIndex);
        final boolean changeSuccess = this.priorityMap.replace(element, currentPriority, newPriority);
        if (!changeSuccess) {
            return false;
        }

        this.priorities[newPriority].add(element);
        this.taskCount.incrementAndGet(newPriority);
        return true;
    }

    public E dequeue() {
        for (int i = 0; i < this.priorities.length; i++) {
            if (this.taskCount.get(i) == 0) continue;
            E element = priorities[i].poll();
            if (element != null) {
                this.taskCount.decrementAndGet(i);
                this.priorityMap.remove(element);
                return element;
            }
        }
        return null;
    }

    public boolean contains(E element) {
        return priorityMap.containsKey(element);
    }

    public void remove(E element) {
        Integer priority = this.priorityMap.remove(element);
        if (priority == null) return;

        boolean removed = this.priorities[priority].remove(element);
        if (removed) this.taskCount.decrementAndGet(priority);
    }

    public int size() {
        return priorityMap.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
