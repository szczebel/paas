package paas.desktop.gui.infra.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.desktop.gui.infra.EventBus;

import javax.swing.*;

import static paas.desktop.gui.ViewRequest.LOGIN;

@Aspect
@Component
public class RequiresLoginAspect {

    @Autowired EventBus eventBus;
    @Autowired LoginData loginData;

    @Before("@annotation(paas.desktop.gui.infra.security.RequiresLogin)")
    public void isLoggedIn(JoinPoint joinPoint) throws Throwable {
        if(!loginData.isLoggedIn()) {
            SwingUtilities.invokeLater(() -> eventBus.requestView(LOGIN));
            throw new SecurityException("Not logged in");
        }
    }
}
