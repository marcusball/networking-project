package bitTorrentPkg;

import java.util.LinkedList;

/**
 * Because I'm lazy
 * @author http://stackoverflow.com/questions/5498865/size-limited-queue-that-holds-last-n-elements-in-java
 * @param <E>
 */
public class LimitedQueue<E> extends LinkedList<E> {
    private int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) { super.remove(); }
        return true;
    }
}