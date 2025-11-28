# --- build stage ---
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

# Copy the project sources
COPY . .

# Compile java sources into 'out' directory
RUN mkdir -p out \
    && javac -d out src/main/java/com/example/tictactoe/*.java

# Create runnable jar (with Main-Class)
RUN jar cfe tictactoe.jar com.example.tictactoe.Main -C out .

# --- runtime stage ---
FROM eclipse-temurin:24-jdk-jammy AS runtime
WORKDIR /app

# Copy jar and static files from build stage
COPY --from=build /app/tictactoe.jar ./tictactoe.jar
COPY --from=build /app/static ./static

# Expose default dev port (Render provides PORT at runtime)
EXPOSE 8000

# Run the application
CMD ["java", "-jar", "tictactoe.jar"]
