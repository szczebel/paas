package paas.host;

public class ShellOutput {

    private final long timestamp;
    private final  String outputLine;

    public ShellOutput(long timestamp, String outputLine) {
        this.timestamp = timestamp;
        this.outputLine = outputLine;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOutputLine() {
        return outputLine;
    }
}
