FROM eclipse-temurin:17-jdk-alpine 
COPY SeilatsatsiSaRona.war app.war 
EXPOSE 8080 
ENTRYPOINT ["java", "-jar", "app.war"] 
