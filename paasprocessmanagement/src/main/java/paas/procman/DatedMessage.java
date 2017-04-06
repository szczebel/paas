package paas.procman;

public class DatedMessage {

    private final long timestamp;
    private final String message;

    DatedMessage(long timestamp, String outputLine) {
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
