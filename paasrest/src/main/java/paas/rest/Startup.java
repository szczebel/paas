package paas.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import paas.host.Shell;

import javax.annotation.PostConstruct;

@Component
public class Startup {

    @Autowired Shell shell;

    @PostConstruct
    void startup() {
        //todo: register 'logfile' as special command if springboot's /logfile endpoint doesnt work...
    }
}
