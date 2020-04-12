# TVeebot
Downloader component for the TVeeBot application


# Build Docker Image

```bash
./gradlew build
docker build -t tveebot . --file docker/Dockerfile
```

# Push Docker Image

```bash
docker login --username=dfialho14
docker push dfialho14/tveebot:latest
```
