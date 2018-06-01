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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ubleipzig.iiif.vocabulary.SCEnum;

import java.util.List;

@JsonInclude(Include.NON_NULL)
public class Canvas {

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type = SCEnum.Canvas.compactedIRI();

    @JsonProperty("images")
    private List<PaintingAnnotation> images;

    @JsonProperty("metadata")
    private List<Metadata> metadata;

    @JsonProperty("label")
    private String label;

    @JsonProperty("height")
    private int height;

    @JsonProperty("width")
    private int width;

    /**
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * @param id String
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return int
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height int
     */
    public void setHeight(final int height) {
        this.height = height;
    }

    /**
     * @return int
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width int
     */
    public void setWidth(final int width) {
        this.width = width;
    }

    /**
     * @return List
     */
    public List<PaintingAnnotation> getImages() {
        return images;
    }

    /**
     * @param images List
     */
    public void setImages(final List<PaintingAnnotation> images) {
        this.images = images;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public String getLabel() {
        return this.label;
    }

    /**
     *
     * @param label String
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * @return String
     */
    @JsonIgnore
    public List<Metadata> getMetadata() {
        return this.metadata;
    }

}
