# oysh

oyster? shell? ssh? idk... oysh

A small library intended to expose an interactive shell over SSH from inside a Java application
and connecting with a standard SSH client.

---

### oysh-core

Framework-agnostic building blocks for serving an interactive shell over SSH from a Java 
application.

### oysh-spring-boot-starter

Easy setup and configuration for plugging oysh into a new or existing SpringBoot project.

### oysh-example

Runnable examples to demonstrate how to use this project.

- [**oysh-standalone-example**](./oysh-example/oysh-standalone-example/README.md) - A minimal 
example that leverages just the core module with no reliance on the Springboot Starter 
for exposing a tiny shell over ssh.
- [**oysh-spring-boot-example**](./oysh-example/oysh-spring-boot-example/README.md) - A minimal
example that uses the Springboot starter to configure and run oysh.

---

## Documentation

Documentation is managed by [mkdocs](https://www.mkdocs.org/) with the
[Material](https://squidfunk.github.io/mkdocs-material/) theme. Versioned deployments are handled
by [mike](https://github.com/jimporter/mike). Sources live in [`docs/`](./docs).

To view online, see [the documentation here](https://noahhhx.github.io/oysh/).

To build the documentation locally:
Install dependencies:
```shell
# Using pip
pip install mkdocs-material mike
# On Arch (using Yay)
yay -S mkdocs python-mike
```
Serve locally:
```shell
# Using Makefile
make docs
# Using mike
mkdocs serve -a localhost:8082
```
Then open `http://localhost:8082/oysh` in your browser.

---

## TODOs

- [] Non interactive exec channel support
- [] Public key auth
- [] Integrate spring security
- [] Integrate with [afon](https://github.com/noahhhx/afon)