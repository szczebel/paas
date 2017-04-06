package paas.host;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class Shell {

    public static void main(String[] args) throws IOException, InterruptedException {
        long timestamp = 0;
        List<ShellOutput> newOutput;
        Shell shell = new Shell("cmd");
        shell.start();
        timestamp = printNewOutput(timestamp, shell);
        shell.execute("echo hello");
        timestamp = printNewOutput(timestamp, shell);
        shell.execute("dir");
        timestamp = printNewOutput(timestamp, shell);
//        shell.execute("echo hello");//this should throw, shell is dead
    }

    private static long printNewOutput(long timestamp, Shell shell) throws InterruptedException {
        Thread.sleep(1000);
        List<ShellOutput> newOutput = shell.getOutputNewerThan(timestamp);
        if(newOutput.isEmpty()) return timestamp;
        timestamp = newOutput.get(newOutput.size()-1).getTimestamp();
        newOutput.stream().map(ShellOutput::getOutputLine).forEach(System.out::println);
        return timestamp;
    }

    private static final int OUTPUT_MAX_SIZE = 1000;
    private final String shellInvokeCmd;
    private final LinkedList<ShellOutput> output = new LinkedList<>();

    private Process shellProcess;
    private BufferedWriter shellWriter;

    public Shell(String shellInvokeCmd) {
        this.shellInvokeCmd = shellInvokeCmd;
    }

    public synchronized List<ShellOutput> getOutputNewerThan(long timestamp) {
        if(timestamp==0) return new ArrayList<>(output);
        LinkedList<ShellOutput> retval = new LinkedList<>();
        Iterator<ShellOutput> shellOutputIterator = output.descendingIterator();
        while (shellOutputIterator.hasNext()) {
            ShellOutput next = shellOutputIterator.next();
            if(next.getTimestamp() > timestamp) retval.addFirst(next);
            else break;
        }
        return retval;
    }

    private synchronized void appendLine(String line) {
        output.addLast(new ShellOutput(System.nanoTime(), line));
        if (output.size() > OUTPUT_MAX_SIZE) {
            output.removeFirst();
        }
    }

    public synchronized void execute(String command) throws IOException, InterruptedException {
        if(shellProcess == null || !shellProcess.isAlive()) {
            output.clear();
            appendLine("Shell process not running, creating one");
            start();
        }
        shellWriter.write(command);
        shellWriter.newLine();
        shellWriter.flush();
    }



    private void start() throws IOException {
        if(shellProcess!=null && shellProcess.isAlive()) throw new IllegalStateException("Shell process already running");
        shellProcess = new ProcessBuilder()
                .command(shellInvokeCmd)
                .directory(new File(System.getProperty("user.home")))
                .redirectErrorStream(true)
                .start();
        shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
        Thread outputReader = new Thread(this::readShellOutput);
        outputReader.setDaemon(true);
        outputReader.start();
    }

    private void readShellOutput() {
        System.out.println("running readoutput thread");
        try (Scanner reader = new Scanner(shellProcess.getInputStream())) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                appendLine(line);
            }
        }
    }


}
