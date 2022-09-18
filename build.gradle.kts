plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}
dependencies {
    implementation("com.sparkjava:spark-core:2.9.2")
    implementation("dev.imabad.mceventsuite:eventcore:1.0-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.auth0:java-jwt:3.10.3")
    testImplementation("junit:junit:4.12")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "dev.imabad.mceventsuite.api.EventAPI"
    }
}