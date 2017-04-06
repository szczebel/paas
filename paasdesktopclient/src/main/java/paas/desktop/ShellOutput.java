package paas.desktop;

public class ShellOutput {

    private long timestamp;
    private String outputLine;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setOutputLine(String outputLine) {
        this.outputLine = outputLine;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOutputLine() {
        return outputLine;
    }
}
