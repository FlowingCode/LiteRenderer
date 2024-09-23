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
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Basic use")
@SuppressWarnings("serial")
@Route(value = "lite-renderer/demo", layout = RendererDemoView.class)
public class LiteRendererDemo extends Div {

  public LiteRendererDemo() {

    ComboBox<Person> comboBox = new ComboBox<>("Choose employee");
    comboBox.setItems(DataService.getPeople(10));

    comboBox.setItemLabelGenerator(person -> person.firstName() + " " + person.lastName());
    comboBox.getStyle().set("--vaadin-combo-box-overlay-width", "16em");

    // Use component instances as template

    Div template = new Div(
        new Image("${item.pictureUrl}", "Portrait of ${item.firstName} ${item.lastName}"),
        new Div(new Text("${item.firstName} ${item.lastName}"), new Div("${item.profession}")));
    template.setClassName("lite-renderer-demo-template");

    comboBox.setRenderer(LiteRenderer.<Person>of(template)
        .withProperty("pictureUrl", Person::pictureUrl)
        .withProperty("firstName", Person::firstName)
        .withProperty("lastName", Person::lastName)
        .withProperty("profession", Person::profession));
    add(comboBox);

  }

}
