def call(Map config = [:]) {
    echo "🔧 Running shared cache setup..."

    // Default services list (you can override by passing config.services)
    def services = config.get('services', [
        'api-gateway',
        'auth-service',
        'course-service',
        'payment-service',
        'profile-service',
        'rating-service',
        'media-service',
        'notification-service'
    ])

    // Cache root dependencies
    if (fileExists('package.json')) {
        def rootCacheKey = sh(
            script: 'md5sum package.json | cut -d" " -f1 2>/dev/null || echo "no-root"',
            returnStdout: true
        ).trim()

        cache(maxCacheSize: 500, caches: [
            arbitraryFileCache(
                path: 'node_modules',
                fingerprint: "npm-root-${rootCacheKey}"
            )
        ]) {
            if (!fileExists('node_modules')) {
                echo "💾 Installing root dependencies..."
                sh 'npm ci --prefer-offline'
            } else {
                echo "⚡ Using cached root dependencies"
            }
        }
    }

    // Cache frontend dependencies
    if (fileExists('frontend-microservice/package.json')) {
        def frontendCacheKey = sh(
            script: 'md5sum frontend-microservice/package.json | cut -d" " -f1 2>/dev/null || echo "no-frontend"',
            returnStdout: true
        ).trim()

        cache(maxCacheSize: 500, caches: [
            arbitraryFileCache(
                path: 'frontend-microservice/node_modules',
                fingerprint: "npm-frontend-${frontendCacheKey}"
            )
        ]) {
            if (!fileExists('frontend-microservice/node_modules')) {
                echo "💾 Installing frontend dependencies..."
                dir('frontend-microservice') {
                    sh 'npm ci --prefer-offline'
                }
            } else {
                echo "⚡ Using cached frontend dependencies"
            }
        }
    }

    // Cache each microservice dependencies
    services.each { service ->
        if (fileExists("microservices/${service}/package.json")) {
            def serviceCacheKey = sh(
                script: "md5sum microservices/${service}/package.json | cut -d' ' -f1 2>/dev/null || echo 'no-${service}'",
                returnStdout: true
            ).trim()

            cache(maxCacheSize: 200, caches: [
                arbitraryFileCache(
                    path: "microservices/${service}/node_modules",
                    fingerprint: "npm-${service}-${serviceCacheKey}"
                )
            ]) {
                if (!fileExists("microservices/${service}/node_modules")) {
                    echo "💾 Installing ${service} dependencies..."
                    dir("microservices/${service}") {
                        sh 'npm ci --prefer-offline'
                    }
                } else {
                    echo "⚡ Using cached ${service} dependencies"
                }
            }
        }
    }

    echo "✅ Dependency caching complete!"
}
