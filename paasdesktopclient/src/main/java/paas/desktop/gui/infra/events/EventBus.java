package paas.desktop.gui.infra.events;

import org.springframework.stereotype.Component;
import swingutils.spring.edt.MustBeInEDT;

@Component
public class EventBus extends GenericEventBus {

    @MustBeInEDT
    @Override
    public void dispatch(Object event) {
        super.dispatch(event);
    }

    @MustBeInEDT
    @Override
    public void dispatchEvent(String eventName) {
        super.dispatchEvent(eventName);
    }
}
