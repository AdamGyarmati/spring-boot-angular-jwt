# Használj egy alap Spring Boot képet
FROM openjdk:11

# Másold be a projekt forrásfájljait a konténerbe
COPY . /app

# Állítsd be a munkakönyvtárat
WORKDIR /app

# Telepítsd a szükséges függőségeket és buildeld újra az alkalmazást
RUN ./mvnw clean package

# Indítsd el az alkalmazást
CMD ["java", "-jar", "target/security-demo-with-angular-0.0.1-SNAPSHOT.jar"]