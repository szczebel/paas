package paas.rest.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import paas.rest.persistence.entities.PaasUser;
import paas.rest.persistence.repos.PaasUserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private PaasUserRepository paasUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PaasUser user = paasUserRepository.findOne(username);
        if(user==null) throw new UsernameNotFoundException(username);
        return User.withUsername(username)
                .password(user.getHashedPassword())
                .roles(Role.USER)
                .build();
    }

    public void create(String username, String hashedPassword) {
        if(paasUserRepository.findOne(username)!=null)
            throw new RuntimeException(username + " - user already exists");
        paasUserRepository.save(new PaasUser(username, hashedPassword));
    }
}
