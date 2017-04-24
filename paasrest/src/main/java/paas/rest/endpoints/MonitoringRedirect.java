package paas.rest.endpoints;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import paas.shared.Links;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static paas.rest.endpoints.ServerUrl.getServerUrl;

@Controller
public class MonitoringRedirect {

    @Autowired ApplicationRegistry registry;

    @Value("${spring.boot.admin.context-path}")
    private String monitoringPath;

    @GetMapping(Links.MONITOR)
    public String monitoringRedirect(@PathVariable long appId, HttpServletRequest request) {
        Collection<Application> apps = registry.getApplicationsByName("PaaS.HostedApp." + appId);
        Assert.isTrue(apps.size()<2, "Multiple applications with this id registered for monitoring");
        Assert.isTrue(apps.size()==1, "This application is not registered for monitoring");
        Application theApp = apps.iterator().next();
        String monitoringId = theApp.getId();
        String suffix = "/#/applications/" + monitoringId + "/details";
        String monitorUrl = getServerUrl(request) + monitoringPath;
        return "redirect:" + monitorUrl + suffix;
    }
}
