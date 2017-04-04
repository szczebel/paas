package paas.desktop.gui.infra;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Aspect
@Component
public class MustBeInBackgroundAspect {

    @Before("@annotation(paas.desktop.gui.infra.MustBeInBackground)")
    public void checkIfOnEDT(JoinPoint joinPoint) throws Throwable {
        if (SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("Methods annotated with @MustBeInBackground must not be called on EDT, " + joinPoint.getSignature());
    }
}
