package org.lei.opi.core;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Create a concurrent safe circular buffer of fixed-size objects.
 * 
 * Andrew Turpin
 * Date Mon 17 Jun 2024 09:49:45 AWST
 */
public class CircularBuffer<T> {
    /** default capacity for {@link buffer}} */
    static final private int DEFAULT_CAPACITY = 16;
    /** The buffer */
    private Object []buffer;
    /** Max number of elements in buffer */
    private int capacity;
    /** Next available element of buffer */
    private int index = 0;

    /** Used to lock {@link buffer} for update and reading */
    public final ReentrantLock lock = new ReentrantLock();

    /**
     * @param supplier Supplier of objects to put in buffer (eg String::new or a -> new MyClass(a))
     * @param capacity Number of elements to allow. If < 0 use {@link DEFAULT_CAPACITY}
     */
    public CircularBuffer(Supplier<T> supplier, int capacity) {
        if (capacity < 1)
            capacity = DEFAULT_CAPACITY;

        buffer = new Object[capacity];
        index = 0;

        for (int i = 0 ; i < capacity ; i++)
            buffer[i] = supplier.get();
    }

    /**
     * Remove the first element of the buffer and return it.
     * (Does not free the memory - leaves object there for reuse.)
     * @return First element of buffer.
     */
    public T pop() {
        Object o = null;
        lock.lock();
        try {
            o = buffer[(index - 1) % this.capacity];
            index--;
            if (index == -1)
                index = this.capacity;
        } finally {
            lock.unlock();
        }
        return (T)o;
    }

    /**
     * Apply {@link mutator} to the free end of the buffer (element {@link index}).
     * @param mutator Function to mutate element the buffer (eg a -> b.copyTo(a))
     */
    public void put(Consumer<T> mutator) {
        lock.lock();
        try {
            mutator.accept((T)buffer[index]);
            index = (index + 1) % capacity;
        } finally {
            lock.unlock();
        }
        return;
    }

    /**
     * Apply {@link f} to the end of the buffer (element {@link index - 1}).
     * @param f Function to take an element and do something 
     */
    public void apply(Consumer<T> f) {
        lock.lock();
        try {
            f.accept((T)buffer[(index - 1) % capacity]);
        } finally {
            lock.unlock();
        }
        return;
    }

    /**
     * @param filter Predicate to apply to each element of {@link buffer}
     * @param copy Function to copy element i of the buffer to dst (eg copy = (src, dst) -> src.copyTo(dst); )
     * @param dst Destination object for the copy  TODO do i need this??? or can it be in copy function?
     * @return Copy the first element for which {@link filter} is true into dst and return true. False for no match.
     */
    public boolean get(Predicate<T> filter, BiConsumer<T, T> copy, T dst) {
        boolean found = false;
        lock.lock();
        try {
            for(int i = (index + 1) % capacity ; i != index ; i = (i + 1) % capacity)
                if (filter.test((T)buffer[i])) {
                    copy.accept((T)buffer[i], dst);
                    found = true;
                    break;
                }
        } finally {
            lock.unlock();
        }
        return found;
    }

    /**
     * Apply {@link mutator} to the first element of {@link buffer} for which {@link filter} returns true.
     * If no elements make {@link filter} true, throw NoSuchElementException.
     * @param filter Function that takes a T and returns true of false.
     * @param mutator Function to mutate element i of the buffer
     */
    public void findAndApply(Function<T, Boolean> filter, Function<T, T> mutator) throws NoSuchElementException {
        lock.lock();
        try {
            for(int i = (index + 1) % capacity ; i != index ; i = (i + 1) % capacity)
                if (filter.apply((T)buffer[i])) {
                    buffer[i] = mutator.apply((T)buffer[i]);
                    break;
                }
        } finally {
            lock.unlock();
        }
        return;
    }
}
