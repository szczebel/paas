package paas.host;

import paas.procman.AsyncOutputReader;
import paas.procman.DatedMessage;
import paas.procman.OutputBuffer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Shell {

    public static void main(String[] args) throws IOException, InterruptedException {
        long timestamp = 0;
        Shell shell = new Shell("cmd", new File("."));
        shell.start();
        timestamp = printNewOutput(timestamp, shell);
        shell.execute("echo hello");
        timestamp = printNewOutput(timestamp, shell);
        shell.execute("dir");
        printNewOutput(timestamp, shell);
//        shell.execute("echo hello");//this should throw, shell is dead
    }

    private static long printNewOutput(long timestamp, Shell shell) throws InterruptedException {
        Thread.sleep(1000);
        List<DatedMessage> newOutput = shell.getOutputNewerThan(timestamp);
        if(newOutput.isEmpty()) return timestamp;
        timestamp = newOutput.get(newOutput.size()-1).getTimestamp();
        newOutput.stream().map(DatedMessage::getMessage).forEach(System.out::println);
        return timestamp;
    }


    private static final int OUTPUT_MAX_SIZE = 300;
    private final File shellWorkingDir;
    private final String shellInvokeCmd;

    private Process shellProcess;
    private BufferedWriter shellWriter;
    private OutputBuffer outputBuffer = new OutputBuffer(OUTPUT_MAX_SIZE);
    private AsyncOutputReader outputReader = new AsyncOutputReader(outputBuffer);

    public Shell(String shellInvokeCmd, File shellWorkingDir) {
        this.shellInvokeCmd = shellInvokeCmd;
        this.shellWorkingDir = shellWorkingDir;
    }

    public List<DatedMessage> getOutputNewerThan(long timestamp) {
        return outputBuffer.getOutputNewerThan(timestamp);
    }

    public synchronized void execute(String command) throws IOException, InterruptedException {
        if (shellProcess == null || !shellProcess.isAlive()) {
            start();
        }
        outputBuffer.accept("[INPUT] >>>>> Command received : " + command);
        if(specialCommandsMap.containsKey(command)) specialCommandsMap.get(command).accept(outputBuffer);
        else {
            shellWriter.write(command);
            shellWriter.newLine();
            shellWriter.flush();
        }
    }


    private void start() throws IOException {
        if(shellProcess!=null && shellProcess.isAlive()) throw new IllegalStateException("Shell process already running");
        shellProcess = new ProcessBuilder()
                .command(shellInvokeCmd)
                .directory(shellWorkingDir)
                .redirectErrorStream(true)
                .start();
        shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
        outputReader.asyncCollect(
                "Shell process not running, creating one",
                shellProcess.getInputStream()
        );
    }

    public void killShellProcess(Consumer<String> output) {
        output.accept("Killing existing shell process");
        if(shellProcess!=null) shellProcess.destroyForcibly();
    }

    public void registerSpecialCommand(String command, Consumer<Consumer<String>> outputProducer) {
        registerSpecialCommand(command, outputProducer, false);
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public void registerSpecialCommand(String command, Consumer<Consumer<String>> outputProducer, boolean override) {
        if(override) specialCommandsMap.put(command, outputProducer);
        else {
            if(specialCommandsMap.containsKey(command)) throw new IllegalArgumentException("Special command '" + command + "' already defined");
            else specialCommandsMap.put(command, outputProducer);
        }
    }

    private Map<String, Consumer<Consumer<String>>> specialCommandsMap = new HashMap<>();{
        specialCommandsMap.put("restart", this::killShellProcess);
    }
}
