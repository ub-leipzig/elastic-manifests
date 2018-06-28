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
import static java.io.File.separator;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;

import java.io.InputStream;
import java.util.Objects;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Generator.
 *
 * @author christopher-johnson
 */
public class Generator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);
    private static final JedisConnectionFactory CONNECTION_FACTORY = new JedisConnectionFactory();
    private static RedisTemplate<String, String> redisTemplate;
    private static final String HTTP_ACCEPT = "Accept";
    private static final String QUERY = "q";
    private static final String EMPTY = "empty";
    private static final String TYPE = "type";
    private static final String contentTypeJson = "application/json";
    static {
        CONNECTION_FACTORY.setHostName("redis");
        CONNECTION_FACTORY.setPort(6379);
        CONNECTION_FACTORY.afterPropertiesSet();
    }

    /**
     * @param args String[]
     * @throws Exception Exception
     */
    public static void main(final String[] args) throws Exception {
        final Generator generator = new Generator();
        generator.init();
    }

    /**
     * @throws Exception Exception
     */
    private void init() throws Exception {
        final Main main = new Main();
        main.addRouteBuilder(new QueryRoute());
        main.addMainListener(new Events());
        final JndiRegistry registry = new JndiRegistry(createInitialContext());
        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(CONNECTION_FACTORY);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        Objects.requireNonNull(registry)
                .bind("redisTemplate", redisTemplate);
        //main.setPropertyPlaceholderLocations("classpath:de.ubleipzig.elastic.manifests.generator.cfg");
        main.setPropertyPlaceholderLocations("file:${env:GENERATOR_HOME}/de.ubleipzig.elastic.manifests.generator.cfg");
        main.run();
    }

    /**
     * Events.
     */
    public static class Events extends MainListenerSupport {

        @Override
        public void afterStart(final MainSupport main) {
            System.out.println("Generator is now started!");
        }

        @Override
        public void beforeStop(final MainSupport main) {
            System.out.println("Generator is now being stopped!");
        }
    }

    /**
     * QueryRoute.
     */
    public static class QueryRoute extends RouteBuilder {
        /**
         * configure.
         */
        public void configure() {
            from("jetty:http://{{api.host}}:{{api.port}}{{api.prefix}}?"
                    + "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false"
                    + "&httpMethodRestrict=GET,POST,OPTIONS")
                    .routeId("Generator")
                    .removeHeaders(HTTP_ACCEPT)
                    .setHeader("Access-Control-Allow-Origin")
                    .constant("*")
                    .choice()
                    .when(header(HTTP_METHOD).isEqualTo("POST"))
                    .to("direct:postQuery")
                    .when(header(HTTP_METHOD).isEqualTo("GET"))
                    .to("direct:redis-get");
            from("direct:redis-get").routeId("RedisGet")
                    .process(e -> {
                        final String httpquery = e.getIn()
                                .getHeader(QUERY)
                                .toString();
                        if (redisTemplate.opsForValue().get(httpquery) != null) {
                            e.getIn().setBody(redisTemplate.opsForValue().get(httpquery));
                            e.getIn().setHeader(CONTENT_TYPE, contentTypeJson);
                            LOGGER.info("Getting query result from Redis Cache");
                        } else {
                            e.getIn().setHeader(CONTENT_TYPE, EMPTY);
                        }
                    })
                    .choice()
                    .when(header(CONTENT_TYPE).isEqualTo(EMPTY))
                    .to("direct:getQuery");
            from("direct:postQuery").routeId("postQuery")
                    .log(INFO, LOGGER, "Posting Query to Elastic Search API")
                    .convertBodyTo(String.class)
                    .setHeader(HTTP_METHOD)
                    .constant("POST")
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJson)
                    .process(e -> e.getIn().setBody(e.getIn().getBody()))
                    .setHeader(HTTP_PATH, simple(separator + "${header.index}" + separator + "_search") )
                    .to("http4:{{elasticsearch.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                    .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                    .convertBodyTo(InputStream.class)
                    .log(INFO, LOGGER, "Building JSON-LD Manifest from Query Results")
                    .choice()
                    .when(header(TYPE).isEqualTo("orp"))
                    .to("direct:buildManifest")
                    .when(header(TYPE).isEqualTo("atomic"))
                    .to("direct:buildAtomManifest");
            from("direct:getQuery").routeId("getQuery")
                    .log(INFO, LOGGER, "Get Query from Elastic Search API")
                    .process(e -> {
                        e.getIn().setBody(e.getIn().getHeader(QUERY), InputStream.class);
                    })
                    .removeHeaders("Camel*")
                    .setHeader(HTTP_PATH, simple(separator + "${header.index}" + separator + "_search") )
                    .setHeader(HTTP_METHOD)
                    .constant("POST")
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJson)
                    .to("http4:{{elasticsearch.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
                    .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                    .convertBodyTo(InputStream.class)
                    .log(INFO, LOGGER, "Building JSON-LD Manifest from Query Results")
                    .choice()
                    .when(header(TYPE).isEqualTo("orp"))
                    .to("direct:buildManifest")
                    .when(header(TYPE).isEqualTo("atomic"))
                    .to("direct:buildAtomManifest");
            from("direct:buildManifest").routeId("ManifestBuilder")
                    .log(INFO, LOGGER, "Building Manifest")
                    .process(e -> {
                        final InputStream is = e.getIn().getBody(InputStream.class);
                        final ManifestBuilder builder = new ManifestBuilder(is);
                        e.getIn().setBody(builder.build());
                    })
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJson)
                    .to("direct:redis-put");
            from("direct:buildAtomManifest").routeId("AtomicManifestBuilder")
                    .log(INFO, LOGGER, "Building Atomic Manifest")
                    .process(e -> {
                        final InputStream is = e.getIn().getBody(InputStream.class);
                        final AtomicManifestBuilder builder = new AtomicManifestBuilder(is);
                        e.getIn().setBody(builder.build());
                    })
                    .setHeader(CONTENT_TYPE)
                    .constant(contentTypeJson)
                    .to("direct:redis-put");
            from("direct:redis-put").routeId("RedisPut")
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
    }
}