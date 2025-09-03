def call(Map config = [:]) {
    // Default list of microservices
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

    pipeline {
        agent any
        stages {
            stage('Cache Dependencies') {
                steps {
                    script {
                        echo "🔧 Setting up dependency caching..."

                        // Cache global npm cache
                        cache(path: "${env.HOME}/.npm", key: "npm-global-cache") {
                            echo "⚡ Using global npm cache"
                        }

                        // Root dependencies
                        cache(path: "node_modules", key: "npm-root-${env.BRANCH_NAME}") {
                            sh 'npm ci --cache ~/.npm --prefer-offline'
                        }

                        // Frontend dependencies
                        if (fileExists("frontend-microservice/package.json")) {
                            dir("frontend-microservice") {
                                cache(path: "node_modules", key: "npm-frontend-${env.BRANCH_NAME}") {
                                    sh 'npm ci --cache ~/.npm --prefer-offline'
                                }
                            }
                        }

                        // Microservices dependencies
                        services.each { service ->
                            if (fileExists("microservices/${service}/package.json")) {
                                dir("microservices/${service}") {
                                    cache(path: "node_modules", key: "npm-${service}-${env.BRANCH_NAME}") {
                                        sh 'npm ci --cache ~/.npm --prefer-offline'
                                    }
                                }
                            }
                        }

                        echo "✅ Dependency caching complete!"
                    }
                }
            }
        }
    }
}
