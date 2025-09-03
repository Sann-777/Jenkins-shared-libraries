def call() {
    // List of all services you want to install/cache
    def services = [
        [path: ".", name: "Root"],
        [path: "frontend-microservice", name: "Frontend"],
        [path: "microservices/api-gateway", name: "API Gateway"],
        [path: "microservices/auth-service", name: "Auth Service"],
        [path: "microservices/course-service", name: "Course Service"],
        [path: "microservices/payment-service", name: "Payment Service"],
        [path: "microservices/profile-service", name: "Profile Service"],
        [path: "microservices/rating-service", name: "Rating Service"],
        [path: "microservices/media-service", name: "Media Service"],
        [path: "microservices/notification-service", name: "Notification Service"]
    ]

    echo "🚀 Starting Jenkins Dependency Caching Setup..."

    // Cache global ~/.npm (so npm doesn’t re-download packages each run)
    jobcacher(
        caches: [[
            $class: 'ArbitraryFileCache',
            path: "${env.HOME}/.npm",
            key: "npm-global-cache",
            maxCacheSize: 1000
        ]]
    ) {
        services.each { service ->
            dir(service.path) {
                if (fileExists("package.json")) {
                    jobcacher(
                        caches: [[
                            $class: 'ArbitraryFileCache',
                            path: "node_modules",
                            key: "npm-${service.name.replaceAll(' ', '-')}-${env.BRANCH_NAME}",
                            maxCacheSize: 500
                        ]]
                    ) {
                        if (!fileExists("node_modules")) {
                            echo "💾 Installing dependencies for ${service.name}..."
                            sh "npm ci --cache ${env.HOME}/.npm --prefer-offline"
                        } else {
                            echo "⚡ Using cached dependencies for ${service.name}"
                        }
                    }
                } else {
                    echo "⚠️  Skipping ${service.name} (no package.json found)"
                }
            }
        }
    }

    echo "✅ Dependency caching complete!"
}
