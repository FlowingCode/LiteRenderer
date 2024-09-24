[![Published on Vaadin Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/lite-renderer-addon)
[![Stars on vaadin.com/directory](https://img.shields.io/vaadin-directory/star/lite-renderer-addon.svg)](https://vaadin.com/directory/component/lite-renderer-addon)
[![Build Status](https://jenkins.flowingcode.com/job/LiteRenderer-addon/badge/icon)](https://jenkins.flowingcode.com/job/LiteRenderer-addon)
[![Maven Central](https://img.shields.io/maven-central/v/com.flowingcode.vaadin.addons/lite-renderer-addon)](https://mvnrepository.com/artifact/com.flowingcode.vaadin.addons/lite-renderer-addon)
[![Javadoc](https://img.shields.io/badge/javadoc-00b4f0)](https://javadoc.flowingcode.com/artifact/com.flowingcode.vaadin.addons/lite-renderer-addon)

# Lite Renderer Add-on

**[EXPERIMENTAL]** A `Renderer` for Vaadin Flow that uses a component instance as template, intended for use as an alternative to `LitRenderer`.

```
grid.addColumn(LiteRenderer.<Person>of(
    new Button("Update", ev->handleUpdate(LiteRenderer.getItem(ev, Item.class))),
    new Button("Update", ev->handleRemove(LiteRenderer.getItem(ev, Item.class)));
```

Instead of:

```
grid.addColumn(LitRenderer.<Person>of(
     "<button @click=\"${handleUpdate}\">Update</button>" +
     "<button @click=\"${handleRemove}\">Remove</button>")
    .withFunction("handleUpdate", person -> { ... })
    .withFunction("handleRemove", person -> { ... })        
)
```

## Features

* Use component instances as template, instead of the HTML approach used by `LitRenderer`.
* The component is rewritten as a lit template.

## Online demo

[Online demo here](http://addonsv24.flowingcode.com/lite-renderer)

## Download release

[Available in Vaadin Directory](https://vaadin.com/directory/component/lite-renderer)

### Maven install

Add the following dependencies in your pom.xml file:

```xml
<dependency>
   <groupId>com.flowingcode.vaadin.addons</groupId>
   <artifactId>lite-renderer-addon</artifactId>
   <version>X.Y.Z</version>
</dependency>
```
<!-- the above dependency should be updated with latest released version information -->

Release versions are available from Maven Central repository. For SNAPSHOT versions see [here](https://maven.flowingcode.com/snapshots/).

## Building and running demo

- git clone repository
- mvn clean install jetty:run

To see the demo, navigate to http://localhost:8080/

## Release notes

See [here](https://github.com/FlowingCode/LiteRenderer/releases)

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. 

As first step, please refer to our [Development Conventions](https://github.com/FlowingCode/DevelopmentConventions) page to find information about Conventional Commits & Code Style requirements.

Then, follow these steps for creating a contribution:

- Fork this project.
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- For commit message, use [Conventional Commits](https://github.com/FlowingCode/DevelopmentConventions/blob/main/conventional-commits.md) to describe your change.
- Send a pull request for the original project.
- Comment on the original issue that you have implemented a fix for it.

## License & Author

This add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

Lite Renderer Add-On is written by Flowing Code S.A.

# Developer Guide

## Getting started


- Use component instances as template:
``` 
    Div template = new Div(
        new Image("${item.pictureUrl}", "Portrait of ${item.firstName} ${item.lastName}"),
        new Div("${item.firstName} ${item.lastName}"),
        new Div("${item.profession}"));

    comboBox.setRenderer(LiteRenderer.<Person>of(template)
        .withProperty("pictureUrl", Person::pictureUrl)
        .withProperty("firstName", Person::firstName)
        .withProperty("lastName", Person::lastName)
        .withProperty("profession", Person::profession));
```

- `withAttribute` and `withListener` are fluent methods that allow setting attributes and listeners:
```
    Div div = new Div("${item.firstName} ${item.lastName}");
    grid.addColumn(LiteRenderer.<Person>of(div)
        .withProperty("firstName", Person::firstName)
        .withProperty("lastName", Person::lastName)
        .withProperty("age", Person::age)
        .withAttribute(div, "tooltip", "Age: ${item.age}")
        .withAttribute(div, "style", person-> "color: "+ (person.age()<18?"red":"blue"))
        .withListener(div, "click", item -> {
          Notification.show(item.firstName() + " " + item.lastName() + " Clicked!");
        }
    ));
```
	
- Wrapping a component with `LiteComponent` allows fluent setters for attributes, properties, and listeners, removing the need for variables.
```
    Div div = new Div("${item.firstName} ${item.lastName}");
    grid.addColumn(LiteRenderer.<Person>of(div)
        .withProperty("firstName", Person::firstName)
        .withProperty("lastName", Person::lastName)
        .withProperty("age", Person::age)
        .withAttribute(div, "tooltip", "Age: ${item.age}")
        .withAttribute(div, "style", person-> "color: "+ (person.age()<18?"red":"blue"))
        .withListener(div, "click", item -> {
          Notification.show(item.firstName() + " " + item.lastName() + " Clicked!");
        }
    ));
```	
	
- Listeners can also receive more data in addition to the item:
```
    TextField tf = new TextField();
    grid.addColumn(LiteRenderer.<Person>of(tf).withListener(tf, "change", (item, args) -> {
              Notification.show(item.firstName()
              + " " + item.lastName()
              + " says: " + args.getString(0));
    }, "event.target.value"));
```

- The click event of a template `Button` is called when the button is clicked. Within the event listener, `LiteRenderer.getItem(ev, Person.class)` returns the current item:
```
    grid.addColumn(LiteRenderer.<Person>of(new Button("Click", ev->{
      var item = LiteRenderer.getItem(ev, Person.class);
      Notification.show(item.firstName() + " " + item.lastName() + " Clicked!");
    })));
```
	
- [Flow Viritin](https://vaadin.com/directory/component/flow-viritin) components enable more fluent method chaining:
```
    grid.addColumn(LiteRenderer.<Person>of(
            VSpan.of("${item.firstName} ${item.lastName}")
                 .withStyle("color", "${item.color}"))
         .withProperty("color", person->person.age()<18?"red":"blue")
         .withProperty("firstName", Person::firstName)
         .withProperty("lastName", Person::lastName));
```