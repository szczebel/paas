package paas.desktop.dto;

public class DatedMessage {

    private long timestamp;
    private String message;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
