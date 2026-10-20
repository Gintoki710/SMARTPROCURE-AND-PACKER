package dsa.stack;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

/**
 * Custom Generic Stack implementation (Module 1 / Experiment 1).
 * Supports standard LIFO operations: push, pop, peek, isEmpty, size, and element inspection.
 */
public class CustomStack<T> {
    private Object[] elements;
    private int top;
    private static final int DEFAULT_CAPACITY = 32;

    public CustomStack() {
        this(DEFAULT_CAPACITY);
    }

    public CustomStack(int initialCapacity) {
        this.elements = new Object[initialCapacity];
        this.top = -1;
    }

    public void push(T item) {
        if (top == elements.length - 1) {
            resize(elements.length * 2);
        }
        elements[++top] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T item = (T) elements[top];
        elements[top] = null; // Dereference for garbage collection
        top--;
        return item;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return (T) elements[top];
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public int size() {
        return top + 1;
    }

    public void clear() {
        for (int i = 0; i <= top; i++) {
            elements[i] = null;
        }
        top = -1;
    }

    /**
     * Returns a snapshot of stack elements from Top to Bottom (LIFO).
     */
    @SuppressWarnings("unchecked")
    public List<T> toListTopToBottom() {
        List<T> list = new ArrayList<>();
        for (int i = top; i >= 0; i--) {
            list.add((T) elements[i]);
        }
        return list;
    }

    private void resize(int newCapacity) {
        Object[] newElements = new Object[newCapacity];
        System.arraycopy(elements, 0, newElements, 0, top + 1);
        elements = newElements;
    }
}

