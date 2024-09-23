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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Listener with args")
@SuppressWarnings("serial")
@Route(value = "lite-renderer/listener-args", layout = RendererDemoView.class)
public class ListenerArgsDemo extends Div {

  public ListenerArgsDemo() {

    Grid<Person> grid = new Grid<>();
    grid.setItems(DataService.getPeople(20));
    grid.setHeightFull();

    grid.addColumn(Person::firstName);
    grid.addColumn(Person::lastName);

    // Listeners can also receive more data in addition to the item:
    TextField tf = new TextField();
    LiteComponent.of(tf).withElementProperty("value", "I am ${item.firstName}");
    grid.addColumn(LiteRenderer.<Person>of(tf).withListener(tf, "change", (item, args) -> {
              Notification.show(item.firstName()
              + " " + item.lastName()
              + " says: " + args.getString(0));
    }, "event.target.value").withProperty("firstName", Person::firstName));

    add(grid);

  }

}
