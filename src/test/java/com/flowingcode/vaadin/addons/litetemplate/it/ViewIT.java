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

package com.flowingcode.vaadin.addons.litetemplate.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import com.flowingcode.vaadin.addons.litetemplate.LiteRenderer;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTHTDElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;

public class ViewIT extends AbstractViewTest implements HasRpcSupport {

  private final IntegrationCallables $server = createCallableProxy(IntegrationCallables.class);

  private static final Item ITEM = Item.builder().foo("foo").bar("bar").build();

  public ViewIT() {
    super(IntegrationView.ROUTE);
  }

  private GridTHTDElement cell() {
    return $(GridElement.class).first().getCell(0, 0);
  }

  private TestBenchElement body() {
    return cell().$("div").first();
  }

  @Test
  public void testBodyInterpolation() {
    Div div = new Div("${item.foo} ${item.bar}");
    $server.render(ITEM,
        LiteRenderer.<Item>of(div)
        .withProperty("foo", Item::foo)
        .withProperty("bar", Item::bar));
    assertThat(cell().getText().trim(), equalTo("foo bar"));
    assertThat(body().getText().trim(), equalTo("foo bar"));
  }

  @Test
  public void testListener() {
    Div div = new Div("div");
    $server.render(ITEM, LiteRenderer.<Item>of(div).withListener(div, "click", item -> {
      item.clicked(true);
    }));
    assertThat($server.getItem().clicked(), is(false));
    body().click();
    assertThat($server.getItem().clicked(), is(true));
  }

  @Test
  public void testListenerWithArgs() {
    Div div = new Div("div");
    $server.render(ITEM, LiteRenderer.<Item>of(div).withListener(div, "click", (item, args) -> {
      item.foo(args.get(0).asString());
      item.clicked(true);
    }, "event.target.tagName"));
    assertThat($server.getItem().clicked(), is(false));
    body().click();
    assertThat($server.getItem().clicked(), is(true));
    assertThat($server.getItem().foo(), is("DIV"));
  }

}
