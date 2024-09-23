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

package com.flowingcode.vaadin.addons.litetemplate.test;

import static org.hamcrest.MatcherAssert.assertThat;
import com.flowingcode.vaadin.addons.litetemplate.LiteComponent;
import com.flowingcode.vaadin.addons.litetemplate.LiteRenderer;
import com.flowingcode.vaadin.addons.litetemplate.Person;
import com.vaadin.flow.component.html.Div;
import elemental.json.Json;
import elemental.json.JsonValue;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

public class LiteRendererTest  {

  private static BaseMatcher<LiteRenderer<?>> rendersTo(String expectedValue) {
    return new BaseMatcher<>() {

      @Override
      public void describeTo(Description description) {
        description.appendValue(expectedValue);
      }

      @Override
      public boolean matches(Object item) {
        String value = ((LiteRenderer<?>) item).toString();
        return canonical(expectedValue).equals(canonical(value));
      }

      private String canonical(String s) {
        return s.replaceAll("[\r\n\s\t]+", " ")
                .replaceAll(">\\s+", ">")
                .replaceAll("\\s+<", "<")
                .trim();
      }
    };
  }

  private LiteRenderer<?> renderWithTitle(String title) {
    Div div = new Div();
    return LiteRenderer.of(div).withAttribute(div, "title", title);
  }

  private LiteRenderer<?> renderWithBody(String body) {
    Div div = new Div(body);
    return LiteRenderer.of(div);
  }

  private LiteRenderer<?> render(Consumer<LiteComponent<?>> consumer) {
    LiteComponent<Div> lite = LiteComponent.of(new Div());
    consumer.accept(lite);
    return LiteRenderer.of(lite);
  }

  @Test
  public void testBodyInterpolation() {
    assertThat(renderWithBody("${item.foo} ${item.bar}"), rendersTo("""
        <div>${item.foo} ${item.bar}</div>
        """));
  }

  @Test
  public void testBodyInterpolationDisabled() {
    assertThat(renderWithBody("${item.foo} ${item.bar}").disableInterpolation(), rendersTo("""
        <div>${`\\${item.foo} \\${item.bar}`}</div>
        """));
  }

  @Test
  public void testBodyLiteralInterpolationDisabled() {
    assertThat(renderWithBody("foo").disableInterpolation(), rendersTo("""
        <div>foo</div>
        """));
  }

  @Test
  public void testBodyLiteralAmp() {
    assertThat(renderWithBody("&amp;"), rendersTo("""
        <div>&amp;amp;</div>
        """));
  }


  @Test
  public void testBodyLiteralStago() {
    assertThat(renderWithBody("<br>"), rendersTo("""
        <div>&lt;br></div>
        """));
  }

  @Test
  public void testAttributeInterpolationDisabled() {
    assertThat(renderWithTitle("${item.bar}").disableInterpolation(), rendersTo("""
        <div title=${`\\${item.bar}`}></div>
        """));
  }


  @Test
  public void testAttributeInterpolationOne() {
    assertThat(renderWithTitle("${item.bar}"), rendersTo("""
        <div title=${item.bar}></div>
        """));
  }

  @Test
  public void testAttributeInterpolationWithPrefix() {
    assertThat(renderWithTitle("foo ${item.bar}"), rendersTo("""
        <div title="foo ${item.bar}"></div>
        """));
  }

  @Test
  public void testAttributeInterpolationTwo() {
    assertThat(renderWithTitle("${item.foo} ${item.bar}"), rendersTo("""
        <div title="${item.foo} ${item.bar}"></div>
        """));
  }

  @Test
  public void testAttributeLiteralAmp() {
    assertThat(renderWithTitle("&amp;"), rendersTo("""
        <div title="&amp;amp;"></div>
        """));
  }

  @Test
  public void testAttributeLiteralApos() {
    assertThat(renderWithTitle("'"), rendersTo("""
        <div title="'"></div>
        """));
  }

  @Test
  public void testAttributeLiteralQuot() {
    assertThat(renderWithTitle("\""), rendersTo("""
        <div title='"'></div>
        """));
  }

  @Test
  public void testAttributeBooleanTrue() {
    assertThat(render(lite -> lite.withAttribute("hidden", true)), rendersTo("""
        <div hidden></div>
        """));
  }

  @Test
  public void testAttributeBooleanFalse() {
    assertThat(render(lite -> lite.withAttribute("hidden", false)), rendersTo("""
        <div></div>
        """));
  }

  @Test
  public void testPropertyBoolean() {
    assertThat(render(lite -> lite.withElementProperty("foo", true)), rendersTo("""
        <div .foo=${true}></div>
        """));
  }

  @Test
  public void testPropertyDouble() {
    assertThat(render(lite -> lite.withElementProperty("foo", 1.0)), rendersTo("""
        <div .foo=${1.0}></div>
        """));
  }

  @Test
  public void testPropertyString() {
    assertThat(render(lite -> lite.withElementProperty("foo", "bar")), rendersTo("""
        <div .foo=${`bar`}></div>
        """));
  }

  @Test
  public void testPropertyValueProvider() {
    assertThat(
        render(lite -> lite.withElementProperty("foo", Person::age)), rendersTo("""
        <div .foo=${item.property0}></div>
        """));
  }

  @Test
  public void testPropertyList() {
    List<?> list = List.of(1, 2, 3);
    assertThat(render(lite -> lite.withElementPropertyList("foo", list)), rendersTo("""
        <div .foo=${[1,2,3]}></div>
        """));
  }

  @Test
  public void testPropertyMap() {
    Map<String, Integer> map = new LinkedHashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    assertThat(render(lite -> lite.withElementPropertyMap("foo", map)), rendersTo("""
        <div .foo=${{"a":1,"b":2}}></div>
        """));
  }

  @Test
  public void testPropertyJsonBoolean() {
    JsonValue value = Json.create(true);
    assertThat(render(lite -> lite.withElementPropertyJson("foo", value)), rendersTo("""
        <div .foo=${true}></div>
        """));
  }

  @Test
  public void testPropertyJsonDouble() {
    JsonValue value = Json.create(1.0);
    assertThat(render(lite -> lite.withElementPropertyJson("foo", value)), rendersTo("""
        <div .foo=${1.0}></div>
        """));
  }

  @Test
  public void testPropertyJsonString() {
    JsonValue value = Json.create("a");
    assertThat(render(lite -> lite.withElementPropertyJson("foo", value)), rendersTo("""
        <div .foo=${`a`}></div>
        """));
  }

  @Test
  public void testPropertyJsonStringEscaped() {
    // templates in JsonString values are escaped
    JsonValue value = Json.create("${item.bar}");
    assertThat(render(lite -> lite.withElementPropertyJson("foo", value)), rendersTo("""
        <div .foo=${`\\${item.bar}`}></div>
        """));
  }

  @Test
  public void testListener() {
    assertThat(render(lite -> lite.withListener("click", item -> {
    })), rendersTo("""
        <div @click=${()=>function0()}></div>
        """));
  }

  @Test
  public void testListenerWithArgs() {
    assertThat(render(lite -> lite.withListener("click", (item, args) -> {
    }, "arg1", "arg2")), rendersTo("""
        <div @click=${(event)=>function0(arg1, arg2)}></div>
        """));
  }

}
