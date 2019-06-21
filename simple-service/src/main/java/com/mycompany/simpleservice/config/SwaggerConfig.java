package com.mycompany.simpleservice.config;


import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.builders.PathSelectors.ant;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

import com.fasterxml.classmate.TypeResolver;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Profile("!prod")
public class SwaggerConfig {

    @Autowired
    private TypeResolver typeResolver;

    @Value("http://192.168.99.100:8080/auth")
    private String keycloakUrl;

    @Value("sampleClient")
    private String clientId;

    @Value("realmtest")
    private String realm;

    @Bean
    public Docket selfCareApi() {

        return new Docket(DocumentationType.SWAGGER_2)
                .globalOperationParameters(Collections.singletonList(new ParameterBuilder().
                        name("Accept-Language")
                        .allowableValues(
                                new AllowableListValues(Arrays.asList("fr_Fr", "en-US"), "string"))
                        .description("API accepts languages")
                        .modelRef(new ModelRef("string"))
                        .parameterType("header")
                        .build()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mycompany"))
                .paths(PathSelectors.any())
                .build()
                .pathMapping("/")
                .directModelSubstitute(LocalDate.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(
                        newRule(typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                                typeResolver.resolve(WildcardType.class)))
                .useDefaultResponseMessages(false)
                .enableUrlTemplating(false).apiInfo(apiInfo())
                .securitySchemes(newArrayList(oAuth()))
                .securityContexts(newArrayList(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("API EE").description("API EE, Attijariwafa Bank")
                .contact(contact()).version("V1").build();
    }

    private Contact contact() {
        return new Contact("E-banking ee", "https://www.attijariwafa.com/", "contact@attijariwafa.com");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(ant("/**"))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("openid profile", "access everything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return newArrayList(
                new SecurityReference("swagger_auth", authorizationScopes)
        );
    }

    private OAuth oAuth() {
        return new OAuthBuilder()
                .scopes(Collections.singletonList(
                        new AuthorizationScopeBuilder().scope("openid profile").build()))
                .grantTypes(newArrayList(new ResourceOwnerPasswordCredentialsGrant(keycloakUrlBuilder())))
                .name("swagger_auth")
                .build();
    }

    @Bean
    SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId(clientId)
                .clientSecret(null)
                .realm(realm)
                .appName(clientId)
                .scopeSeparator(" ")
                .useBasicAuthenticationWithAccessCodeGrant(false)
                .build();
    }

    private String keycloakUrlBuilder() {
        return keycloakUrl
                + "/realms/"
                + realm + "/protocol/openid-connect/token";
    }
}
