package pl.lstypka.springSecurityDistributedSystem.config.config;
/**
 * Created by Lukasz Stypka on 2015-11-20.
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.ExpiringSession;
import org.springframework.session.web.http.HeaderHttpSessionStrategy;
import org.springframework.session.web.http.HttpSessionStrategy;
import org.springframework.session.web.http.SessionRepositoryFilter;
import pl.lstypka.springSecurityDistributedSystem.config.filter.CustomUsernamePasswordAuthenticationFilter;
import pl.lstypka.springSecurityDistributedSystem.config.handler.AuthenticationFailureHandler;
import pl.lstypka.springSecurityDistributedSystem.config.handler.AuthenticationSuccessHandler;
import pl.lstypka.springSecurityDistributedSystem.config.handler.RestAuthenticationEntryPoint;
import pl.lstypka.springSecurityDistributedSystem.config.repository.JpaSessionRepository;
import pl.lstypka.springSecurityDistributedSystem.config.repository.SessionEntity;
import pl.lstypka.springSecurityDistributedSystem.config.repository.SpringSessionRepository;
import pl.lstypka.springSecurityDistributedSystem.config.service.AuthService;
import org.springframework.session.SessionRepository;

@EnableJpaRepositories(basePackageClasses=SpringSessionRepository.class)
@EntityScan(basePackageClasses = SessionEntity.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final static String AUTHENTICATE_ENDPOINT = "/authenticate";

    // Beans connected with translating input and output to JSON
    @Bean
    AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthenticationFailureHandler();
    }

    @Bean
    AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler();
    }

    @Bean
    RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public CustomUsernamePasswordAuthenticationFilter authenticationFilter() throws Exception {
        CustomUsernamePasswordAuthenticationFilter authFilter = new CustomUsernamePasswordAuthenticationFilter();
        authFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(AUTHENTICATE_ENDPOINT, "POST"));
        authFilter.setAuthenticationManager(super.authenticationManager());
        authFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
        authFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        authFilter.setUsernameParameter("j_username");
        authFilter.setPasswordParameter("j_password");
        return authFilter;
    }

    // Bean responsible for getting information about user details
    @Bean
    AuthService authService() {
        return new AuthService();
    }

    ////////////////////

    @Autowired
    private SessionRepository<ExpiringSession> sessionRepository;

    @Autowired
    private SpringSessionRepository springSessionRepository;

    private HttpSessionStrategy httpSessionStrategy = new HeaderHttpSessionStrategy(); // or HeaderHttpSessionStrategy

    @Bean
    public SessionRepository<ExpiringSession> sessionRepository() {
        return new JpaSessionRepository(springSessionRepository);
    }



    ////////////////////



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SessionRepositoryFilter<ExpiringSession> sessionRepositoryFilter = new SessionRepositoryFilter<>(sessionRepository());
        sessionRepositoryFilter.setHttpSessionStrategy(httpSessionStrategy);
        http.exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint()).and().addFilterBefore(authenticationFilter(), CustomUsernamePasswordAuthenticationFilter.class).csrf().disable()
                .authorizeRequests().antMatchers("/**").authenticated().and().formLogin().loginProcessingUrl(AUTHENTICATE_ENDPOINT).failureHandler(authenticationFailureHandler())
                .successHandler(authenticationSuccessHandler()).and().logout().and().addFilterBefore(sessionRepositoryFilter, ChannelProcessingFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(authService());
    }

}