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

import java.util.List;
import java.util.Map;

public class AtomicElasticResponse {

    @JsonProperty
    private Integer took;

    @JsonProperty
    private Boolean timed_out;

    @JsonProperty
    private Object _shards;

    @JsonProperty
    private ElasticHits hits;

    /**
     * @return ElasticHits
     */
    public ElasticHits getHits() {
        return hits;
    }

    /**
     * @param hits List
     */
    public void setHits(final ElasticHits hits) {
        this.hits = hits;
    }

    public static class ElasticHits {

        @JsonProperty
        private Integer total;

        @JsonProperty
        private Integer max_score;

        @JsonProperty
        private List<Hits> hits;

        /**
         *
         * @return List
         */
        public List<Hits> getHits() {
            return hits;
        }

        /**
         *
         * @param hits List
         */
        public void setHits(final List<Hits> hits) {
            this.hits = hits;
        }
    }

    public static class Hits {
        @JsonProperty
        private String _index;

        @JsonProperty
        private String _type;

        @JsonProperty
        private String _id;

        @JsonProperty
        private Integer _score;

        public Source _source;

        /**
         *
         * @return Source
         */
        public Source getSource() {
            return _source;
        }

        /**
         *
         * @param _source Source
         */
        public void setSource(final Source _source) {
            this._source = _source;
        }
    }

    public static class Source {
        @JsonProperty
        private Integer imageIndex;

        @JsonProperty
        private String iiifService;

        @JsonProperty
        private Map<String, String> metadata;

        @JsonProperty
        private Map<Integer, Structure> structureMap;

        /**
         * @return Integer
         */
        public Integer getImageIndex() {
            return imageIndex;
        }

        /**
         * @param imageIndex Integer
         */
        public void setImageIndex(final Integer imageIndex) {
            this.imageIndex = imageIndex;
        }

        /**
         * @return String
         */
        public String getIiifService() {
            return iiifService;
        }

        /**
         * @param iiifService String
         */
        public void setIiifService(final String iiifService) {
            this.iiifService = iiifService;
        }

        /**
         * @return MetadataMap
         */
        public Map<String, String> getMetadata() {
            return metadata;
        }

        /**
         * @param metadata metadata
         */
        public void setMetadata(final Map<String, String> metadata) {
            this.metadata = metadata;
        }

        /**
         * @return MetadataMap
         */
        public Map<Integer, Structure> getStructureMap() {
            return structureMap;
        }

        /**
         * @param structureMap structureMap
         */
        public void setStructureMap(final Map<Integer, Structure> structureMap) {
            this.structureMap = structureMap;
        }
    }
}
