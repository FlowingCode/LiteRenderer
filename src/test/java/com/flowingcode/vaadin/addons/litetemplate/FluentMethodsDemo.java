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

import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Fluent methods")
@SuppressWarnings("serial")
@Route(value = "lite-renderer/fluent", layout = RendererDemoView.class)
public class FluentMethodsDemo extends Div {

  public FluentMethodsDemo() {

    Grid<Person> grid = new Grid<>();
    grid.setItems(DataService.getPeople(20));
    grid.setHeightFull();

    // withAttribute and withListener are fluent methods that allow setting attributes and listeners
    Div div = new Div("${item.firstName} ${item.lastName}");
    grid.addColumn(LiteRenderer.<Person>of(div)
        .withProperty("firstName", Person::firstName)
        .withProperty("lastName", Person::lastName)
        .withProperty("age", Person::age)
        .withAttribute(div, "title", "Age: ${item.age}")
        .withAttribute(div, "style", person-> "color: "+ (person.age()<18?"red":"blue"))
        .withListener(div, "click", item -> {
          Notification.show(item.firstName() + " " + item.lastName() + " Clicked!");
        }
    ));

    add(grid);

  }

}
