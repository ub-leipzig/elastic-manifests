package de.ubleipzig.elastic.manifests.generator;/*
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

import static de.ubleipzig.elastic.manifests.generator.AbstractSerializer.serialize;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jdk.incubator.http.HttpRequest.BodyPublisher.fromInputStream;
import static jdk.incubator.http.HttpResponse.BodyHandler.asString;

import de.ubleipzig.elastic.manifests.templates.CrossFieldQuery;
import de.ubleipzig.elastic.manifests.templates.ElasticHits;
import de.ubleipzig.elastic.manifests.templates.ElasticResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.rdf.jena.JenaRDF;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

public class MapElasticResponseTest {
    static final JenaRDF rdf = new JenaRDF();

    private static HttpClient getClient() {
        final ExecutorService exec = Executors.newCachedThreadPool();
        return HttpClient.newBuilder().executor(exec).build();
    }

    @Test
    void testMapElasticResponse() {
        final InputStream is = MapElasticResponseTest.class.getResourceAsStream("/test1.json");
        final Deserializer deserializer = new Deserializer();
        final ElasticResponse res = deserializer.mapElasticResponse(is);
        final ElasticHits hits = res.getHits();
    }

    @Test
    void generateManifestFromElasticResponse() {
        final InputStream is = MapElasticResponseTest.class.getResourceAsStream("/test.json");
        final ManifestBuilder builder = new ManifestBuilder(is);
        final String json = builder.build();
        System.out.println(json);
    }

    @Test
    void generateManifestFromCrossFieldResponse() throws URISyntaxException, IOException, InterruptedException {
        final CrossFieldQuery cfq = new CrossFieldQuery();
        cfq.setSize(100);
        cfq.query = cfq.new Query();
        cfq.query.multi_match = cfq.query.new MultiMatch();
        cfq.query.multi_match.query = "1799 Sommerhalbjahr";

        final Optional<String> json = serialize(cfq);
        if (json.isPresent()) {
            final InputStream is = new ByteArrayInputStream(json.get().getBytes());
            final HttpClient client = getClient();
            final HttpRequest req = HttpRequest.newBuilder(
                    new URI("http://workspaces.ub.uni-leipzig.de:9100/vp4/_search")).headers(
                    CONTENT_TYPE, "application/json").POST(fromInputStream(() -> is)).build();
            final HttpResponse<String> response = client.send(req, asString());
            if (response.statusCode() == 200) {
                final String res = response.body();
                final InputStream er = new ByteArrayInputStream(res.getBytes());
                final ManifestBuilder builder = new ManifestBuilder(er);
                final String manifest = builder.build();
                System.out.println(manifest);
            }
        }
    }

    @Test
    void generateAtomManifestFromCrossFieldResponse() throws URISyntaxException, IOException, InterruptedException {
        final CrossFieldQuery cfq = new CrossFieldQuery();
        cfq.setSize(200);
        cfq.query = cfq.new Query();
        cfq.query.multi_match = cfq.query.new MultiMatch();
        cfq.query.multi_match.query = "0000009857";

        final Optional<String> json = serialize(cfq);
        if (json.isPresent()) {
            final InputStream is = new ByteArrayInputStream(json.get().getBytes());
            final HttpClient client = getClient();
            final HttpRequest req = HttpRequest.newBuilder(
                    new URI("http://workspaces.ub.uni-leipzig.de:9100/t4/_search")).headers(
                    CONTENT_TYPE, "application/json").POST(fromInputStream(() -> is)).build();
            final HttpResponse<String> response = client.send(req, asString());
            if (response.statusCode() == 200) {
                final String res = response.body();
                final InputStream er = new ByteArrayInputStream(res.getBytes());
                final AtomicManifestBuilder builder = new AtomicManifestBuilder(er);
                final String manifest = builder.build();
                System.out.println(manifest);
            }
        }
    }

    @Disabled
    @Test
    void generateManifestFromCamelAPI() throws URISyntaxException, IOException, InterruptedException {
        final CrossFieldQuery cfq = new CrossFieldQuery();
        cfq.setSize(100);
        cfq.query = cfq.new Query();
        cfq.query.multi_match = cfq.query.new MultiMatch();
        cfq.query.multi_match.query = "1799 Sommerhalbjahr";

        final Optional<String> json = serialize(cfq);
        if (json.isPresent()) {
            final InputStream is = new ByteArrayInputStream(json.get().getBytes());
            final HttpClient client = getClient();
            final HttpRequest req = HttpRequest.newBuilder(
                    new URI("http://localhost:9090/generator")).headers(
                    CONTENT_TYPE, "application/json").POST(fromInputStream(() -> is)).build();
            final HttpResponse<String> response = client.send(req, asString());
            System.out.println(response.body());
        }
    }
}
