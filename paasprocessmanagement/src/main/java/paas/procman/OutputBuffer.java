package paas.procman;

import java.util.*;
import java.util.function.Consumer;

import static paas.procman.DatedMessage.msg;

public class OutputBuffer implements Consumer<String> {

    private final int bufferSize;
    private final ArrayDeque<DatedMessage> buffer;

    public OutputBuffer(int bufferSize) {
        buffer = new ArrayDeque<>(bufferSize+1);
        this.bufferSize = bufferSize;
    }


    @Override
    public synchronized void accept(String line) {
        buffer.addLast(msg(line));
        if (buffer.size() > bufferSize) {
            buffer.removeFirst();
        }
    }

    public synchronized List<DatedMessage> getOutputNewerThan(long timestamp) {
        if(timestamp==0) return new ArrayList<>(buffer);
        LinkedList<DatedMessage> retval = new LinkedList<>();
        Iterator<DatedMessage> shellOutputIterator = buffer.descendingIterator();
        while (shellOutputIterator.hasNext()) {
            DatedMessage next = shellOutputIterator.next();
            if(next.getTimestamp() > timestamp) retval.addFirst(next);
            else break;
        }
        return retval;
    }
}
