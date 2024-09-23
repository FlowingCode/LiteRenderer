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
package com.flowingcode.vaadin.addons.litetemplate.it;

import com.flowingcode.vaadin.addons.litetemplate.LiteRenderer;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

@Route(IntegrationView.ROUTE)
public class IntegrationView extends Div implements IntegrationCallables {

  static final String ROUTE = "it";

  Grid<Item> grid = new Grid<>();

  public IntegrationView() {
    add(grid);
  }

  @Override
  @ClientCallable
  public JsonValue $call(JsonObject invocation) {
    return IntegrationCallables.super.$call(invocation);
  }

  @Override
  public void render(Item item, LiteRenderer<Item> renderer) {
    grid.removeAllColumns();
    grid.setItems(item);
    grid.addColumn(renderer);
    System.out.println(renderer);
  }

  @Override
  public Item getItem() {
    return grid.getListDataView().getItem(0);
  }

}
