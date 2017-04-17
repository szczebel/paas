package paas.desktop.remoting;

import org.springframework.stereotype.Component;
import restcall.RestCall;
import swingutils.spring.edt.MustNotBeInEDT;

import java.util.List;

import static paas.shared.Links.REGISTER;
import static paas.shared.Links.WHOAMI;
import static restcall.RestCall.restPostVoid;

@Component
public class AuthService {

    @MustNotBeInEDT
    public UserInfo whoAmI(String serverUrl, String username, String password) {
        return
                RestCall.restGet(serverUrl + WHOAMI, UserInfo.class)
                        .httpBasic(username, password)
                        .execute();
    }

    @MustNotBeInEDT
    public void register(String username, String password, String serverUrl) {
        restPostVoid(serverUrl + REGISTER)
                .param("username", username)
                .param("password", password)//todo send hashed?
                .execute();
    }

    public static class UserInfo {
        List<AuthorityInfo> authorities;

        public List<AuthorityInfo> getAuthorities() {
            return authorities;
        }

        public void setAuthorities(List<AuthorityInfo> authorities) {
            this.authorities = authorities;
        }

        public static class AuthorityInfo {
            String authority;

            public String getAuthority() {
                return authority;
            }

            public void setAuthority(String authority) {
                this.authority = authority;
            }

            @Override
            public String toString() {
                return authority;
            }
        }
    }
}
