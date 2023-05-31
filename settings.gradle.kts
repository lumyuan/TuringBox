pluginManagement {
    repositories {
        maven( url = "https://maven.aliyun.com/repository/public/")
        maven( url = "https://maven.aliyun.com/repository/google/")

        google()
        mavenCentral()
        maven("https://repo.eclipse.org/content/groups/releases/")
        maven("https://maven.aliyun.com/nexus/content/groups/public/")

        maven(url = "https://jitpack.io")
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven( url = "https://maven.aliyun.com/repository/public/")
        maven( url = "https://maven.aliyun.com/repository/google/")

        google()
        mavenCentral()
        maven("https://repo.eclipse.org/content/groups/releases/")
        maven("https://maven.aliyun.com/nexus/content/groups/public/")

        maven(url = "https://jitpack.io")
        gradlePluginPortal()
    }
}

rootProject.name = "TuringBox"
include(":app")
 