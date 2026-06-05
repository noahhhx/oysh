# oysh-spring-boot-example

Example of how to integrate oysh into a Springboot project. Listens on port 2222
and serves Picocli commands over SSH.

## Build

From the root of the project:
```shell
# Using Makefile (TODO - Build only this project?)
make build-dev
# Using Maven Wrapper
./mvnw clean install -DskipTests
```

Which will produce an executable jar at:
`target/oysh-spring-boot-example.jar`

## Run

### **Without** building

You should be able to run the 
[OyshApplication](./src/main/java/io/github/noahhhx/oysh/boot/OyshApplication.java) class directly
from your IDE of choice.

Or

Using Maven
```shell
# Using Makefile
make run-spring-example
# Using Maven Wrapper
./mvnw spring-boot:run -pl oysh-example/oysh-spring-boot-example
```

## Usage