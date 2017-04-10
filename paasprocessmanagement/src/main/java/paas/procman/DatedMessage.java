package paas.procman;

public class DatedMessage {

    static DatedMessage msg(String message) {
        return new DatedMessage(System.currentTimeMillis(), message);
    }

    private final long timestamp;
    private final String message;

    private DatedMessage(long timestamp, String outputLine) {
        this.timestamp = timestamp;
        this.message = outputLine;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
