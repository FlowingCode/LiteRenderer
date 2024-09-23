/*-
 * #%L
 * Lite Renderer
 * %%
 * Copyright (C) 2024 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.vaadin.addons.litetemplate;

import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.JsonArray;
import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class LiteListener<SOURCE> implements Serializable {

  private final SerializableBiConsumer<SOURCE, JsonArray> handler;
  private final String arguments;

  LiteListener(SerializableConsumer<SOURCE> handler) {
    this((item, ignore) -> handler.accept(item), (String) null);
  }

  LiteListener(SerializableBiConsumer<SOURCE, JsonArray> handler, String[] arguments) {
    this(handler, Stream.of(arguments).collect(Collectors.joining(", ")));
  }

  boolean hasArguments() {
    return arguments != null;
  }

}
