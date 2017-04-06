package paas.host;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Shell {

    public static void main(String[] args) throws IOException, InterruptedException {
        Shell shell = new Shell("cmd");
        shell.start();
        System.out.println("--------------------------------------------------------------");
        shell.getOutput().forEach(System.out::println);
        shell.execute("echo hello");
        System.out.println("--------------------------------------------------------------");
        shell.getOutput().forEach(System.out::println);
        shell.execute("dir");
        System.out.println("--------------------------------------------------------------");
        shell.getOutput().forEach(System.out::println);
//        shell.execute("echo hello");//this should throw, shell is dead
    }

    private final String shellCmd;

    public Shell(String shellCmd) {
        this.shellCmd = shellCmd;
    }

    public synchronized List<String> getOutput() {
        return new ArrayList<>(output);
    }

    private final LinkedList<String> output = new LinkedList<>();
    private Process shellProcess;
    private BufferedWriter shellWriter;

    private synchronized void appendLine(String line) {
        output.addLast(line);
        if (output.size() > 100) {
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
                .command(shellCmd)
                .directory(new File(System.getProperty("user.home")))
                .redirectErrorStream(true)
                .start();
        shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
        new Thread(this::readShellOutput).start();
    }

    private void readShellOutput() {
        try (Scanner reader = new Scanner(shellProcess.getInputStream())) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                appendLine(line);
            }
        }
    }


}
