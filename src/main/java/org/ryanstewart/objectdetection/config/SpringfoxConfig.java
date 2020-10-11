package org.ryanstewart.objectdetection.config;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Configuration that specifies the Swagger documentation for the API. The documentation consists of
 * general information, security requirements, and endpoint documentation.
 * <p>
 * The Swagger configuration specified is tightly coupled to the Swagger 2.x spec, and is not compliant with
 * the OpenAPI 3.x spec. If an application requires compliance with the OpenAPI 3.x spec please contact
 * the maintainers of this repository.
 *
 * @version 1.0.0
 * @see <a href="http://springfox.github.io/springfox/docs/current/">Springfox Documentation</a>
 * @see <a href="https://swagger.io/specification/v2/">Swagger 2.0 Specification</a>
 * @since 1.0.0
 */
@Configuration
public class SpringfoxConfig {

	/**
	 * Selector to grab only requests defined in this project, instead of requests in imported projects.
	 *
	 * @see RequestHandlerSelectors
	 * @since 1.0.0
	 */
	private static final Predicate<RequestHandler> IS_PROJECT_REQUEST_HANDLER = RequestHandlerSelectors
			.basePackage("org.ryanstewart.objectdetection");

	/**
	 * Selector to grab only endpoints that begin with {@literal /api/}.
	 *
	 * @see PathSelectors
	 * @since 1.0.0
	 */
	private static final Predicate<String> IS_API_PATH = PathSelectors.ant("/api/**");

	/**
	 * The header used for authentication and authorization to the microservice is {@value}.
	 *
	 * @since 1.0.0
	 */
	private static final String AUTHORIZATION_HEADER = "Authorization";

	/**
	 * The type of key used for authentication and authorization to the microservice is {@value}.
	 *
	 * @since 1.0.0
	 */
	private static final String API_KEY_TYPE = "JWT_TOKEN";

	/**
	 * Create the general API information available to consumers consisting of name, version,
	 * terms of service, contact information, and licensing.
	 *
	 * @return the general API information
	 * @since 1.0.0
	 */
	private static ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("ObjectDetectionApi")
				.description("Tensorflow Springboot API")
				.version("1.0.0")
				.contact(contactInfo())
				.licenseUrl("No License")
				.extensions(Collections.emptyList())
				.build();
	}

	/**
	 * Create the documentation for required authentication and authorization methods for endpoints.
	 * <p>
	 * The default behavior enforces a Keycloak JWT token passed in the header for all endpoints under {@literal /api/}.
	 * The authentication and authorization token is not required for accessing the Swagger documentation
	 * web pages.
	 *
	 * @return the documentation for required authentication and authorization methods for endpoints
	 * @since 1.0.0
	 */
	private static SecurityContext securityContext() {
		return SecurityContext.builder()
				.securityReferences(globalAuthorizationScope())
				.operationSelector(operationContext -> IS_API_PATH.test(operationContext.requestMappingPattern()))
				.build();
	}

	/**
	 * Create the documentation for the token required for authentication and authorization to the API.
	 * <p>
	 * The default token is a Keycloak JWT token passed in the header.
	 *
	 * @return the documentation for the token required for authentication and authorization
	 */
	private static ApiKey apiKey() {
		return new ApiKey(API_KEY_TYPE, AUTHORIZATION_HEADER, "header");
	}

	/**
	 * Create the primary point of contact for the API consisting of name, url, and email.
	 *
	 * @return the primary point of contact for the API
	 * @since 1.0.0
	 */
	private static Contact contactInfo() {
		return new Contact(
				"Ryan Stewart",
				"https://www.greenzoneinc.com/",
				"ryan.stewart113@gmail.com");
	}

	/**
	 * Create a list of security references that document the authorization and
	 * scope the authorization applies to.
	 * <p>
	 * The default security reference requires the Keycloak JWT token for all scopes.
	 *
	 * @return a list of security references that document the authorization and
	 * scope the authorization applies to
	 * @since 1.0.0
	 */
	private static List<SecurityReference> globalAuthorizationScope() {
		final AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");

		final SecurityReference securityReference = SecurityReference.builder()
				.reference(API_KEY_TYPE)
				.scopes(new AuthorizationScope[] { authorizationScope })
				.build();

		return Collections.singletonList(securityReference);
	}

	/**
	 * Create the bean of the full documentation for the API consisting of general information,
	 * security requirements, and endpoint documentation.
	 *
	 * @return the full documentation for the API
	 * @since 1.0.0
	 */
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.genericModelSubstitutes(ResponseEntity.class)
				.securityContexts(Collections.singletonList(securityContext()))
				.securitySchemes(Collections.singletonList(apiKey()))
				.useDefaultResponseMessages(false)
				.select()
				.apis(IS_PROJECT_REQUEST_HANDLER)
				.paths(IS_API_PATH)
				.build();
	}
}