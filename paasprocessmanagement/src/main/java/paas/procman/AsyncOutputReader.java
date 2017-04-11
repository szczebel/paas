package paas.procman;

import java.io.InputStream;
import java.util.Scanner;
import java.util.function.Consumer;

public class AsyncOutputReader {

    private final Consumer<String> drain;

    public AsyncOutputReader(Consumer<String> drain) {
        this.drain = drain;
    }

    private Thread reader;
    public synchronized void asyncCollect(String initialMessage, InputStream inputStream) {
        if(reader!=null) reader.interrupt();
        drain.accept(initialMessage);
        reader = new Thread(() -> {
            try (Scanner reader = new Scanner(inputStream)) {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    drain.accept(line);
                }
            }
            drain.accept("Process ended");
        });
        reader.setDaemon(true);
        reader.start();
    }
}
