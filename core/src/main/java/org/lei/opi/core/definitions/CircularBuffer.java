package org.lei.opi.core.definitions;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Create a concurrent safe circular LIFO buffer of fixed-size objects.
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
    /** Current "rightmost" element of buffer; most recently added. */
    private int head;
    /** Current "leftmost" element of buffer; oldest in the array. */
    private int tail;

    /** Number of elements in array */
    private int n;

    /** Used to lock {@link buffer} for access */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * @param supplier Supplier of objects to put in buffer (eg String::new or a -> new MyClass(a))
     * @param capacity Number of elements to allow. If < 0 use {@link DEFAULT_CAPACITY}
     */
    public CircularBuffer(Supplier<T> supplier, int capacity) {
        if (capacity < 1)
            capacity = DEFAULT_CAPACITY;
        this.capacity = capacity;

        buffer = new Object[capacity];
        head = -1;
        n = 0;

        for (int i = 0 ; i < capacity ; i++)
            buffer[i] = supplier.get();
    }

    /** @return true if buffer is empty, false otherwise */
    public boolean empty() { 
        lock.lock();
        try {
            return n == 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove the last element in the buffer and return it.
     * (Does not free the memory - leaves object there for reuse.)
     * @return First element of buffer.
      
      Worried that this returns o and not o.clone()

    public T pop() {
        if (empty()) return null;

        Object o = null;
        lock.lock();
        try {
            o = buffer[tail];
            tail = (tail + 1) % capacity;
            n--;
        } finally {
            lock.unlock();
        }
        return (T)o;
    }
     */

    /**
     * Apply {@link mutator} to the free end of the buffer (element "head + 1").
     * @param mutator Function to mutate a buffer element (eg (T a) -> b.copyTo(a))
     */
    public void put(Consumer<T> mutator) {
        lock.lock();
        int newHead = (head + 1) % capacity;
        try {
            mutator.accept((T)buffer[newHead]);
            if (n == 0)  // empty
                tail = newHead;
            else if (newHead == tail) { // full!
                tail = (tail + 1) % capacity;
                n--;
            }
            head = newHead;
            n++;
        } finally {
            lock.unlock();
        }
        return;
    }

    /**
     * If predicate evaluates to true on head, cut it off!
     * @param decider Predicate to apply to head of {@link buffer}
     */
    public void conditionalPop(Predicate<T> f) {
        if (empty()) return;

        lock.lock();
        try {
            if (f.test((T)buffer[head])) {
                if (n == 1) {
                    head = -1;
                    n = 0;
                } else {
                    head = (head - 1) % capacity;
                    n--;
                }
            }
        } finally {
            lock.unlock();
        }
        return;
    }

    /**
     * Apply {@link f} to the tail (oldest added) of the buffer.
     * @param f Function to take an element and do something 
     */
    public void applyTail(Consumer<T> f) {
        if (empty()) return;

        lock.lock();
        try {
            f.accept((T)buffer[tail]);
        } finally {
            lock.unlock();
        }
        return;
    }

    /**
     * Apply {@link f} to the head (most recently added) of the buffer.
     * @param f Function to take an element and do something 
     */
    public void applyHead(Consumer<T> f) {
        if (empty()) return;
        lock.lock();
        try {
            f.accept((T)buffer[head]);
        } catch (Exception e) {
            System.out.println("\t\t\tCircuarBuffer::applyHead Fails.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return;
    }

    /**
     * Elements are linear searched from tail (oldest) to head (newest).
     * 
     * @param filter Predicate to apply to each element of {@link buffer}
     * @param copy Function to copy element i of the buffer to dst (eg copy = (src, dst) -> src.copyTo(dst); )
     * @param dst Destination object for the copy  TODO do i need this??? or can it be in copy function?
     * @return Copy the first element for which {@link filter} is true into dst and return true. False for no match.
     */
    public boolean getTailToHead(Predicate<T> filter, BiConsumer<T, T> copy, T dst) {
        if (empty()) return false;
        lock.lock();
        try {
            for(int i = 0 ; i < n ; i++) {
                int j = (tail + i ) % capacity;
                if (filter.test((T)buffer[j])) {
                    copy.accept((T)buffer[j], dst);
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Elements are linear searched from head (newest) to tail (oldest).
     * 
     * @param filter Predicate to apply to each element of {@link buffer}
     * @param copy Function to copy element i of the buffer to dst (eg copy = (src, dst) -> src.copyTo(dst); )
     * @param dst Destination object for the copy  TODO do i need this??? or can it be in copy function?
     * @return Copy the first element for which {@link filter} is true into dst and return true. False for no match.
     */
    public boolean getHeadToTail(Predicate<T> filter, BiConsumer<T, T> copy, T dst) {
        if (empty()) return false;
        lock.lock();
        try {
            for(int i = 0 ; i < n ; i++) {
                int j = (head + i ) % capacity;
                if (filter.test((T)buffer[j])) {
                    copy.accept((T)buffer[j], dst);
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Elements are linear searched from tail (oldest) to head (newest).
     * 
     * Apply {@link mutator} to the first element of {@link buffer} for which {@link filter} returns true.
     * If no elements make {@link filter} true, throw NoSuchElementException.
     * @param filter Function that takes a T and returns true of false.
     * @param mutator Function to mutate element i of the buffer
     */
    public void findAndApplyTailToHead(Predicate<T> filter, Function<T, T> mutator) throws NoSuchElementException {
        if (empty()) return;
        lock.lock();
        try {
            for(int i = 0 ; i < n ; i++) {
                int j = (tail + i) % capacity;
                if (filter.test((T)buffer[j])) {
                    buffer[i] = mutator.apply((T)buffer[j]);
                    return;
                }
            }
        } finally {
            lock.unlock();
        }
        return;
    }

    /**
     * Elements are linear searched from head (newest)to tail (oldest) .
     * 
     * Apply {@link mutator} to the first element of {@link buffer} for which {@link filter} returns true.
     * If no elements make {@link filter} true, throw NoSuchElementException.
     * @param filter Function that takes a T and returns true of false.
     * @param mutator Function to mutate element i of the buffer
     */
    public void findAndApplyHeadToTail(Predicate<T> filter, Function<T, T> mutator) throws NoSuchElementException {
        if (empty()) return;
        lock.lock();
        try {
            for(int i = 0 ; i < n ; i++) {
                int j = (head + i) % capacity;
                if (filter.test((T)buffer[j])) {
                    buffer[i] = mutator.apply((T)buffer[j]);
                    return;
                }
            }
        } finally {
            lock.unlock();
        }
        return;
    }

    /**
     * 
     */
    public String toString() {
        lock.lock();
        try {
            StringBuilder b = new StringBuilder(String.format("CircularBuffer (n = %d/%d, tail = %s, head = %s):", n, capacity, tail, head));
            
            if (empty())
                return b.toString();

            for(int i = 0 ; i < n ; i++) {
                int j = (tail + i) % capacity;

                b.append("\n\t\t" + ((T)buffer[j]).toString());
            }

            return b.toString();
        } finally {
            lock.unlock();
        }
    }
}