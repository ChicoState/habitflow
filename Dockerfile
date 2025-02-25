# Use the CirrusLabs Android SDK image as the base
FROM ghcr.io/cirruslabs/android-sdk:34

# Install dependencies
RUN apt-get update && apt-get install -y curl unzip zip git openjdk-21-jdk \
    && apt-get clean

# Install SDKMAN
RUN curl -s "https://get.sdkman.io" | bash

# Ensure SDKMAN is available for all future commands
SHELL ["/bin/bash", "-c"]

# Install Kotlin and Gradle
RUN source /root/.sdkman/bin/sdkman-init.sh \
    && sdk install gradle \
    && sdk install kotlin

# Ensure SDKMAN is properly loaded
RUN echo "source /root/.sdkman/bin/sdkman-init.sh" >> /root/.bashrc

# Ensure Kotlin and Gradle are available in PATH
ENV PATH="/root/.sdkman/bin:/root/.sdkman/candidates/gradle/current/bin:/root/.sdkman/candidates/kotlin/current/bin:$PATH"

# Set the working directory inside the container
WORKDIR /workspace

# Default command (ensures login shell)
CMD ["/bin/bash", "-l"]
