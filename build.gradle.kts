plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.6.0"
}

group = "com.playground"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

spotless {
    java {
        // Use the default importOrder configuration
        importOrder()

        removeUnusedImports()
        forbidWildcardImports() // or expandWildcardImports, see below
        forbidModuleImports()

        googleJavaFormat()
    }
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jsoup:jsoup:1.22.2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	dependsOn("spotlessApply")
	useJUnitPlatform()
}
