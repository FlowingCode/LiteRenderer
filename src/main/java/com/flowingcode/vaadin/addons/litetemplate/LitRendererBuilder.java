/*-
 * #%L
 * Lite Renderer Add-On
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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.JsonArray;
import elemental.json.JsonBoolean;
import elemental.json.JsonNumber;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.NonNull;

final class LitRendererBuilder<SOURCE> {

  public static final String RENDERER_KEY = "renderer";

  private final List<Component> components;

  private final StringBuilder sb = new StringBuilder();
  private final Map<String, ValueProvider<SOURCE, ?>> properties = new HashMap<>();
  private final Map<String, SerializableBiConsumer<SOURCE, JsonArray>> functions = new HashMap<>();


  private LitRendererBuilder(List<Component> components) {
    this.components = components;
  }

  public static <SOURCE> LitRenderer<SOURCE> build(
      @NonNull List<Component> components,
      @NonNull Map<String, ValueProvider<SOURCE, ?>> properties,
      @NonNull Consumer<String> setTemplateExpression) {
    return new LitRendererBuilder<SOURCE>(components).build(properties, setTemplateExpression);
  }

  private LiteRendererData<SOURCE> getData(Component component) {
    return LiteRendererData.getInstance(component);
  }

  private LitRenderer<SOURCE> build(
      Map<String, ValueProvider<SOURCE, ?>> rendererProperties,
      Consumer<String> setTemplateExpression) {

    if (!sb.isEmpty()) {
      throw new IllegalStateException();
    }

    properties.putAll(rendererProperties);

    for (Component component : components) {
      Element element;
      if (component instanceof LiteComponent<?> lite) {
        element = lite.unwrap().getElement();
      } else {
        element = component.getElement();
      }

      writeElement(element, true, "\n  ");
    }

    String templateExpression = sb.toString() + "\n";
    var renderer = LitRenderer.<SOURCE>of(templateExpression);
    functions.forEach((n, v) -> renderer.withFunction(n, v));
    properties.forEach((n, v) -> renderer.withProperty(n, v));

    setTemplateExpression.accept(templateExpression);
    return renderer;
  }

  private String addFunction(SerializableBiConsumer<SOURCE, JsonArray> handler) {
    String name = "function" + functions.size();
    functions.put(name, handler);
    return name;
  }

  private String addProperty(ValueProvider<SOURCE, ?> valueProvider) {
    String name = "property" + properties.size();
    properties.put(name, valueProvider);
    return name;
  }


  private void writeElement(Element e, boolean interpolationEnabled, String padding) {
    if (e.isTextNode()) {
      sb.append(padding);
      String text = e.getText();
      if (interpolationEnabled || !text.contains("${") && !text.contains("`")) {
        sb.append(encodeReplaceableCharacterData(text));
      } else {
        sb.append(wrapAndEscapeTemplateCharacters(text));
      }
    } else {
      var data = getData(e.getComponent().get());
      if (data.isInterpolationEnabled().filter(value -> value != interpolationEnabled)
          .isPresent()) {
        writeElement(e, !interpolationEnabled, padding);
        return;
      }

      sb.append(padding);
      sb.append('<').append(e.getTag());

      data.getAttributes().forEach((name, valueProvider) -> {
        e.removeAttribute(name);
        writeAttribute(name, valueProvider);
      });

      e.getAttributeNames().forEach(name -> writeAttribute(e, name, interpolationEnabled));

      data.getProperties().forEach((name, valueProvider) -> {
        e.removeProperty(name);
        writeProperty(name, valueProvider);
      });
      e.getPropertyNames().forEach(name -> writeProperty(e, name, interpolationEnabled));

      writeEventListeners(e);

      sb.append('>');

      if (e.getChildCount() != 0) {
        String nested = padding + "  ";
        for (int i = 0, n = e.getChildCount(); i < n; i++) {
          writeElement(e.getChild(i), interpolationEnabled, nested);
        }
        sb.append(padding);
      }
      sb.append("</").append(e.getTag()).append('>');
    }
  }

  private void writeAttribute(String name, ValueProvider<SOURCE, ?> valueProvider) {
    sb.append('\s').append(name);
    sb.append("=${item.").append(addProperty(valueProvider)).append('}');
  }

  private void writeAttribute(Element element, String name, boolean interpolationEnabled) {
    sb.append('\s').append(name);
    String value = element.getAttribute(name);
    if (!value.isEmpty()) {
      sb.append('=');
      if (interpolationEnabled) {
        sb.append(encodeAttributeValueSpecification(value));
      } else {
        sb.append(wrapAndEscapeTemplateCharacters(value));
      }
    }
  }

  private void writeProperty(Element element, String name, boolean interpolationEnabled) {

    boolean wrap = true;
    Object value = element.getPropertyRaw(name);
    if (value instanceof String val) {
      if (interpolationEnabled) {
        value = encodePropertyValue(val, "${`", "`}");
      } else {
        value = wrapAndEscapeTemplateCharacters(val);
      }
      wrap = false;
    } else if (value instanceof JsonString val) {
      value = wrapAndEscapeTemplateCharacters(val.asString());
      wrap = false;
    } else if (value instanceof Double || value instanceof Boolean) {
      value = String.valueOf(value);
    } else if (value instanceof JsonBoolean val) {
      value = Boolean.toString(val.asBoolean());
    } else if (value instanceof JsonNumber val) {
      value = Double.toString(val.asNumber());
    } else if (value instanceof JsonValue val) {
      value = val.toJson();
    } else {
      throw new IllegalArgumentException();
    }

    sb.append(" .").append(name).append('=');
    if (wrap) {
      sb.append("${").append(value).append('}');
    } else {
      sb.append(value);
    }
  }

  private void writeProperty(String name, ValueProvider<SOURCE, ?> valueProvider) {
    sb.append(" .").append(name);
    sb.append("=${item.").append(addProperty(valueProvider)).append('}');
  }

  private void writeEventListeners(Element e) {

    Component c = e.getComponent().get();

    if (c instanceof Button && !getData(c).hasListener("click")) {
      getData(c).addListener("click",
          new LiteListener<>(item -> LiteRenderer.fireEvent(item, new ClickEvent<>(c))));
    }

    getData(c).getListeners().forEach((eventType, listener) -> {
      var handler = listener.getHandler();
      String functionName = addFunction((item, args) -> handler.accept(item, args));

      String actualArguments;
      String formalArguments;
      if (listener.hasArguments()) {
        actualArguments = listener.getArguments();
        formalArguments = "event";
      } else {
        actualArguments = "";
        formalArguments = "";
      }

      sb.append(String.format(" @%s=${(%s)=>%s(%s)}",
          eventType,
          formalArguments,
          functionName,
          actualArguments));
    });
  }

  private static String encodeAttributeValueSpecification(String value) {
    if (value.contains("${")) {
      return encodePropertyValue(value, "\"", "\"");
    }
    value = value.replace("&", "&amp;");
    if (value.indexOf('"') >= 0 && value.indexOf('\'') < 0) {
      return '\'' + value + '\'';
    } else {
      return '"' + value.replace("\"", "&quot;") + '"';
    }
  }

  private static String encodeReplaceableCharacterData(String value) {
    if (value.startsWith("\\${")) {
      value = value.substring(1);
    } else if (value.startsWith("${")) {
      return value;
    }
    return value.replace("&", "&amp;").replace("<", "&lt;");
  }

  private static String wrapAndEscapeTemplateCharacters(String value) {
    return "${`" + value.replace("`", "\\`").replace("$", "\\$") + "`}";
  }


  private static String encodePropertyValue(String value, String prefix, String suffix) {
    return PropertyEncoder.encode(value, prefix, suffix)
        .orElseGet(() -> wrapAndEscapeTemplateCharacters(value));
  }

}
