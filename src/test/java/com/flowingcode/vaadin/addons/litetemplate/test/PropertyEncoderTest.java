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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.SneakyThrows;
import org.hamcrest.Matcher;
import org.junit.Test;

public class PropertyEncoderTest {

  private static final Method encode = lookupMethod();

  @SneakyThrows
  static Method lookupMethod() {
    var clazz = Class.forName("com.flowingcode.vaadin.addons.litetemplate.PropertyEncoder");
    var method = clazz.getDeclaredMethod("encode", String.class, String.class, String.class);
    method.setAccessible(true);
    return method;
  }

  @SneakyThrows
  private static String encode(String s) {
    return (String) ((Optional<?>) encode.invoke(null, s, "[", "]")).orElse(null);
  }

  private static Matcher<String> wrapped(String s) {
    return equalTo("[" + s + "]");
  }

  public void testValidTemplates() {
    String s;
    assertThat(encode(s = "${a}"), equalTo(s));
    assertThat(encode(s = "${`${a}`}"), equalTo(s));
    assertThat(encode(s = "${`${{}}`}"), equalTo(s));
    assertThat(encode(s = "${`a`}"), equalTo(s));
    assertThat(encode(s = "${'a'}"), equalTo(s));
    assertThat(encode(s = "${\"a\"}"), equalTo(s));
    assertThat(encode(s = "${{}}"), equalTo(s));
  }

  @Test
  public void testWrappedTemplates() {
    String s;
    assertThat(encode(s = "a"), wrapped(s));
    assertThat(encode(s = "$"), wrapped(s));
    assertThat(encode(s = "$$"), wrapped(s));
    assertThat(encode(s = "\\`"), wrapped(s));
    assertThat(encode(s = "`"), wrapped("\\`"));
  }

  @Test
  public void testInvalidTemplates() {
    assertThat(encode("${"), nullValue());
    assertThat(encode("${a"), nullValue());
    assertThat(encode("${{"), nullValue());
    assertThat(encode("${'a}"), nullValue());
    assertThat(encode("${a'}"), nullValue());
    assertThat(encode("${\"a}"), nullValue());
    assertThat(encode("${a\"}"), nullValue());
    assertThat(encode("${`a}"), nullValue());
    assertThat(encode("${a`}"), nullValue());
    assertThat(encode("${'}"), nullValue());
    assertThat(encode("${\"}"), nullValue());
    assertThat(encode("${`}"), nullValue());
  }
}
