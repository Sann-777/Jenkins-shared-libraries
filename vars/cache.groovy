def call() {
    jobcacher {
        // Cache global npm cache
        arbitraryFileCache(
            path: "${env.HOME}/.npm",
            includes: '**/*'
        )

        // Cache root node_modules if lockfile exists
        if (fileExists("package-lock.json")) {
            arbitraryFileCache(
                path: "node_modules",
                includes: '**/*',
                cacheValidityDecidingFile: "package-lock.json"
            )
        }

        // Detect and cache each microservice node_modules
        def microservices = findFiles(glob: "microservices/*/package-lock.json")
        microservices.each { lockFile ->
            def serviceDir = lockFile.path.replace("/package-lock.json", "")
            def serviceName = serviceDir.split("/")[-1]

            echo "📦 Caching dependencies for microservice: ${serviceName}"

            arbitraryFileCache(
                path: "${serviceDir}/node_modules",
                includes: '**/*',
                cacheValidityDecidingFile: "${serviceDir}/package-lock.json"
            )
        }
    }
}
