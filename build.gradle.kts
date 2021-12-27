buildscript {
    repositories {
        maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
        maven("https://artifactory.daiv.org/artifactory/gradle-dev-local")
    }
    dependencies {
        classpath("org.daiv.dependency:DependencyHandling:0.1.11")
    }
}

plugins {
    kotlin("multiplatform") version "1.6.10"
//    kotlin("plugin.serialization") version "1.6.10"
    id("com.jfrog.artifactory") version "4.17.2"
    id("org.daiv.dependency.VersionsPlugin") version "0.1.3"
    `maven-publish`
}

val versions = org.daiv.dependency.DefaultDependencyBuilder()

group = "org.daiv.appendable"
version = versions.setVersion { appendable }

repositories {
    mavenCentral()
    maven("https://artifactory.daiv.org/artifactory/gradle-dev-local")
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(versions.kutil())
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

artifactory {
    setContextUrl("${project.findProperty("daiv_contextUrl")}")
    publish(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig> {
        repository(delegateClosureOf<groovy.lang.GroovyObject> {
            setProperty("repoKey", "gradle-dev-local")
            setProperty("username", project.findProperty("daiv_user"))
            setProperty("password", project.findProperty("daiv_password"))
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<groovy.lang.GroovyObject> {
            invokeMethod("publications", arrayOf("jvm", "js", "kotlinMultiplatform", "metadata", "linuxX64"))
            setProperty("publishPom", true)
            setProperty("publishArtifacts", true)
        })
    })
}

versionPlugin {
    versionPluginBuilder = org.daiv.dependency.Versions.versionPluginBuilder {
        versionMember = { appendable }
        resetVersion = { copy(appendable = it) }
    }
    setDepending(tasks)
}

