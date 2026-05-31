# oysh

oyster? shell? ssh? idk... oysh

A small library intended to expose an interactive shell over SSH from inside a Java application
and connecting with a standard SSH client.

---

### oysh-core

Framework-agnostic building blocks for serving an interactive shell over SSH from a Java 
application.

### oysh-spring-boot-starter

TODO

### oysh-example

Runnable examples to demonstrate how to use this project.

- [**oysh-standalone-example**](./oysh-example/oysh-standalone-example/README.md) - A minimal 
example that leverages just the core module with no reliance on the Springboot Starter 
for exposing a tiny shell over ssh.
- [**oysh-spring-boot-example**]() - TODO


---

## Documentation

Documentation is managed by [mkdocs](https://www.mkdocs.org/). Written purely in Markdown, and
located in the [`docs`](./docs) directory.

To view online, see [the documentation here](https://noahhhx.github.io/oysh/).

To build the documentation locally. \
Install mkdocs:
```shell
# Using pip
pip install mkdocs-material
# On Arch (using Yay)
yay -S mkdocs
```
Run locally:
```shell
# Using Makefile
make docs
# Using mkdocs
mkdocs serve -a localhost:8082mkdocs serve -a localhost:8082
```
Then go to `http://localhost:8082/oysh` in your browser.

---

## TODOs

- [x] Add core module docs (sort of !TODO! versioned docs)
- [] Add core example docs
- [] Springboot starter module
- [] Springboot example project
- [] Springboot test cases
- [] Non interactive exec channel support
- [] Public key auth
- [] Integrate spring security