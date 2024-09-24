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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.JsonArray;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * {@code Renderer} that uses a component instance as template. Intended as an alternative of
 * {@link LitRenderer}.
 *
 * @author Javier Godoy
 *
 * @param <SOURCE> the type of the model object used in the template
 *
 * @see #of(Component, Component...)
 */
@SuppressWarnings("serial")
public class LiteRenderer<SOURCE> extends Renderer<SOURCE> {

  private final List<Component> components;

  @Setter(AccessLevel.PRIVATE)
  private String templateExpression;

  private LitRenderer<SOURCE> renderer;

  private final Map<String, ValueProvider<SOURCE, ?>> properties = new HashMap<>();

  private LiteRenderer(List<Component> components) {
    this.components = components;
  }

  private void updateRenderer() {
    renderer = LitRendererBuilder.build(components,
        Collections.unmodifiableMap(properties),
        this::setTemplateExpression);
  }

  /** Returns a string representation of the object. */
  @Override
  public String toString() {
    return Optional.ofNullable(templateExpression).orElseGet(() -> {
      try {
        updateRenderer();
        return templateExpression;
      } finally {
        templateExpression = null;
        renderer = null;
      }
    });
  }

  @Override
  public Rendering<SOURCE> render(Element container, DataKeyMapper<SOURCE> keyMapper,
      String rendererName) {
    if (renderer == null) {
      updateRenderer();
    }
    return renderer.render(container, keyMapper, rendererName);
  }

  /**
   * Creates a new {@code LiteRenderer} using the specified template components. The renderer will
   * automatically reflect any updates made to the components, up until the first render.
   * <p>
   * <code>${placeholder}</code> expressions within attribute and property values will be
   * interpolated, allowing them to dynamically reflect the same properties accessible to
   * {@link LitRenderer}. Additionally, dynamic
   * {@linkplain LiteComponent#withAttribute(String, ValueProvider) attributes} and
   * {@linkplain LiteComponent#withElementProperty(String, ValueProvider) properties} will be
   * replaced with generated placeholders.
   *
   * Example:
   *
   * <pre>
   * {@code
   * LiteRenderer.<Person>of(new Div("Name: ${item.name}")).withProperty("name", Person::getName);
   * }
   * </pre>
   *
   * @param <SOURCE> the type of the input object used inside the template
   *
   * @param templateComponent the first template components used to render items. Must not be
   *        {@code null}.
   *
   * @param moreTemplateComponents additional template components used to render items. Must not be
   *        {@code null}.
   *
   * @throws NullPointerException if any template component is {@code null}
   *
   * @return a {@code LiteRenderer} configured with the given template component.
   *
   * @see LiteRenderer#withProperty(String, ValueProvider)
   * @see LiteRenderer#withProperty(Component, String, ValueProvider)
   * @see LiteRenderer#withListener(Component, String, SerializableConsumer)
   */
  public static <SOURCE> LiteRenderer<SOURCE> of(Component templateComponent, Component... moreTemplateComponents) {
    List<Component> templateComponents;

    if (moreTemplateComponents == null || moreTemplateComponents.length == 0) {
      templateComponents = List.of(templateComponent);
    } else {
      templateComponents = new ArrayList<>(1+moreTemplateComponents.length);
      templateComponents.add(templateComponent);
      templateComponents.addAll(Arrays.asList(moreTemplateComponents));
    }

    if (Stream.of(templateComponents).anyMatch(Objects::isNull)) {
      throw new NullPointerException();
    }

    return new LiteRenderer<>(templateComponents);
  }

  /**
   * Disable interpolation of template properties within the attributes and properties of the
   * template components and their children.
   * <p>
   * To enable interpolation on a particular child, use
   * {@link LiteRenderer#enableInterpolation(Component)} on that child.
   *
   * @return this instance for method chaining
   * @see LiteComponent#disableInterpolation()
   * @see LiteRenderer#disableInterpolation(Component)
   */
  public LiteRenderer<SOURCE> disableInterpolation() {
    components.forEach(component -> LiteComponent.of(component).disableInterpolation());
    return this;
  }

  /**
   * Disable interpolation of template properties within the attributes and properties of the
   * specified component and its children.
   * <p>
   * To enable interpolation on a particular child, use {@link #enableInterpolation(Component)} on
   * that child.
   *
   * @see LiteComponent#disableInterpolation()
   */
  public LiteRenderer<SOURCE> disableInterpolation(Component component) {
    LiteComponent.of(component).disableInterpolation();
    return this;
  }

  /**
   * Enable interpolation of template properties within the attributes and properties of the
   * specified component and its children.
   * <p>
   * To enable interpolation on a particular child, use {@link #disableInterpolation(Component)} on
   * that child.
   *
   * @see LiteComponent#enableInterpolation()
   */
  public LiteRenderer<SOURCE> enableInterpolation(Component component) {
    LiteComponent.of(component).enableInterpolation();
    return this;
  }

  private void assertTemplateContainsComponent(Component c) {
    if (components.stream().noneMatch(component -> find(component, c))) {
      throw new IllegalArgumentException("Component is not a children of the template");
    }
  }

  private static boolean find(Component c, Component target) {
    return c == target || c.getChildren().anyMatch(child -> find(child, target));
  }

  /**
   * Makes a Lit property available to the template component. Each property is referenced inside
   * attributes or properties of the template by using the {@code ${item.property}} syntax.
   * <p>
   * Example:
   *
   * <pre>
   * {@code
   * LiteRenderer.<Person>of(new Div("Name: ${item.name}"))
   *             .withProperty("name", Person::getName);
   * }
   * </pre>
   *
   * Any types supported by {@link LitRenderer} are valid types for {@code LiteRenderer}.
   *
   * @param property the name of the property used inside the template expression. Must not be
   *        {@code null}.
   *
   * @param provider a {@link ValueProvider} that provides the actual value for the property. Must
   *        not be {@code null}.
   *
   * @return this instance for method chaining
   * @see LitRenderer#withProperty(String, ValueProvider)
   */
  public LiteRenderer<SOURCE> withProperty(
      @NonNull String property,
      @NonNull ValueProvider<SOURCE, ?> provider) {
    properties.put(property, provider);
    if (renderer != null) {
      renderer.withProperty(property, provider);
    }
    return this;
  }

  /**
   * Binds a dynamic property with a specified component inside the template. The value of the
   * property is provided by a {@code ValueProvider} and will be updated based on the input object.
   * <p>
   * This method invalidates any previous renderer to reflect the new property.
   *
   * @param component the component within the template to which the property is being bound. Must
   *        not be {@code null}.
   * @param name the name of the property being bound. Must not be {@code null}.
   * @param valueProvider a {@code ValueProvider} that supplies the value of the property from the
   *        input object. Must not be {@code null}.
   * @throws IllegalArgumentException if the provided component is not part of the template.
   * @return this instance for method chaining
   * @see LiteComponent#withElementProperty(String, ValueProvider)
   */
  public LiteRenderer<SOURCE> withProperty(@NonNull Component component,
      @NonNull String name,
      @NonNull ValueProvider<SOURCE, ?> valueProvider) {
    assertTemplateContainsComponent(component);
    LiteComponent.of(component).withElementProperty(name, valueProvider);
    renderer = null;
    return this;
  }

  /**
   * Binds a dynamic attribute to a specified component inside the template.. The value of the
   * property is provided by a {@code ValueProvider} and will be updated based on the input object.
   * <p>
   * This method invalidates any previous renderer to reflect the new attribute.
   *
   * @param component the component within the template to which the attribute is being bound. Must
   *        not be {@code null}.
   * @param attribute the name of the attribute being bound. Must not be {@code null}.
   * @param valueProvider a {@code ValueProvider} that supplies the value of the attribute from the
   *        input object. Must not be {@code null}.
   * @throws IllegalArgumentException if the provided component is not part of the template.
   * @return this instance for method chaining
   * @see LiteComponent#withAttribute(String, ValueProvider)
   */
  public LiteRenderer<SOURCE> withAttribute(@NonNull Component component,
      @NonNull String attribute, @NonNull ValueProvider<SOURCE, ?> valueProvider) {
    assertTemplateContainsComponent(component);
    LiteComponent.of(component).withAttribute(attribute, valueProvider);
    renderer = null;
    return this;
  }

  /**
   * Assigns a static attribute to a specified component in the template. This attribute will have a
   * fixed value and will not dynamically update based on the input object, but it can contain
   * <code>${placeholder}</code> expressions allowing dynamic content to be inserted when rendered.
   *
   * Example:
   *
   * <pre>{@code
   * Div div = new Div("${item.firstName} ${item.lastName}");
   * grid.addColumn(LiteRenderer.<Person>of(div)
   *    .withProperty("firstName", Person::firstName)
   *    .withProperty("lastName", Person::lastName)
   *    .withProperty("age", Person::age)
   *    .withAttribute(div, "title", "Age: ${item.age}")
   * }</pre>
   *
   * @param component the component within the template to which the attribute is being assigned.
   *        Must not be {@code null}.
   * @param attribute the name of the attribute being assigned. Must not be {@code null}.
   * @param value the static value of the attribute. Must not be {@code null}.
   * @throws IllegalArgumentException if the provided component is not part of the template.
   * @return this instance for method chaining
   * @see LiteComponent#withAttribute(String, ValueProvider)
   */
  public LiteRenderer<SOURCE> withAttribute(@NonNull Component component,
      @NonNull String attribute, String value) {
    assertTemplateContainsComponent(component);
    LiteComponent.of(component).withAttribute(attribute, value);
    renderer = null;
    return this;
  }

  /**
   * Registers an event listener for a DOM event on one of the child elements within the template
   * component. Each DOM event can only be registered once per component. The function accepts
   * arguments that can be consumed by the given handler.
   *
   * <p>
   * Examples:
   *
   * <pre>
   * {@code
   * LiteRenderer.of(new TextField())
   *             .withListener(child, "keypress", , (item, args) -> {
   *       System.out.println("Pressed key: " + args.getString(0));
   *     }, "e.key");
   * }
   * </pre>
   *
   * @param component The component whose child element will receive the event listener.
   * @param eventType The type of DOM event to listen for (e.g., "click", "change").
   * @param handler A callback function to handle the event when triggered
   * @param eventArguments One or more expressions for extracting event data. When an event is fired
   *        in the browser, the expression is evaluated and its value is sentback to the server. The
   *        expression is evaluated in a context where element refers to this element and event
   *        refersto the fired event. If multiple expressions are defined for the sameevent, their
   *        order of execution is undefined.
   *
   * @return this instance for method chaining
   * @see #withListener(Component, String, SerializableConsumer)
   * @see LiteComponent#withListener(String, SerializableBiConsumer, String...)
   */
  public LiteRenderer<SOURCE> withListener(
      @NonNull Component component,
      @NonNull String eventType,
      @NonNull SerializableBiConsumer<SOURCE, JsonArray> handler,
      String... eventArguments) {
    assertTemplateContainsComponent(component);
    LiteComponent.of(component).withListener(eventType, handler, eventArguments);
    renderer = null;
    return this;
  }

  /**
   * Registers an event listener for a DOM event on one of the child elements within the template
   * component. Each DOM event can only be registered once per component.
   *
   * @param component The component whose child element will receive the event listener.
   * @param eventType The type of DOM event to listen for (e.g., "click", "change").
   * @param handler A callback function to handle the event when triggered
   *
   * @return this instance for method chaining
   * @see #withListener(Component, String, SerializableBiConsumer, String...)
   * @see LiteComponent#withListener(String, SerializableConsumer)
   */
  public LiteRenderer<SOURCE> withListener(
      @NonNull Component component,
      @NonNull String eventType,
      @NonNull SerializableConsumer<SOURCE> handler) {
    assertTemplateContainsComponent(component);
    LiteComponent.of(component).withListener(eventType, handler);
    renderer = null;
    return this;
  }

  static void fireEvent(Object item, ComponentEvent<?> event) {
    setItem(event, item);
    try {
      ComponentUtil.fireEvent(event.getSource(), event);
    } finally {
      setItem(event, null);
    }
  }

  @RequiredArgsConstructor
  private static class LiteRendererItemReference implements Serializable {
    private final Object item;
  }

  private static void setItem(ComponentEvent<?> event, Object item) {
    var ref = Optional.ofNullable(item).map(LiteRendererItemReference::new).orElse(null);
    ComponentUtil.setData(event.getSource(), LiteRendererItemReference.class, ref);
  }

  /**
   * Returns the item of an event fired on the template.
   *
   * @param event the event fired on the template component.
   * @param type the type of the item.
   * @throws ClassCastException if the captured item is not assignable to {@code type}.
   * @throws IllegalStateException if no item was captured.
   *
   * @return the item of an event fired on the template
   */
  public static <T> T getItem(ComponentEvent<?> event, Class<T> type) {
    var ref = ComponentUtil.getData(event.getSource(), LiteRendererItemReference.class);
    if (ref == null) {
      throw new IllegalStateException();
    }
    return type.cast(ref.item);
  }

}
