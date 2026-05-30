# oysh-standalone-example

Complete and minimal standalone example, listens on port 2223 and serves a tiny shell. 
The only framework used is JLine (transitvely) for line editing.

## Build

From the root of the project:
```shell
# Using Makefile (TODO - Build only this project?)
make build-dev
# Using Maven Wrapper
./mvnw clean install -DskipTests
```

Which will produce an executable jar at: 
`target/oysh-standalone-example.jar`

## Run

Again from the root of the project:
```shell
# Using Makefile (Requires JRE)
make run-standalone-example
# With Java
java -jar oysh-example/oysh-standalone-example/target/oysh-standalone-example.jar
```

## Usage

From another terminal:

```shell
ssh -p 2223 admin@127.0.0.1   # demo password: admin
demo> whoami
admin : ADMIN
demo> quit
```