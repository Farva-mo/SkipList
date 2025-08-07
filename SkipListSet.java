//Mohammad Anisi

//final version, passing all test cases

import java.util.*;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {

    // Inner class for SkipList nodes
    public class SkipListSetItem {
        private SkipListSetItem next;
        private SkipListSetItem up;
        private SkipListSetItem below;
        private SkipListSetItem previous;
        private T value;
        private Integer level;

        public SkipListSetItem() {
            this.next = null;
            this.below = null;
            this.up = null;
            this.previous = null;
            this.value = null;
            this.level = null;
        }

        public SkipListSetItem(T item) {
            this.next = null;
            this.below = null;
            this.up = null;
            this.previous = null;
            this.value = item;
            this.level = 1;
        }

        /* compares this node's value with another item to determine order
         returns, -1 if this comes first, 0 if equal, +1 if this comes after
         */
        public int compareTo(T item) {
            if (this.value == null) return -1;
            return this.value.compareTo(item);
        }

        // Getters
        public SkipListSetItem getNext() {
            return next;
        }
        public SkipListSetItem getBelow() {
            return below;
        }
        public SkipListSetItem getUp() {
            return up;
        }
        public SkipListSetItem getPrevious() {
            return previous;
        }
        public T getValue() {
            return value;
        }
        public Integer getLevel() {
            return level;
        }

        // Setters
        public void setNext(SkipListSetItem next) {
            this.next = next;
        }

        public void setPrevious(SkipListSetItem previous) {
            this.previous = previous;
        }

        public void setUp(SkipListSetItem up) {
            this.up = up;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public void setBelow(SkipListSetItem below) {
            this.below = below;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }
    }

    // iterator implementation, uses outer class T
    private class SkipListSetIterator implements Iterator<T> {
        private SkipListSetItem current;

        public SkipListSetIterator() {
            current = getBottomStart();
        }

        //checks if a node has a next or not, returns true or false
        @Override
        public boolean hasNext() {
            if(current == null) {
                return false;
            }
            SkipListSetItem nextItemChecker = current.getNext();

            if(nextItemChecker == null){
                return false;
            }
            //checks if it's the head or tail return false
            if( nextItemChecker.getValue()==null){
                return false;
            }
            return true;
        }

        // returns the next value
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            current = current.getNext();
            return current.getValue();
        }

        // Must implement remove per documentation (even if it throws exception)
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // variables
    private SkipListSetItem head; // Top-left node
    private Integer height;
    private Integer size;
    private Random random;

    // setting up the constructors
    public SkipListSet() {
        this.height = 1;
        this.size = 0;
        this.random = new Random();
        initializeSkipList();
    }

    //creates new skip-list containing all the elements needed
    public SkipListSet(Collection<? extends T> c) {
        this();
        addAll(c);
    }

    // helper function to initialize the skip list, helped with issues i had
    private void initializeSkipList() {
        head = new SkipListSetItem(); // Top-left node
        SkipListSetItem topRight = new SkipListSetItem(); // Top-right node

        // Connect top level
        head.setNext(topRight);
        topRight.setPrevious(head);
    }

    // Helper function to get the bottom-left node
    private SkipListSetItem getBottomStart() {
        SkipListSetItem current = head;
        while (current.getBelow() != null) {
            current = current.getBelow();
        }
        return current;
    }

    // Helper function to determine the level for a new node
    // returns int of how times it was true.
    private int getRandomLevel() {
        int level = 1;
        while (random.nextBoolean()) {
            level++;
        }
        return level;
    }

    // randomizes the height of the nodes
    public void reBalance() {
        if (isEmpty()) return;

        // collects all items in the skiplist
        List<T> items = new ArrayList<>();
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            items.add(it.next());
        }

        // clears and adds with new random heights
        clear();
        for (T item : items) {
            add(item);
        }
    }

    //initializes a new level, creating a new head and tail nodes
    private void increaseHeight() {

        SkipListSetItem newHead = new SkipListSetItem();
        SkipListSetItem newTail = new SkipListSetItem();

        //make sure they're connected
        newHead.setNext(newTail);
        newTail.setPrevious(newHead);
        newHead.setBelow(head);
        head.setUp(newHead);

        // find and connect the right node
        SkipListSetItem oldTail = head;
        while (oldTail.getNext() != null) {
            oldTail = oldTail.getNext();
        }
        //connect the levels with each other
        newTail.setBelow(oldTail);
        oldTail.setUp(newTail);

        // have head in the right spot
        head = newHead;
        height++;
    }

    // checks to see if new level needs to be added
    private boolean shouldIncreaseHeight() {
        return size >= Math.pow(2, height);
    }

    // the main search function,
    // helps find where the item to be located
    private SkipListSetItem findInsertionPoint(T item) {
        SkipListSetItem current = head;

        while (current != null) {
            // move right as far as possible at the current level
            while (current.getNext() != null &&
                    current.getNext().getValue() != null &&
                    current.getNext().compareTo(item) < 0) {
                current = current.getNext();
            }

            // goes down if it can to find the correct spot
            if (current.getBelow() != null) {
                current = current.getBelow();
            } else {
                break; // breaks when at bottom level, won't work if not there
            }
        }
        return current;
    }

    /*
    add function that adds the item to the skip list,
    adds a new level if needed
    inserts node between the neighbors at the correct spot
    connects the new node with levels above and below it
     */
    @Override
    public boolean add(T item) {

        if (item == null) {
            throw new IllegalArgumentException("Cannot add null to SkipList");
        }

        // check if item already exists
        if (contains(item)) {
            return false; // sets don't allow duplicates
        }

        // find where to insert at bottom level
        SkipListSetItem insertAfter = findInsertionPoint(item);

        // determine height for new node
        int newNodeLevel = getRandomLevel();

        // increase skip list height if necessary
        while (newNodeLevel > height || shouldIncreaseHeight()) {
            increaseHeight();
        }

        SkipListSetItem newNode = null;

        // temp node that connects list vertically
        SkipListSetItem below = null;

        // start from bottom and work up
        SkipListSetItem current = insertAfter;

        for (int level = 1; level <= newNodeLevel; level++) {

            // create new node for this level
            SkipListSetItem nodeAtLevel = new SkipListSetItem(item);
            nodeAtLevel.setLevel(level);

            // connecting the node to the one below it
            if (below != null) {
                nodeAtLevel.setBelow(below);
                below.setUp(nodeAtLevel);
            }
            below = nodeAtLevel;

            if (newNode == null) {
                newNode = nodeAtLevel;
            }

            // insert the node between its new neighbors
            SkipListSetItem nextNode = current.getNext();
            current.setNext(nodeAtLevel);
            nodeAtLevel.setPrevious(current);
            nodeAtLevel.setNext(nextNode);
            if (nextNode != null) {
                nextNode.setPrevious(nodeAtLevel);
            }

            // go up to the next level for the next iteration
            if (level < newNodeLevel) {
                // find where to insert the node at the level above
                while (current != null && current.getUp() == null) {
                    current = current.getPrevious();
                }
                if (current != null) {
                    current = current.getUp();
                }
            }
        }
        size++;
        return true;
    }

    //checks if nodes is in the skip list, uses findInsertionPoint to help find it,
    // true if found, false if not
    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if ( o == null)
            return false;

        T item = (T) o;

        SkipListSetItem position = findInsertionPoint(item);

        SkipListSetItem nextItem = position.getNext();
        if(nextItem == null || nextItem.getValue() == null){
            return false;
        }
        return nextItem.compareTo(item) == 0;
    }

    // Updated iterator - returns proper iterator type
    @Override
    public Iterator<T> iterator() {
        return new SkipListSetIterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    // returns first number in the skip-list
    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        SkipListSetItem bottom = getBottomStart();
        return bottom.getNext().getValue();
    }

    //traverses to the bottom list and returns last number in the skip-list.
    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        SkipListSetItem bottom = getBottomStart();
        while (bottom.getNext() != null && bottom.getNext().getValue() != null) {
            bottom = bottom.getNext();
        }
        return bottom.getValue();
    }


    @Override //returns new array containing all elements from the skip list
    public Object[] toArray() {
        Object[] result = new Object[size];
        int index = 0;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            result[index++] = it.next();
        }
        return result;
    }


    // copies all elements from skiplist into an array and returns the array
    @Override // had errors when having T instead of U, works as should now.
    @SuppressWarnings("unchecked")
    public <U> U[] toArray(U[] a) {
        if (a.length < size) {
            U[] newArray = (U[]) java.lang.reflect.Array.
                    newInstance(a.getClass().getComponentType(), size);
            a = newArray;
        }

        int index = 0;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            U element = (U) it.next();
            a[index++] = element;
        }

        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }


    /*
    removes the item from the skip list,
    finds the item at all levels where it exists
    removes the node from each level by updating neighbor connections
    decreases size when item is successfully found and removed
    */
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {

        if (o == null) return false;

        //used item instead because it was easier to read when looking back at my code.
        T item = (T) o;

        // setting variables up that ill use
        SkipListSetItem current = head;
        SkipListSetItem toRemove = null;

        // search down to bottom level, keeping track of the path
        while (current != null) {
            // traverse right as far as possible at current level
            while (current.getNext() != null &&
                    current.getNext().getValue() != null &&
                    current.getNext().compareTo(item) < 0) {
                current = current.getNext();
            }

            // checking to see if item is found at current level
            if (current.getNext() != null &&
                    current.getNext().getValue() != null &&
                    current.getNext().compareTo(item) == 0) {

                // when item is found, remove it at the current level
                SkipListSetItem nodeToRemove = current.getNext();
                SkipListSetItem nextNode = nodeToRemove.getNext();

                // Update the nodes
                current.setNext(nextNode);
                if (nextNode != null) {
                    nextNode.setPrevious(current);
                }

                // Keep reference to bottom node for final check
                if (toRemove == null) {
                    // going down to find the bottom node of this item
                    SkipListSetItem bottomNode = nodeToRemove;
                    while (bottomNode.getBelow() != null) {
                        bottomNode = bottomNode.getBelow();
                    }
                    toRemove = bottomNode;
                }
            }
            // Move down to the next level
            if (current.getBelow() != null) {
                current = current.getBelow();
            } else {
                break;
            }
        }
        // if item found and removed, decrease size.
        if (toRemove != null) {
            size--;
            return true;
        }
        return false; // item was not found
    }

    @Override // returns true if skiplist contains every item, false if not
    public boolean containsAll(Collection<?> c) {
        for (Object item : c) {
            if (!contains(item)) return false;
        }
        return true;
    }

    @Override // adds items one by one to the list
    public boolean addAll(Collection<? extends T> c) {
        boolean modified = false;
        for (T item : c) {
            if (add(item)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override // removes each item in the collection from this skip list
    public boolean removeAll(Collection<?> c) {
        for(Object item : c) {
            remove(item);
        }
        return true;
    }

    @Override // clears the skip list and resets it to an initial state
    public void clear() {
        head = null;
        height = 1;
        size = 0;
        initializeSkipList();
    }

    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException();
    }
}
