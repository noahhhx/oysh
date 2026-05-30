.PHONY: build-all build-dev run-standalone-example

build-all: ## Build whole project
	./mvnw clean install

build-dev: ## Build all skipping tests
	./mvnw clean install -DskipTests

run-standalone-example:
	java -jar oysh-example/oysh-standalone-example/target/oysh-standalone-example.jar
	
help: ## Show available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf " %-20s %s\n", $$1, $$2}'