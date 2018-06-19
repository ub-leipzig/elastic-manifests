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
import java.util.Objects;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public final class GeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorTest.class);
    private static final String HTTP_ACCEPT = "Accept";
    private static final String QUERY = "q";
    private static final String EMPTY = "empty";
    private static final String TYPE = "type";
    private static final String contentTypeJson = "application/json";

    private static final JedisConnectionFactory CONNECTION_FACTORY = new JedisConnectionFactory();
    private static RedisTemplate<String, String> redisTemplate;

    static {
        CONNECTION_FACTORY.setHostName("localhost");
        CONNECTION_FACTORY.setPort(6379);
        CONNECTION_FACTORY.afterPropertiesSet();
        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(CONNECTION_FACTORY);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
    }

    private GeneratorTest() {
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("About to run GeneratorTest API...");
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        Objects.requireNonNull(registry).bind("redisTemplate", redisTemplate);
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
                        .to("direct:redis-get");
                from("direct:redis-get")
                        .routeId("RedisGet")
                        .log(INFO, LOGGER, "${headers}")
                        .process(e -> {
                            final String httpquery = e.getIn().getHeader(QUERY).toString();
                            if (redisTemplate.opsForValue().get(httpquery) != null) {
                                e.getIn().setBody(redisTemplate.opsForValue().get(httpquery));
                                LOGGER.info("Getting query result from Redis Cache");
                            } else {
                                e.getIn().setHeader(CONTENT_TYPE, EMPTY);
                            }
                        })
                        .choice()
                        .when(header(CONTENT_TYPE).isEqualTo(EMPTY))
                        .to("direct:getQuery");
                from("direct:postQuery")
                        .routeId("postQuery")
                        .log(INFO, LOGGER, "Posting Query to Elastic Search API")
                        .convertBodyTo(String.class)
                        .setHeader(HTTP_METHOD)
                        .constant("POST")
                        .setHeader(CONTENT_TYPE)
                        .constant(contentTypeJson)
                        .process(e -> e.getIn().setBody(e.getIn().getBody()))
                        .to("http4:{{elasticsearch.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                        .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                        .setHeader(CONTENT_TYPE)
                        .constant(contentTypeJson)
                        .convertBodyTo(String.class)
                        .log(INFO, LOGGER, "Building JSON-LD Manifest from Query Results")
                        .choice()
                        .when(header(TYPE).isEqualTo("jld"))
                        .to("direct:buildManifest")
                        .when(header(TYPE).isEqualTo("atomic"))
                        .to("direct:buildAtomManifest");
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
                        .constant(contentTypeJson)
                        .to("http4:{{elasticsearch.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                        .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                        .setHeader(CONTENT_TYPE)
                        .constant(contentTypeJson)
                        .convertBodyTo(String.class)
                        .log(INFO, LOGGER, "Building JSON-LD Manifest from Query Results")
                        .choice()
                        .when(header(TYPE).isEqualTo("jld"))
                        .to("direct:buildManifest")
                        .when(header(TYPE).isEqualTo("atomic"))
                        .to("direct:buildAtomManifest");
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
                        })
                        .to("direct:redis-put");
                from("direct:buildAtomManifest").routeId("AtomicManifestBuilder")
                        .setHeader(HTTP_CHARACTER_ENCODING)
                        .constant("UTF-8")
                        .setHeader(CONTENT_TYPE)
                        .constant("application/ld+json")
                        .log(INFO, LOGGER, "Building Atomic Manifest")
                        .process(e -> {
                            final String jsonResults = e.getIn().getBody().toString();
                            final InputStream is = new ByteArrayInputStream(jsonResults.getBytes());
                            final AtomicManifestBuilder builder = new AtomicManifestBuilder(is);
                            e.getIn().setBody(builder.build());
                        })
                        .to("direct:redis-put");
                from("direct:redis-put")
                        .routeId("RedisPut")
                        .log(INFO, LOGGER, "Storing query result in Redis Cache")
                        .process(e -> {
                            final String httpquery = e.getIn().getHeader(QUERY).toString();
                            final String body = e.getIn().getBody().toString();
                            if (null == redisTemplate.opsForValue().get(httpquery)) {
                                redisTemplate.opsForValue().set(httpquery, body);
                            }
                            e.getIn().setBody(redisTemplate.opsForValue().get(httpquery));
                        });
            }
        });

        camelContext.start();

        Thread.sleep(60 * 60 * 1000);

        camelContext.stop();

    }
}






