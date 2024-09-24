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

import com.flowingcode.vaadin.addons.DemoLayout;
import com.flowingcode.vaadin.addons.GithubLink;
import com.flowingcode.vaadin.addons.demo.TabbedDemo;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@ParentLayout(DemoLayout.class)
@Route("lite-renderer")
@GithubLink("https://github.com/FlowingCode/AddonStarter24")
@CssImport("./styles/lite-template-demo-styles.css")
public class RendererDemoView extends TabbedDemo {

  public RendererDemoView() {
    addDemo(LiteRendererDemo.class);
    addDemo(FluentMethodsDemo.class);
    addDemo(LiteComponentDemo.class);
    addDemo(ListenerArgsDemo.class);
    addDemo(ButtonClickDemo.class);
    addDemo(ViritinDemo.class);
    setSizeFull();
  }
}
