import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ecommerce"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

extra["springCloudVersion"] = "2025.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")

	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")

	// Package info - https://jitpack.io/#Metelyoff/outbox/1.0.0
	// Source code - https://github.com/Metelyoff/outbox
	implementation("com.github.Metelyoff:outbox:1.0.8")

	// Package info - https://jitpack.io/#Metelyoff/ecommerce-common-persistance/1.0.0
	// Source code - https://github.com/Metelyoff/ecommerce-common-persistance
	implementation("com.github.Metelyoff:ecommerce-common-persistance:1.0.0")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:junit-jupiter:1.21.3")
	testImplementation("org.testcontainers:postgresql:1.21.3")
	testImplementation("org.testcontainers:kafka:1.21.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// Overridden test dependencies due to vulnerability issues
	testImplementation("org.apache.commons:commons-compress:1.27.1")
	testImplementation("net.minidev:json-smart:2.5.2")
	testImplementation("org.apache.commons:commons-lang3:3.18.0")
	testImplementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.9")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<BootJar> {
	enabled = true
}
