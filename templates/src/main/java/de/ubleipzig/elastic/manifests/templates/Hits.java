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

public class Hits {

    @JsonProperty
    private String _index;

    @JsonProperty
    private String _type;

    @JsonProperty
    private String _id;

    @JsonProperty
    private Integer _score;

    @JsonProperty
    private Source _source;

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
