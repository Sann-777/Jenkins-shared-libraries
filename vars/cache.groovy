def call(){
  echo "🔧 Setting up dependency caching..."
            
            // Generate cache keys based on package.json files
            def rootCacheKey = sh(
                script: 'md5sum package.json | cut -d" " -f1 2>/dev/null || echo "no-root"',
                returnStdout: true
            ).trim()
            
            def frontendCacheKey = sh(
                script: 'md5sum frontend-microservice/package.json | cut -d" " -f1 2>/dev/null || echo "no-frontend"',
                returnStdout: true
            ).trim()
            
            // Microservices cache keys
            def services = ['api-gateway', 'auth-service', 'course-service', 'payment-service', 'profile-service', 'rating-service', 'media-service', 'notification-service']
            def serviceCacheKeys = [:]
            
            services.each { service ->
                serviceCacheKeys[service] = sh(
                    script: "md5sum microservices/${service}/package.json | cut -d' ' -f1 2>/dev/null || echo 'no-${service}'",
                    returnStdout: true
                ).trim()
            }
            
            // Cache npm global directory
            cache(maxCacheSize: 1000, caches: [
                arbitraryFileCache(
                    path: '~/.npm',
                    fingerprint: 'npm-global-cache-v1'
                )
            ]) {
                
                // Cache root dependencies
                cache(maxCacheSize: 500, caches: [
                    arbitraryFileCache(
                        path: 'node_modules',
                        fingerprint: "npm-root-${rootCacheKey}"
                    )
                ]) {
                    if (!fileExists('node_modules')) {
                        echo "💾 Installing root dependencies..."
                        sh 'npm ci --cache ~/.npm --prefer-offline'
                    } else {
                        echo "⚡ Using cached root dependencies"
                    }
                }
                
                // Cache frontend dependencies
                if (fileExists('frontend-microservice/package.json')) {
                    cache(maxCacheSize: 500, caches: [
                        arbitraryFileCache(
                            path: 'frontend-microservice/node_modules',
                            fingerprint: "npm-frontend-${frontendCacheKey}"
                        )
                    ]) {
                        if (!fileExists('frontend-microservice/node_modules')) {
                            echo "💾 Installing frontend dependencies..."
                            dir('frontend-microservice') {
                                sh 'npm ci --cache ~/.npm --prefer-offline'
                            }
                        } else {
                            echo "⚡ Using cached frontend dependencies"
                        }
                    }
                }
                
                // Cache each microservice dependencies
                services.each { service ->
                    if (fileExists("microservices/${service}/package.json")) {
                        cache(maxCacheSize: 200, caches: [
                            arbitraryFileCache(
                                path: "microservices/${service}/node_modules",
                                fingerprint: "npm-${service}-${serviceCacheKeys[service]}"
                            )
                        ]) {
                            if (!fileExists("microservices/${service}/node_modules")) {
                                echo "💾 Installing ${service} dependencies..."
                                dir("microservices/${service}") {
                                    sh 'npm ci --cache ~/.npm --prefer-offline'
                                }
                            } else {
                                echo "⚡ Using cached ${service} dependencies"
                            }
                        }
                    }
                }
                
                // Install global tools if needed
                sh '''
                    if ! command -v jest &> /dev/null && ! [ -f "node_modules/.bin/jest" ]; then
                        echo "Installing Jest globally..."
                        npm install -g jest@latest --cache ~/.npm
                    fi
                    
                    if ! command -v concurrently &> /dev/null && ! [ -f "node_modules/.bin/concurrently" ]; then
                        echo "Installing Concurrently globally..."
                        npm install -g concurrently@latest --cache ~/.npm
                    fi
                '''
            }
            
            echo "✅ Dependency caching setup complete!"
}
