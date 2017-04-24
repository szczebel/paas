package paas.rest.endpoints;

import javax.servlet.http.HttpServletRequest;

class ServerUrl {

    //todo: this is not recommended, might give incorrect results based on network topology
    //e.g. firewalls, proxies, reverse proxies might be hitting the app
    static String getServerUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();  // includes leading forward slash
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }
}
