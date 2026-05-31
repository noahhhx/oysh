.PHONY: build-all build-dev run-standalone-example docs

build-all: ## Build whole project
	./mvnw clean install

build-dev: ## Build all skipping tests
	./mvnw clean install -DskipTests

run-standalone-example: ## Run standalone example program
	java -jar oysh-example/oysh-standalone-example/target/oysh-standalone-example.jar
	
docs: ## Serve documentation locally (usage: make docs PORT=8082)
	mkdocs serve -a localhost:$(or $(PORT), 8082)

help: ## Show available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf " %-20s %s\n", $$1, $$2}'