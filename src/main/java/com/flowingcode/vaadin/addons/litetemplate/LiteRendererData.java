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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.function.ValueProvider;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("serial")
class LiteRendererData<SOURCE> implements Serializable {

  private Map<String, LiteListener<SOURCE>> listeners = Collections.emptyMap();
  private Map<String, ValueProvider<SOURCE, ?>> attributes = Collections.emptyMap();
  private Map<String, ValueProvider<SOURCE, ?>> properties = Collections.emptyMap();
  private Boolean interpolationEnabled;

  @SuppressWarnings("unchecked")
  static <SOURCE> LiteRendererData<SOURCE> getInstance(Component c) {
    var data = ComponentUtil.getData(c, LiteRendererData.class);
    if (data == null) {
      data = new LiteRendererData<>();
      ComponentUtil.setData(c, LiteRendererData.class, data);
    }
    return data;
  }

  Optional<Boolean> isInterpolationEnabled() {
    return Optional.ofNullable(interpolationEnabled);
  }

  void setInterpolationEnabled(boolean interpolationEnabled) {
    this.interpolationEnabled = interpolationEnabled;
  }

  void addProperty(String name, ValueProvider<SOURCE, ?> valueProvider) {
    properties = put(properties, name, valueProvider);
  }

  void removeProperty(String name) {
    properties = remove(properties, name);
  }

  Map<String, ValueProvider<SOURCE, ?>> getProperties() {
    return Collections.unmodifiableMap(properties);
  }

  void addAttribute(String attribute, ValueProvider<SOURCE, ?> valueProvider) {
    attributes = put(attributes, attribute, valueProvider);
  }

  void removeAttribute(String name) {
    attributes = remove(attributes, name);
  }

  Map<String, ValueProvider<SOURCE, ?>> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  void addListener(String eventType, LiteListener<SOURCE> listener) {
    if (hasListener(eventType)) {
      throw new IllegalArgumentException(String.format(
          "A listener for the event type '%s' is already registered on the specified component.",
          eventType));
    }
    listeners = put(listeners, eventType, listener);
  }

  Map<String, LiteListener<SOURCE>> getListeners() {
    return Collections.unmodifiableMap(listeners);
  }

  boolean hasListener(String eventType) {
    return listeners.containsKey(eventType);
  }

  private static <K, V> Map<K, V> put(Map<K, V> map, K k, V v) {
    return switch (map.size()) {
      case 0 -> Map.of(k, v);
      case 1 -> {
        map = new HashMap<>(map);
        map.put(k, v);
        yield map;
      }
      default -> {
        map.put(k, v);
        yield map;
      }
    };
  }

  private static <K, V> Map<K, V> remove(Map<K, V> map, K k) {
    return !map.containsKey(k) ? map : switch (map.size()) {
      case 1 -> Collections.emptyMap();
      default -> {
        map.remove(k);
        yield map;
      }
    };
  }

}
