package jgl.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ElementContainer extends Element {

    private Element[] elements;
    private int size;

    public void addElement(Element element) {
        if (size == elements.length) {
            Element[] copy = new Element[elements.length * 2];
            System.arraycopy(elements, 0, copy, 0, elements.length);
            elements = copy;
        }

        int index = size++;

        element.setParent(this);
        element.setIndex(index);
        elements[index] = element;
    }

    public void removeElement(Element element) {
        int index = element.getIndex();

        if (index < 0 || index >= size)
            return;

        element.setParent(null);
        element.setIndex(-1);

        int lastIndex = size - 1;

        if (index != lastIndex) {
            Element last = elements[lastIndex];
            elements[index] = last;
            last.setIndex(index);
        }

        elements[lastIndex] = null;
        size--;
    }

    public void insertElement(Element element, int index) {
        if (index >= elements.length - 1 || index < 0) {
            addElement(element);
            return;
        }

        element.setParent(this);
        element.setIndex(index);

        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
    }

    public Collection<Element> getElements() {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    @Override
    public void update(float delta) {
        for (int i = 0; i < size; i++) {
            elements[i].update(delta);
        }
    }

    @Override
    public void draw() {
        for (int i = 0; i < size; i++) {
            elements[i].draw();
        }
    }
}
