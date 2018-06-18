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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import de.ubleipzig.iiif.vocabulary.SCEnum;

import java.util.List;

/**
 * Manifest.
 *
 * @author christopher-johnson
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"@context", "@id", "@type", "label", "metadata", "attribution", "logo", "sequences", "service"})
public class Manifest {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SCEnum.Manifest.compactedIRI();

    @JsonProperty("label")
    private String label;

    @JsonProperty("attribution")
    private String attribution;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("sequences")
    private List<Sequence> sequences;

    @JsonProperty("service")
    private Service service;

    @JsonProperty("metadata")
    private List<Metadata> metadata;

    @JsonProperty("structures")
    private List<Structure> structures;

    /**
     * Manifest.
     */
    public Manifest() {
    }

    /**
     * @param context String
     */
    public void setContext(final String context) {
        this.context = context;
    }

    /**
     * @param id String
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param type String
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @param label String
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * @param attribution String
     */
    public void setAttribution(final String attribution) {
        this.attribution = attribution;
    }

    /**
     * @param logo String
     */
    public void setLogo(final String logo) {
        this.logo = logo;
    }

    /**
     * @param service String
     */
    public void setService(final Service service) {
        this.service = service;
    }

    /**
     * @param sequences List
     */
    public void setSequences(final List<Sequence> sequences) {
        this.sequences = sequences;
    }

    /**
     * @param structures List
     */
    public void setStructures(final List<Structure> structures) {
        this.structures = structures;
    }

    /**
     * @param metadata List
     */
    public void setMetadata(final List<Metadata> metadata) {
        this.metadata = metadata;
    }

}


