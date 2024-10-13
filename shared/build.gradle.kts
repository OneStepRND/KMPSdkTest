import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp") version "0.0.8"
}

group = "com.zk.kmpsdktest"

val versionMajor = 1
val versionMinor = 0
val versionPatch = 11

// Accessing versionCode and versionName from the project's extra properties
val baseVersionName = "$versionMajor.$versionMinor.$versionPatch" // project.extra["baseVersionName"] as String
val versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch // project.extra["versionCode"] as Int


kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    val xcf = XCFramework()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.github.zivstep"
    compileSdk = 34
    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            artifact("${layout.buildDirectory.get().asFile}/outputs/aar/${project.name}-release.aar")
            groupId = group.toString()
            artifactId = "shared"
            version = baseVersionName

            pom {
                name.set("test SDK")
                description.set("Otest")
                url.set("https://github.com/ZivStep")

                developers {
                    developer {
                        id.set("ziv@onestep.co")
                        name.set("Ziv Kesten")
                        email.set("ziv@onestep.co")
                    }
                }

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/OneStepRND/android-sdk-prototype.git")
                    developerConnection.set("scm:git:ssh://github.com/OneStepRND/android-sdk-prototype.git")
                    url.set("scm:git:ssh://github.com/OneStepRND/android-sdk-prototype.git")
                }

                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")
                    configurations["api"].allDependencies.forEach {
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", it.group)
                        dependencyNode.appendNode("artifactId", it.name)
                        dependencyNode.appendNode("version", it.version)
                        dependencyNode.appendNode("scope", "compile")
                    }
                }
            }
        }
    }

    repositories {
        mavenLocal()
    }
}

if (System.getenv("CI") == null) {
    signing {
        val keyIdPath = System.getenv("SIGNING_KEYID_PATH") as String
        val password =
            System.getenv("SIGNING_PASSWORD") ?: findProperty("signing.password") as String

        val asciiArmoredKey: String = File(keyIdPath).readText()

        useInMemoryPgpKeys(asciiArmoredKey, password)
        sign(publishing.publications)
    }

    nmcp {
        publishAllPublications {
            username = System.getenv("MAVEN_CENTRAL_USERNAME")
                ?: findProperty("MAVEN_CENTRAL_USERNAME") as String
            password = System.getenv("MAVEN_CENTRAL_PASSWORD")
                ?: findProperty("MAVEN_CENTRAL_PASSWORD") as String
            publicationType = "AUTOMATIC"
        }
    }
}
