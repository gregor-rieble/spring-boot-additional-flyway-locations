# spring-boot-additional-flyway-locations

[![Release](https://github.com/gregor-rieble/spring-boot-additional-flyway-locations/actions/workflows/release.yml/badge.svg)](https://github.com/gregor-rieble/spring-boot-additional-flyway-locations/actions/workflows/release.yml)
[![Build & Deploy SNAPSHOT](https://github.com/gregor-rieble/spring-boot-additional-flyway-locations/actions/workflows/deploy-snapshot.yml/badge.svg)](https://github.com/gregor-rieble/spring-boot-additional-flyway-locations/actions/workflows/deploy-snapshot.yml)

## Introduction

Adds an `@AdditionalFlywayLocations` annotation to support specifying additional flyway locations through
spring configuration classes

This is mostly useful when writing custom spring boot starters that want to automatically add database
initialization scripts that are bundled with the starter library.

## Usage

In a custom spring boot starter project, add the following dependency to your
`pom.xml`:

```xml

<dependency>
    <groupId>de.gcoding</groupId>
    <artifactId>spring-boot-additional-flyway-locations</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Alternatively, you can also choose to use the `provided` scope, if you want the user of your starter
project to decide whether he wants the scripts to be loaded automatically:

```xml

<dependency>
    <groupId>de.gcoding</groupId>
    <artifactId>spring-boot-additional-flyway-locations</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Then create or use an existing spring boot class annotated with `@Configuration` or
`@AutoConfiguration` and add the `@AdditionalFlywayLocations` annotation pointing to
the flyway scripts used by your starter project:

```java

@Configuration
@AdditionalFlywayLocations("classpath:db/my-project/migrations")
class MyStarterProjectConfiguration {
    // Additional configuration as needed
    // ...
}
```

> Depending on your needs, don't forget to add your configuration class to the `META-INF/spring.factories`
> file to make spring boot load it automatically