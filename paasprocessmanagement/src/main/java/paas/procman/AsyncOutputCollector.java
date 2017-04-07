package paas.procman;

import java.io.InputStream;
import java.util.*;

public class AsyncOutputCollector {

    private final int bufferSize;
    private final ArrayDeque<DatedMessage> buffer;

    public AsyncOutputCollector(int bufferSize) {
        buffer = new ArrayDeque<>(bufferSize+1);
        this.bufferSize = bufferSize;
    }

    public synchronized void appendLine(String line) {
        buffer.addLast(new DatedMessage(System.nanoTime(), line));
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

    private Thread reader;
    public synchronized void asyncCollect(String initialMessage, InputStream inputStream) {
        if(reader!=null) reader.interrupt();
        buffer.clear();
        appendLine(initialMessage);
        reader = new Thread(() -> {
            try (Scanner reader = new Scanner(inputStream)) {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    appendLine(line);
                }
            }
        });
        reader.setDaemon(true);
        reader.start();
    }
}
