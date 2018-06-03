/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ubleipzig.elastic.manifests.generator;

import static de.ubleipzig.elastic.manifests.generator.ContextUtils.createInitialContext;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_CHARACTER_ENCODING;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorTest.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String QUERY = "q";

    private GeneratorTest() {
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("About to run GeneratorTest API...");
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        final CamelContext camelContext = new DefaultCamelContext(registry);

        camelContext.addRoutes(new RouteBuilder() {
            public void configure() {
                final PropertiesComponent pc = getContext().getComponent("properties", PropertiesComponent.class);
                pc.setLocation("classpath:application.properties");

                from("jetty:http://{{api.host}}:{{api.port}}{{api.prefix}}?" +
                        "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false" +
                        "&httpMethodRestrict=GET,POST,OPTIONS")
                        .routeId("Generator")
                        .removeHeaders(HTTP_ACCEPT)
                        .setHeader("Access-Control-Allow-Origin")
                        .constant("*")
                        .choice()
                        .when(header(HTTP_METHOD).isEqualTo("POST"))
                        .to("direct:postQuery")
                        .when(header(HTTP_METHOD).isEqualTo("GET"))
                        .to("direct:getQuery");
                from("direct:postQuery")
                        .routeId("postQuery")
                        .log(INFO, LOGGER, "Posting Query to Elastic Search API")
                        .convertBodyTo(String.class)
                        .setHeader(HTTP_METHOD)
                        .constant("POST")
                        .setHeader(CONTENT_TYPE)
                        .constant("application/json")
                        .process(e -> e.getIn().setBody(e.getIn().getBody()))
                        .to("http4:{{elasticsearch.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                        .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                        .setHeader(CONTENT_TYPE)
                        .constant("application/json")
                        .convertBodyTo(String.class)
                        .log(INFO, LOGGER, "Building JSON-LD Manifest from Query Results")
                        .to("direct:buildManifest");
                from("direct:getQuery")
                        .routeId("getQuery")
                        .log(INFO, LOGGER, "Get Query from Elastic Search API")
                        .process(e -> {
                            final String query = e.getIn().getHeader(QUERY).toString();
                            e.getIn().setBody(query);
                        })
                        .removeHeaders("Camel*")
                        .setHeader(HTTP_METHOD)
                        .constant("POST")
                        .setHeader(CONTENT_TYPE)
                        .constant("application/json")
                        .to("http4:{{elasticsearch.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                        .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                        .setHeader(CONTENT_TYPE)
                        .constant("application/json")
                        .convertBodyTo(String.class)
                        .log(INFO, LOGGER, "Building JSON-LD Manifest from Query Results")
                        .to("direct:buildManifest");
                from("direct:buildManifest").routeId("ManifestBuilder")
                        .setHeader(HTTP_CHARACTER_ENCODING)
                        .constant("UTF-8")
                        .setHeader(CONTENT_TYPE)
                        .constant("application/ld+json")
                        .log(INFO, LOGGER, "Building Manifest")
                        .process(e -> {
                            final String jsonResults = e.getIn().getBody().toString();
                            final InputStream is = new ByteArrayInputStream(jsonResults.getBytes());
                            final ManifestBuilder builder = new ManifestBuilder(is);
                            e.getIn().setBody(builder.build());
                        });
            }
        });

        camelContext.start();

        Thread.sleep(5 * 60 * 1000);

        camelContext.stop();

    }
}






