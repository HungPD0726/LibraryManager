package com.library.support;

import jakarta.servlet.ServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ThymeleafRenderSupport {

    private ThymeleafRenderSupport() {
    }

    public static String render(String templateName,
                                String requestPath,
                                Map<String, Object> variables,
                                String username,
                                String... roles) {
        ServletContext servletContext = new MockServletContext();
        GenericWebApplicationContext applicationContext = new GenericWebApplicationContext();
        applicationContext.registerBean(
                "webSecurityExpressionHandler",
                DefaultWebSecurityExpressionHandler.class,
                DefaultWebSecurityExpressionHandler::new
        );
        applicationContext.setServletContext(servletContext);
        applicationContext.refresh();
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);

        try {
            String normalizedPath = requestPath.startsWith("/") ? requestPath : "/" + requestPath;
            MockHttpServletRequest request = new MockHttpServletRequest((MockServletContext) servletContext);
            request.setContextPath("/libraryManager");
            request.setRequestURI("/libraryManager" + normalizedPath);
            MockHttpServletResponse response = new MockHttpServletResponse();

            SpringTemplateEngine engine = new SpringTemplateEngine();
            engine.setTemplateResolver(templateResolver());
            engine.addDialect(new LayoutDialect());
            engine.addDialect(new SpringSecurityDialect());

            if (username != null && roles != null && roles.length > 0) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        username,
                        "n/a",
                        Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList()
                ));
            }

            Map<String, Object> model = new HashMap<>(variables);
            model.putIfAbsent("requestPath", normalizedPath);
            model.putIfAbsent("currentUsername", username);
            model.putIfAbsent("isAdmin", hasRole("ROLE_ADMIN", roles));
            model.putIfAbsent("isStaff", hasRole("ROLE_STAFF", roles) || hasRole("ROLE_LIBRARIAN", roles));
            model.putIfAbsent("isStudent", hasRole("ROLE_STUDENT", roles));
            model.putIfAbsent("unreadNotifications", 0L);
            model.putIfAbsent("pendingBorrowCount", 0L);

            JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
            WebContext context = new WebContext(
                    webApplication.buildExchange(request, response),
                    Locale.forLanguageTag("vi"),
                    model
            );
            return engine.process(templateName, context);
        } finally {
            SecurityContextHolder.clearContext();
            applicationContext.close();
        }
    }

    private static ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        return resolver;
    }

    private static boolean hasRole(String role, String... roles) {
        return roles != null && Arrays.asList(roles).contains(role);
    }
}
