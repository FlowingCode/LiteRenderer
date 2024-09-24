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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Button")
@SuppressWarnings("serial")
@Route(value = "lite-renderer/button", layout = RendererDemoView.class)
public class ButtonClickDemo extends Div {

  public ButtonClickDemo() {

    Grid<Person> grid = new Grid<>();
    grid.setItems(DataService.getPeople(20));
    grid.setHeightFull();

    grid.addColumn(Person::firstName);
    grid.addColumn(Person::lastName);

    // The click event of a template Button is called when the button is clicked.
    // Within the event listener, getItem returns the current item:
    grid.addColumn(LiteRenderer.<Person>of(new Button("Click", ev->{
      var item = LiteRenderer.getItem(ev, Person.class);
      Notification.show(item.firstName() + " " + item.lastName() + " Clicked!");
    })));

    add(grid);
  }

}
