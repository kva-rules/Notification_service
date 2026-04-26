FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/notification-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087
ENV SPRING_PROFILES_ACTIVE=local
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
