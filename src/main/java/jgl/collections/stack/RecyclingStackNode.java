package jgl.collections.stack;

/**
 * Represents a doubly linked node used in the RecyclingStack implementation.
 * Each node stores a piece of data, a reference to the next node, and a reference to the previous node.
 *
 * @param <T> The type of data stored in the node.
 * @author Albert Beaupre
 * @version 1.0
 * @since May 1st, 2024
 */
public class RecyclingStackNode<T> {

    /**
     * The next node in the linked list.
     */
    protected RecyclingStackNode<T> next;

    /**
     * The previous node in the linked list.
     */
    protected RecyclingStackNode<T> previous;

    /**
     * The data stored in the node.
     */
    protected T data;

    /**
     * Constructs a RecyclingStackNode with the specified data, next node, and previous node.
     *
     * @param data     The data to be stored in the node.
     * @param next     The next node in the linked list.
     * @param previous The previous node in the linked list.
     */
    public RecyclingStackNode(T data, RecyclingStackNode<T> next, RecyclingStackNode<T> previous) {
        this.data = data;
        this.next = next;
        this.previous = previous;
    }

    /**
     * Gets the next node in the linked list.
     *
     * @return The next node.
     */
    public RecyclingStackNode<T> getNext() {
        return next;
    }

    /**
     * Sets the next node in the linked list.
     *
     * @param next The node to be set as the next node.
     */
    public void setNext(RecyclingStackNode<T> next) {
        this.next = next;
    }

    /**
     * Gets the previous node in the linked list.
     *
     * @return The previous node.
     */
    public RecyclingStackNode<T> getPrevious() {
        return previous;
    }

    /**
     * Sets the previous node in the linked list.
     *
     * @param previous The node to be set as the previous node.
     */
    public void setPrevious(RecyclingStackNode<T> previous) {
        this.previous = previous;
    }

    /**
     * Gets the data stored in the node.
     *
     * @return The data.
     */
    public T getData() {
        return data;
    }

    /**
     * Sets the data to be stored in the node.
     *
     * @param data The data to be set.
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Returns a string representation of the node, including its data, previous node's data, and next node's data.
     *
     * @return A string representation of the node.
     */
    @Override
    public String toString() {
        return "RecyclingStackNode[" +
                "data=" + data + ", " +
                "previous=" + (previous == null ? null : previous.data) + ", " +
                "next=" + (next == null ? null : next.data) +
                "]";
    }
}