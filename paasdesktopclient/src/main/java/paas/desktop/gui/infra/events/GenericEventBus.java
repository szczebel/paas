package paas.desktop.gui.infra.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@SuppressWarnings({"WeakerAccess", "unused"})
public class GenericEventBus {

    public <T> void when(Class<T> eventType, Consumer<T> listener) {
        getConsumerMap().add(eventType, listener);
    }

    public void dispatch(Object event) {
        Collection<Consumer<Object>> consumers = getConsumerMap().getConsumersOf(event.getClass());
        if(consumers.isEmpty()) deadLetterPosted(event);
        consumers.forEach(c -> c.accept(event));
    }

    public void when(String eventName, Runnable command) {
        getRunnableMap().add(eventName, command);
    }

    public void dispatchEvent(String eventName) {
        Collection<Runnable> commands = getRunnableMap().getFor(eventName);
        if(commands.isEmpty()) deadLetterPosted(eventName);
        commands.forEach(Runnable::run);
    }

    private Optional<Consumer<Object>> deadLetterHandler = Optional.empty();
    private void deadLetterPosted(Object deadLetter) {
        deadLetterHandler.orElseThrow(() -> new RuntimeException("Dead letter: " + deadLetter)).accept(deadLetter);
    }

    public void setDeadLetterHandler(Consumer<Object> consumer) {
        deadLetterHandler = Optional.of(consumer);
    }

    private ConsumerMap consumerMap = new ConsumerMap();
    protected ConsumerMap getConsumerMap() {
        return consumerMap;
    }

    private RunnableMap runnableMap = new RunnableMap();
    protected RunnableMap getRunnableMap() {
        return runnableMap;
    }

    protected static class ConsumerMap {
        private Collection<ConsumerWithType> consumers = new ArrayList<>();

        <T> void add(Class<T> eventType, Consumer<T> listener) {
            consumers.add(new ConsumerWithType<>(eventType, listener));
        }

        Collection<Consumer<Object>> getConsumersOf(Class<?> eventType) {
            //noinspection unchecked
            Stream<Consumer<Object>> consumerStream = consumers.stream()
                    .filter(c -> c.eventType.isAssignableFrom(eventType))
                    .map(ConsumerWithType::getConsumer);
            return consumerStream
                    .collect(toList());
        }

        static class ConsumerWithType<T> {

            private final Class<T> eventType;
            private final Consumer<T> listener;

            ConsumerWithType(Class<T> eventType, Consumer<T> listener) {
                this.eventType = eventType;
                this.listener = listener;
            }

            Consumer<Object> getConsumer() {
                //noinspection unchecked
                return (Consumer<Object>) listener;
            }
        }
    }

    protected static class RunnableMap {
        private Collection<RunnableWithEventName> commands = new ArrayList<>();
        public void add(String eventName, Runnable command) {
            commands.add(new RunnableWithEventName(eventName, command));
        }

        public Collection<Runnable> getFor(String eventName) {
            return commands.stream()
                    .filter(rwen -> eventName.equals(rwen.eventName))
                    .map(RunnableWithEventName::getCommand)
                    .collect(toList());
        }

        static class RunnableWithEventName {

            final String eventName;
            final Runnable command;

            public RunnableWithEventName(String eventName, Runnable command) {

                this.eventName = eventName;
                this.command = command;
            }

            Runnable getCommand() {
                return command;
            }
        }
    }

    static class Tester {
        public static void main(String[] args) {
            GenericEventBus bus = new GenericEventBus();
            bus.when("CMD1", Tester::command1);
            bus.when("CMD2", Tester::command2);
            bus.when(Integer.class, Tester::paramCommand1);
            bus.when(Bean.class, Tester::paramCommand2);
            bus.when(SubBean.class, Tester::paramCommand3);

            bus.dispatchEvent("CMD1");
            bus.dispatchEvent("CMD2");
            bus.dispatch(4);
            bus.dispatch(new Bean());//paramCommand2 should receive
            bus.dispatch(new SubBean());//paramCommand2 and paramCommand3 should receive

        }

        static void command1() {
            System.out.println("Command 1 executed");
        }

        static void command2() {
            System.out.println("Command 2 executed");
        }

        static void paramCommand1(Integer integer) {
            System.out.println("Integer received: " + integer);
        }

        static void paramCommand2(Bean string) {
            System.out.println("bean received: " + string);
        }

        static void paramCommand3(SubBean string) {
            System.out.println("subbean received: " + string);
        }

        static class Bean {}
        static class SubBean extends Bean {}
    }
}
