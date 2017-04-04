package paas.procman;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

class Tail {

    static List<String> tail(File file, int limit) throws IOException {
        //Very inefficient implementation.
        //Proper implementation would use RandomAccessFile to read from the end of the file
        ArrayDeque<String> arrayDeque = new ArrayDeque<>(limit + 1);
        Files.lines(file.toPath()).forEach(s -> {
            arrayDeque.addLast(s);
            if (arrayDeque.size() > limit) {
                arrayDeque.removeFirst();
            }
        });
        return new ArrayList<>(arrayDeque);
    }
}
