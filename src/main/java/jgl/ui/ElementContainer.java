package jgl.ui;

public class ElementContainer extends Element {

    private Element[] elements;
    private int size;

    public void addElement(Element element) {
        if (size == elements.length) {
            // If the array is full, create a new array with double the capacity
            Element[] copy = new Element[elements.length * 2];
            // Copy the elements from the old array to the new array
            System.arraycopy(elements, 0, copy, 0, elements.length);
            // Update the reference to the new array
            elements = copy;
        }

        int index = size++;

        element.setParent(this);
        element.setIndex(index);
        elements[index] = element;
    }

    public void removeElement(Element element) {
        int index = element.getIndex();

        // Validate
        if (index < 0 || index >= size) {
            return; // or throw exception
        }

        // Remove parent/index link
        element.setParent(null);
        element.setIndex(-1);

        int lastIndex = size - 1;

        // If we are not removing the last element, swap the last element into its place
        if (index != lastIndex) {
            Element last = elements[lastIndex];
            elements[index] = last;
            last.setIndex(index);
        }

        // Clear the last element slot
        elements[lastIndex] = null;

        size--;
    }

    public Element[] getElements() {
        return elements;
    }

    @Override
    public void update(double delta) {
        for (int i = 0; i < size; i++) {
            elements[i].update(delta);
        }
    }

    @Override
    public void render() {
        for (int i = 0; i < size; i++) {
            elements[i].render();
        }
    }
}
