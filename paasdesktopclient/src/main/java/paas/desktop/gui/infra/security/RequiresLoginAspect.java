package paas.desktop.gui.infra.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Aspect
@Component
public class RequiresLoginAspect {

    @Autowired LoginPresenter mainFrame;
    @Autowired LoginData loginData;

    @Before("@annotation(paas.desktop.gui.infra.security.RequiresLogin)")
    public void isLoggedIn(JoinPoint joinPoint) throws Throwable {
        if(!loginData.isLoggedIn()) {
            SwingUtilities.invokeLater(mainFrame::showLogin);
            throw new SecurityException("Not logged in");
        }
    }
}
