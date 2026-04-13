# jarvis-deploy

A lightweight Java CLI for managing multi-environment deployment configs without a full orchestration stack.

---

## Installation

```bash
git clone https://github.com/your-org/jarvis-deploy.git
cd jarvis-deploy && ./mvnw clean package -q && mv target/jarvis-deploy.jar /usr/local/bin/jarvis
```

---

## Usage

Run the CLI with a target environment and config file:

```bash
java -jar jarvis.jar deploy --env production --config ./configs/prod.yaml
```

**Common commands:**

```bash
# List all available environments
java -jar jarvis.jar envs list

# Validate a config before deploying
java -jar jarvis.jar validate --config ./configs/staging.yaml

# Diff configs between two environments
java -jar jarvis.jar diff --from staging --to production
```

A minimal config file looks like:

```yaml
environment: staging
region: us-east-1
replicas: 2
image: myapp:latest
```

---

## Requirements

- Java 17+
- Maven 3.8+

---

## Contributing

Pull requests are welcome. Please open an issue first to discuss any significant changes.

---

## License

This project is licensed under the [MIT License](LICENSE).