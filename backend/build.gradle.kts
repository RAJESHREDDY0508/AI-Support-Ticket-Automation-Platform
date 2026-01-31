plugins {
    java
}

allprojects {
    group = "com.ticketplatform"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
    }
}
