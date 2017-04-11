package paas.rest.endpoints;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import paas.shared.Links;

@Controller
public class KibanaRedirect {

    @Value("${kibana.host}")
    private String kibanaHost;

    @GetMapping(Links.KIBANA)
    public String kibanaRedirect(@PathVariable long appId) {
                    String url= kibanaHost + "/app/kibana?#/discover?_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-15m,mode:quick,to:now))&_a=(columns:!(level,message,logger_name),index:'logstash-*',interval:auto,query:(query_string:(analyze_wildcard:!t,query:'logger_name:HostedApp.appId." +
                    appId +
                    "')),sort:!('@timestamp',asc))";
                    return "redirect:" + url;
    }
}
