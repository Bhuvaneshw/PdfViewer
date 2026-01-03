plugins {
    alias(libs.plugins.kotlin.dokka)
}

/*
 * allow custom properties from cli arg. helpful for CI/CD
 * sample cmd: gradle dokkaGenerate -Prelease-version=1.0.0 -PdocsDir=build/docs
 */
val apiVersion: String = if (project.hasProperty("release-version")) {
    project.property("release-version")!!.toString()
} else {
    project.version.toString()
}
val dokkaVersionsDirectory = run {
    val outputDirectory = providers.gradleProperty("docsDir").orNull
    return@run outputDirectory?.let(rootDir::resolve)
}

dokka {
    moduleName = "PdfViewer"

    pluginsConfiguration {
        versioning {
            version = apiVersion
            if (dokkaVersionsDirectory != null) olderVersionsDir = dokkaVersionsDirectory
            // TODO: set olderVersionsDirName to empty string
//            olderVersionsDirName = ""
//            waiting for this property to be release in dokka
//            property is in source but not yet released (checked v: 2.1.0)
            renderVersionsNavigationOnAllPages = true
        }
        pluginsConfiguration.html {
            customStyleSheets.from("style.css", "dokka-style.css")
            customAssets.from("logo.png", "main.js")
            footerMessage.set("By Bhuvaneshwaran")
        }
    }

    dokkaPublications.html {
        includes.from("README.md")
        if (dokkaVersionsDirectory != null)
            outputDirectory = dokkaVersionsDirectory.resolve(apiVersion)
    }
}

dependencies {
    dokka(project(":core"))
    dokka(project(":compose"))
    dokka(project(":ui"))
    dokka(project(":compose-ui"))

    dokkaHtmlPlugin(libs.dokka.versioning.plugin)
}
