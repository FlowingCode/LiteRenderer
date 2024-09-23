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
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.JsonArray;
import elemental.json.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

/**
 * Wrapper around a {@code Component} that provides fluent methods for managing attributes,
 * properties, and event listeners. It is designed for use in conjunction with the
 * {@link LiteRenderer} and cannot be used as a normal UI component.
 *
 * @author Javier Godoy
 */
@SuppressWarnings("serial")
public final class LiteComponent<COMPONENT extends Component> extends Component {

  // The wrapped component that is being managed.
  private final COMPONENT component;

  private LiteComponent(@NonNull COMPONENT component) {
    super(null);
    this.component = component;
  }

  /**
   * Disable interpolation of template properties within the attributes and properties of this
   * component and its children.
   * <p>
   * To enable interpolation on a particular child, use {@link #enableInterpolation()} on that
   * child.
   *
   * @see LiteRenderer#disableInterpolation()
   * @see LiteRenderer#disableInterpolation(Component)
   */
  public void disableInterpolation() {
    LiteRendererData.getInstance(component).setInterpolationEnabled(false);
  }

  /**
   * Enable interpolation of template properties within the attributes and properties of this
   * component and its children.
   * <p>
   * To enable interpolation on a particular child, use {@link #disableInterpolation()} on that
   * child.
   *
   * @see LiteRenderer#enableInterpolation(Component)
   */
  public void enableInterpolation() {
    LiteRendererData.getInstance(component).setInterpolationEnabled(true);
  }

  /**
   * Returns an instance of {@code LiteComponent} for the given component.
   */
  @SuppressWarnings("unchecked")
  public static <C extends Component> LiteComponent<C> of(C component) {
    return component instanceof LiteComponent
        ? (LiteComponent<C>) component
        : new LiteComponent<>(component);
  }

  /**
   * Unsupported operation.
   *
   * @throws UnsupportedOperationException, as {@LiteComponent} can only be used with
   *         {@link LiteRenderer}.
   */
  @Override
  public Element getElement() {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported operation.
   *
   * @throws UnsupportedOperationException, as {@LiteComponent} can only be used with
   *         {@link LiteRenderer}.
   */
  @Override
  protected ComponentEventBus getEventBus() {
    throw new UnsupportedOperationException();
  }

  /** Returns the wrapped component. */
  public COMPONENT unwrap() {
    return component;
  }

  /**
   * Sets a boolean attribute on the component.
   *
   * @return this instance for method chaining
   * @see Element#setAttribute(String, boolean)
   */
  public <SOURCE> LiteComponent<COMPONENT> withAttribute(String attribute, boolean value) {
    component.getElement().setAttribute(attribute, value);
    LiteRendererData.getInstance(component).removeAttribute(attribute);
    return this;
  }

  /**
   * Sets a string attribute on the component.
   *
   * @return this instance for method chaining
   * @see Element#setAttribute(String, String)
   */
  public <SOURCE> LiteComponent<COMPONENT> withAttribute(String attribute, String value) {
    component.getElement().setAttribute(attribute, value);
    LiteRendererData.getInstance(component).removeAttribute(attribute);
    return this;
  }

  /**
   * Sets a dynamic attribute on the component. The value of the property is provided by a
   * {@code ValueProvider} and will be updated based on the input object.
   *
   * @param attribute the name of the attribute being bound. Must not be {@code null}.
   * @param valueProvider a {@code ValueProvider} that supplies the value of the attribute from the
   *        input object. Must not be {@code null}.
   * @return this instance for method chaining
   * @see LiteRenderer#withAttribute(Component, String, ValueProvider)
   */
  public <SOURCE> LiteComponent<COMPONENT> withAttribute(String attribute,
      ValueProvider<SOURCE, ?> valueProvider) {
    LiteRendererData.<SOURCE>getInstance(component).addAttribute(attribute, valueProvider);
    component.getElement().removeAttribute(attribute);
    return this;
  }

  /**
   * Sets an element property to the given boolean value.
   *
   * @return this instance for method chaining
   * @see Element#setProperty(String, boolean)
   */
  public <SOURCE> LiteComponent<COMPONENT> withElementProperty(String name, boolean value) {
    component.getElement().setProperty(name, value);
    LiteRendererData.getInstance(component).removeProperty(name);
    return this;
  }

  /**
   * Sets an element property to the given numeric value.
   *
   * @return this instance for method chaining
   * @see Element#setProperty(String, double)
   */
  public <SOURCE> LiteComponent<COMPONENT> withElementProperty(String name, double value) {
    component.getElement().setProperty(name, value);
    LiteRendererData.getInstance(component).removeProperty(name);
    return this;
  }

  /**
   * Sets an element property to the given string value.
   *
   * @return this instance for method chaining
   * @see Element#setProperty(String, String)
   */
  public <SOURCE> LiteComponent<COMPONENT> withElementProperty(String name, String value) {
    component.getElement().setProperty(name, value);
    LiteRendererData.getInstance(component).removeProperty(name);
    return this;
  }

  /**
   * Sets an element property to the given bean, converted to a JSON object.
   *
   * @return this instance for method chaining
   * @see Element#setPropertyBean(String, Object)
   */
  public <SOURCE> LiteComponent<COMPONENT> withElementPropertyBean(String name, Object value) {
    component.getElement().setPropertyBean(name, value);
    LiteRendererData.getInstance(component).removeProperty(name);
    return this;
  }

  /**
   * Sets an element property to the given JSON value.
   *
   * @return this instance for method chaining
   * @see Element#setPropertyBean(String, Object)
   */
  public <SOURCE> LiteComponent<COMPONENT> withElementPropertyJson(String name, JsonValue value) {
    component.getElement().setPropertyJson(name, value);
    LiteRendererData.getInstance(component).removeProperty(name);
    return this;
  }

  /**
   * Sets an element property to the given list of beans or primitive values, converted to a JSON
   * array.
   *
   * @return this instance for method chaining
   * @see Element#setPropertyList(String, List)
   */
  public <SOURCE> LiteComponent<COMPONENT> withElementPropertyList(String name, List<?> value) {
    component.getElement().setPropertyList(name, value);
    LiteRendererData.getInstance(component).removeProperty(name);
    return this;
  }

  /**
   * Sets an element property to the given map of beans or primitive values, converted to a JSON
   * array.
   *
   * @return this instance for method chaining
   * @see Element#setPropertyMap(String, Map)
   */
  public <SOURCE> LiteComponent<COMPONENT> withElementPropertyMap(String name,
      Map<String, ?> value) {
    component.getElement().setPropertyMap(name, value);
    LiteRendererData.getInstance(component).removeProperty(name);
    return this;
  }

  /**
   * Sets a dynamic element property. The value of the property is provided by a
   * {@code ValueProvider} and will be updated based on the input object.
   * <p>
   *
   * @param name the name of the property being bound. Must not be {@code null}.
   * @param valueProvider a {@code ValueProvider} that supplies the value of the property from the
   *        input object. Must not be {@code null}.
   * @return this instance for method chaining
   * @see LiteRenderer#withProperty(Component, String, ValueProvider)
   */
  public <SOURCE> LiteComponent<COMPONENT> withElementProperty(String name,
      ValueProvider<SOURCE, ?> valueProvider) {
    LiteRendererData.<SOURCE>getInstance(component).addProperty(name, valueProvider);
    component.getElement().removeProperty(name);
    return this;
  }

  /**
   * Registers an event listener for a DOM event on one of the child elements within the template
   * component. Each DOM event can only be registered once per component.
   *
   * @param eventType The type of DOM event to listen for (e.g., "click", "change").
   * @param handler A callback function to handle the event when triggered
   *
   * @return this instance for method chaining
   * @see #withListener(String, SerializableBiConsumer, String...)
   * @see LiteRenderer#withListener(Component, String, SerializableConsumer)
   */
  public <SOURCE> LiteComponent<COMPONENT> withListener(String eventType,
      SerializableConsumer<SOURCE> handler) {
    LiteRendererData.<SOURCE>getInstance(component)
        .addListener(eventType, new LiteListener<>(handler));
    return this;
  }

  /**
   * Registers an event listener for a DOM event on one of the child elements within the template
   * component. Each DOM event can only be registered once per component. The function accepts
   * arguments that can be consumed by the given handler.
   *
   * <p>
   * Example:
   *
   * <pre>
   * {@code
   * LiteRenderer.of(new TextField())
   *             .withListener(child, "keypress", (item, args) -> {
   *       System.out.println("Pressed key: " + args.getString(0));
   *     }, "e.key");
   * }
   * </pre>
   *
   * @param eventType The type of DOM event to listen for (e.g., "click", "change").
   * @param handler A callback function to handle the event when triggered
   * @param eventArguments One or more expressions for extracting event data. When an event is fired
   *        in the browser, the expression is evaluated and its value is sentback to the server. The
   *        expression is evaluated in a context where element refers to this element and event
   *        refersto the fired event. If multiple expressions are defined for the sameevent, their
   *        order of execution is undefined.
   *
   * @return this instance for method chaining
   * @see #withListener(String, SerializableConsumer)
   * @see LiteRenderer#withListener(Component, String, SerializableBiConsumer, String...)
   */
  public <SOURCE> LiteComponent<COMPONENT> withListener(String eventType,
      SerializableBiConsumer<SOURCE, JsonArray> handler, String... eventArguments) {
    eventArguments = Arrays.copyOf(eventArguments, eventArguments.length);
    LiteRendererData.<SOURCE>getInstance(component)
        .addListener(eventType, new LiteListener<>(handler, eventArguments));
    return this;
  }

}
