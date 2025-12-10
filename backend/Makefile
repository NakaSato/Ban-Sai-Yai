
# Variables
COMPOSE = docker compose
MVN = ./mvnw

.PHONY: up down logs test clean build dev help

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

up: ## Start the application stack (Backend + DB + MailHog)
	$(COMPOSE) up -d

down: ## Stop the application stack
	$(COMPOSE) down

logs: ## Follow the logs of the stack
	$(COMPOSE) logs -f

clean: ## Clean Maven target directory
	$(MVN) clean

build: ## Build the application (skipping tests)
	$(MVN) clean package -DskipTests

test: ## Run unit and integration tests
	$(MVN) test

verify: ## Run full verification (including integration tests)
	$(MVN) verify

dev: ## Run the backend locally (requires DB to be running)
	$(MVN) spring-boot:run
