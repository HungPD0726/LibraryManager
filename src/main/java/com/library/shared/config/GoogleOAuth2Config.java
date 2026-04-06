package com.library.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.util.StringUtils;

@Configuration
public class GoogleOAuth2Config {

    @Bean
    @Conditional(GoogleOAuthEnabledCondition.class)
    ClientRegistrationRepository clientRegistrationRepository(Environment environment) {
        ClientRegistration googleRegistration = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(environment.getRequiredProperty("app.security.oauth2.google.client-id"))
                .clientSecret(environment.getRequiredProperty("app.security.oauth2.google.client-secret"))
                .scope("profile", "email")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();

        return new InMemoryClientRegistrationRepository(googleRegistration);
    }

    @Bean
    @Conditional(GoogleOAuthEnabledCondition.class)
    OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    static final class GoogleOAuthEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            return StringUtils.hasText(environment.getProperty("app.security.oauth2.google.client-id"))
                    && StringUtils.hasText(environment.getProperty("app.security.oauth2.google.client-secret"));
        }
    }
}
