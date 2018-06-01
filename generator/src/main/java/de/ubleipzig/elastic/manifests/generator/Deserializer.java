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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ubleipzig.elastic.manifests.templates.ElasticResponse;
import de.ubleipzig.elastic.manifests.templates.ImageServiceResponse;

import java.io.IOException;
import java.io.InputStream;

public class Deserializer {
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Deserializer.
     */
    public Deserializer() {
    }

    /**
     *
     * @param is InputStream
     * @return ElasticResponse
     */
    public ElasticResponse mapElasticResponse(final InputStream is) {
        try {
            return MAPPER.readValue(is, new TypeReference<ElasticResponse>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *
     * @param is InputStream
     * @return ImageServiceResponse
     */
    public ImageServiceResponse mapServiceResponse(final InputStream is) {
        try {
            return MAPPER.readValue(is, new TypeReference<ImageServiceResponse>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
