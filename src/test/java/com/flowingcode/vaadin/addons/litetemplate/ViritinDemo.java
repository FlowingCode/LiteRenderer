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

import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.firitin.components.html.VSpan;

@DemoSource
@PageTitle("Viritin")
@SuppressWarnings("serial")
@Route(value = "lite-renderer/viritin", layout = RendererDemoView.class)
public class ViritinDemo extends Div {

  public ViritinDemo() {

    Grid<Person> grid = new Grid<>();
    grid.setItems(DataService.getPeople(20));
    grid.setHeightFull();

    // Flow Viritin components enable more fluent method chaining.
    grid.addColumn(LiteRenderer.<Person>of(
            VSpan.of("${item.firstName} ${item.lastName}")
                 .withStyle("color", "${item.color}"))
         .withProperty("color", person->person.age()<18?"red":"blue")
         .withProperty("firstName", Person::firstName)
         .withProperty("lastName", Person::lastName));

    add(grid);
  }

}
