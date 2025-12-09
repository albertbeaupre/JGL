package jgl.collections.stack;

import java.util.Iterator;

/**
 * A specialized stack implementation that supports recycling of nodes,
 * improving memory efficiency by reusing allocated nodes instead of
 * creating new ones. This recycling is especially useful in scenarios
 * where there is a frequent push-pop cycle.
 *
 * @param <T> The type of data stored in the stack.
 * @author Albert Beaupre
 * @version 1.0
 * @since May 1st, 2024
 */
public class RecyclingStack<T> implements Iterable<T> {
    private RecyclingStackNode<T> pool; // The pool of recycled nodes
    private RecyclingStackNode<T> stack; // The top of the stack
    private int size; // The current size of the stack

    /**
     * Constructs an empty RecyclingStack without allocating the pool.
     */
    public RecyclingStack() {
        // Allow for constructing without allocating the pool
    }

    /**
     * Constructs a RecyclingStack with an initial pool size.
     *
     * @param size The initial size of the node pool.
     */
    public RecyclingStack(int size) {
        allocate(size);
    }

    /**
     * Allocates a specified number of nodes and adds them to the pool.
     *
     * @param size The number of nodes to allocate.
     */
    public void allocate(int size) {
        for (int i = 0; i < size; i++)
            pool = new RecyclingStackNode<>(null, pool, null);
    }

    /**
     * Pushes a new node with the provided data onto the stack.
     *
     * @param data The data to be pushed onto the stack.
     * @return The node at the top of the stack after the push operation.
     */
    public RecyclingStackNode<T> push(T data) {
        RecyclingStackNode<T> newNode = null;
        if (pool == null) {
            newNode = new RecyclingStackNode<T>(data, stack, null);
        } else {
            RecyclingStackNode<T> temp = pool;
            pool = pool.next;

            newNode = temp;
            newNode.data = data;
            newNode.next = stack; // Clear the next pointer before returning the node
        }

        if (stack != null)
            stack.previous = newNode; // Update the previous pointer for the current top of the stack
        stack = newNode;
        size++;
        return stack;
    }

    /**
     * Pops the top node from the stack and returns its data.
     *
     * @return The data of the popped node, or null if the stack is empty.
     */
    public T pop() {
        if (stack == null)
            return null;
        RecyclingStackNode<T> temp = stack;
        T data = stack.data;
        stack = stack.next;

        temp.next = pool;
        pool = temp;

        if (stack != null)
            stack.previous = null; // Clear the previous pointer for the new top of the stack
        size--;
        return data;
    }

    /**
     * Peeks at the data of the top node without removing it.
     *
     * @return The data of the top node, or null if the stack is empty.
     */
    public T peek() {
        return stack == null ? null : stack.data;
    }

    /**
     * Removes the specific node from the stack.
     *
     * @param node The node to be removed.
     */
    public void remove(RecyclingStackNode<T> node) {
        if (isEmpty())
            return;

        if (node.previous != null) {
            node.previous.next = node.next;
        } else if (node == stack) {
            stack = stack.next;
            if (stack != null)
                stack.previous = null;
        }

        if (node.next != null)
            node.next.previous = node.previous;

        node.previous = null;
        node.next = pool;
        pool = node;
        size--;
    }

    /**
     * Checks if the stack contains the specified data.
     *
     * @param data The data to check for in the stack.
     * @return True if the stack contains the specified data, false otherwise.
     */
    public boolean contains(T data) {
        for (RecyclingStackNode<T> temp = stack; temp != null; temp = temp.next) {
            if (temp.data.equals(data)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Removes the first occurrence of the specified data from the stack.
     *
     * @param data The data to be removed.
     */
    public void removeOnce(T data) {
        for (RecyclingStackNode<T> temp = stack; temp != null; temp = temp.next) {
            if (temp.data.equals(data)) {
                remove(temp);
                break;
            }
        }
    }

    /**
     * Removes all occurrences of the specified data from the stack.
     *
     * @param data The data to be removed.
     */
    public void removeAll(T data) {
        for (RecyclingStackNode<T> temp = stack; temp != null; temp = temp.next) {
            if (temp.data.equals(data)) {
                remove(temp);
            }
        }
    }

    /**
     * Checks if the stack is empty.
     *
     * @return True if the stack is empty, false otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clears the entire stack and resets the pool.
     */
    public void clear() {
        stack = null;
        pool = null;
        size = 0;
    }

    /**
     * Retrieves the current size of the stack.
     *
     * @return The current size of the stack.
     */
    public int size() {
        return size;
    }

    /**
     * Provides an iterator to iterate through the elements of the stack.
     *
     * @return An iterator over the elements of the stack.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private RecyclingStackNode<T> current = stack;
            private RecyclingStackNode<T> lastReturned = null;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext())
                    throw new IllegalStateException("No more elements in the stack.");

                T data = current.data;
                lastReturned = current;
                current = current.next;
                return data;
            }

            @Override
            public void remove() {
                if (lastReturned == null)
                    throw new IllegalStateException("remove() can only be called after a call to next().");

                RecyclingStack.this.remove(lastReturned);
                lastReturned = null;
            }
        };
    }

    /**
     * Returns a string representation of the stack.
     *
     * @return A string representation of the stack.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        RecyclingStackNode<T> temp = stack;
        builder.append("RecyclingStack[");
        while (temp != null) {
            builder.append(temp.data);
            temp = temp.next;
            if (temp != null)
                builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }
}