package paas.procman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CompositeOutputDrain implements Consumer<String> {

    private final List<Consumer<String>> drains;

    CompositeOutputDrain(Collection<Consumer<String>> drains) {
        this.drains = new ArrayList<>(drains);
    }

    @Override
    public void accept(String string) {
        drains.forEach(c -> c.accept(string));
    }
}
