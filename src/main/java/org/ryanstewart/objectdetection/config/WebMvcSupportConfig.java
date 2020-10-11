package org.ryanstewart.objectdetection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Configuration that registers static content resources and redirects available in the microservice.
 * <p>
 * In general additions of bundled static content resources will not be necessary for REST microservices.
 * If a microservice requires access to static content it is recommended to utilize RAVEn's S3 buckets
 * as the back-end.
 * <p>
 * The configuration of static content resources is exercised for the web pages of microservices - which
 * only consists of Swagger documentation pages. This file is based off of the recommendations of the
 * Springfox team for migrating from 2.x to 3.x.
 *
 * @author Thomas Kajder
 * @version 1.0.0
 * @see WebMvcSupportConfig
 * @see <a href="https://springfox.github.io/springfox/docs/current/#migrating-from-existing-2-x-version">Springfox 2.x Migration Recommendation</a>
 * @since 1.0.0
 */
@Configuration
public class WebMvcSupportConfig extends WebMvcConfigurationSupport {

	/**
	 * Add several static resources for css, js, and static html files to display Swagger
	 * documentation pages.
	 *
	 * @param resourceRegistry registry of handlers for static content
	 * @see ResourceHandlerRegistry
	 * @since 1.0.0
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry resourceRegistry) {
		if (!resourceRegistry.hasMappingForPattern("/webjars/**")) {
			resourceRegistry
					.addResourceHandler("/webjars/**")
					.addResourceLocations("classpath:/META-INF/resources/webjars/");
		}

		if (!resourceRegistry.hasMappingForPattern("/swagger-ui/**")) {
			// It is recommended by Springfox 3.x to disable caching of the static Swagger page content
			resourceRegistry
					.addResourceHandler("/swagger-ui/**")
					.addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
					.resourceChain(false);
		}
	}

	/**
	 * Set several redirects for ease of use of the Swagger documentation web pages.
	 * <p>
	 * This configuration was expanded to redirect the Springfox 2.x url of {@literal /swagger-ui.html}
	 * to the new Springfox 3.x url of {@literal /swagger-ui/index.html}.
	 *
	 * @param viewRegistry registry of handlers for simple views and status codes
	 * @see ViewControllerRegistry
	 * @since 1.0.0
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry viewRegistry) {
		viewRegistry.addRedirectViewController("/swagger-ui.html", "/swagger-ui/index.html");
		viewRegistry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html");
	}
}
