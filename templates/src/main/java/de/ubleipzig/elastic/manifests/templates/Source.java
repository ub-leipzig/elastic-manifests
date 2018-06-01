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
package de.ubleipzig.elastic.manifests.templates;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Source {

    @JsonProperty
    private Integer imageIndex;

    @JsonProperty
    private String imageServiceIRI;

    @JsonProperty
    private MetadataMap metadataMap;

    /**
     *
     * @return Integer
     */
    public Integer getImageIndex() {
        return imageIndex;
    }

    /**
     *
     * @param imageIndex Integer
     */
    public void setImageIndex(final Integer imageIndex) {
        this.imageIndex = imageIndex;
    }

    /**
     *
     * @return String
     */
    public String getImageServiceIRI() {
        return imageServiceIRI;
    }

    /**
     *
     * @param imageServiceIRI String
     */
    public void setImageServiceIRI(final String imageServiceIRI) {
        this.imageServiceIRI = imageServiceIRI;
    }

    /**
     *
     * @return MetadataMap
     */
    public MetadataMap getMetadataMap() {
        return metadataMap;
    }

    /**
     *
     * @param metadataMap MetadataMap
     */
    public void setMetadataMap(final MetadataMap metadataMap) {
        this.metadataMap = metadataMap;
    }
}
