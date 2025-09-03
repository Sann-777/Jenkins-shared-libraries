#!/usr/bin/env groovy

/**
 * cacheNodeDeps.groovy
 * Installs AND caches Node.js dependencies for all StudyNotion microservices
 */

def call(Map params = [:]) {
    def config = [
        rootDir: params.rootDir ?: "${env.WORKSPACE}",
        npmCacheDir: params.npmCacheDir ?: "/tmp/.npm-cache",
        // Your specific service paths
        services: params.services ?: [
            "microservices/api-gateway": "API Gateway",
            "microservices/auth-service": "Auth Service", 
            "microservices/course-service": "Course Service",
            "microservices/payment-service": "Payment Service",
            "microservices/profile-service": "Profile Service",
            "microservices/rating-service": "Rating Service",
            "microservices/media-service": "Media Service",
            "microservices/notification-service": "Notification Service",
            "frontend-microservice": "Frontend Service"
        ]
    ]

    echo "🚀 Starting StudyNotion Microservices Dependency Caching"
    echo "========================================================"

    // 1. Cache root dependencies first
    cacheRootDependencies(config)

    // 2. Cache all microservices in parallel
    cacheAllMicroservices(config)

    echo "🎉 All StudyNotion dependencies cached successfully!"
    echo "========================================================"
}

/**
 * Cache root dependencies
 */
private void cacheRootDependencies(Map config) {
    echo "📦 Caching root dependencies..."
    
    dir(config.rootDir) {
        if (fileExists('package.json')) {
            // CORRECTED: Use named parameters for arbitraryFileCache
            arbitraryFileCache(
                path: "studynotion-root-deps-${env.BRANCH_NAME ?: 'main'}",
                cacheValidityDecidingFile: 'package-lock.json',
                includes: 'node_modules/**,.npm-cache/**,package-lock.json',
                excludes: 'node_modules/.cache/**,**/*.log'
            ) {
                echo "🚨 Root cache miss: Installing root dependencies"
                installDependencies(config.rootDir, config.npmCacheDir, "Root")
            }
            echo "✅ Root dependencies cached"
        } else {
            echo "⚠️  No root package.json found, skipping root dependencies"
        }
    }
}

/**
 * Cache all microservices in parallel
 */
private void cacheAllMicroservices(Map config) {
    echo "📦 Caching microservices dependencies in parallel..."
    
    def parallelStages = [:]
    
    config.services.each { servicePath, serviceName ->
        parallelStages["Cache-${serviceName}"] = {
            cacheSingleService(config.rootDir, servicePath, serviceName, config.npmCacheDir)
        }
    }
    
    // Execute all service caching in parallel
    parallel parallelStages
}

/**
 * Cache a single microservice
 */
private void cacheSingleService(String rootDir, String servicePath, String serviceName, String npmCacheDir) {
    def fullPath = "${rootDir}/${servicePath}"
    
    echo "🔧 Processing: ${serviceName} (${servicePath})"
    
    dir(fullPath) {
        if (!fileExists('package.json')) {
            echo "⚠️  package.json not found in ${servicePath}, skipping ${serviceName}"
            return
        }

        // Create cache key with service name for uniqueness
        def cacheKey = "studynotion-${servicePath.replace('/', '-')}-${env.BRANCH_NAME ?: 'main'}"
        
        // CORRECTED: Use named parameters for arbitraryFileCache
        arbitraryFileCache(
            path: cacheKey,
            cacheValidityDecidingFile: 'package-lock.json',
            includes: 'node_modules/**,.npm-cache/**,package-lock.json',
            excludes: 'node_modules/.cache/**,**/*.log'
        ) {
            echo "🚨 Cache miss for ${serviceName}: Installing dependencies"
            installDependencies(fullPath, npmCacheDir, serviceName)
        }
        
        echo "✅ ${serviceName} dependencies cached"
    }
}

/**
 * Install dependencies for a specific directory
 */
private void installDependencies(String directory, String npmCacheDir, String serviceName) {
    dir(directory) {
        try {
            // Configure npm for better caching
            sh """
                npm config set cache ${npmCacheDir}
                npm config set prefer-offline true
                npm config set fund false
                npm config set audit false
            """

            echo "🔧 Installing dependencies for ${serviceName}..."
            
            if (fileExists('package-lock.json')) {
                // Use npm ci for deterministic installs
                sh 'npm ci --prefer-offline --silent --no-audit --no-fund'
            } else {
                // Use npm install if no lock file
                sh 'npm install --production=false --silent --no-audit --no-fund'
            }
            
            echo "✅ ${serviceName} dependencies installed successfully"
            
        } catch (Exception e) {
            echo "❌ Installation failed for ${serviceName}: ${e.message}"
            echo "🔄 Trying fallback strategy..."
            
            // Fallback: clean install
            sh 'rm -rf node_modules package-lock.json'
            sh 'npm install --production=false --silent --no-audit --no-fund'
            
            echo "✅ ${serviceName} fallback installation completed"
        }
    }
}

/**
 * Check cache status for all services - FIXED: Now a method that can be called from post
 */
def checkAllCacheStatus() {
    def services = [
        "microservices/api-gateway": "API Gateway",
        "microservices/auth-service": "Auth Service",
        "microservices/course-service": "Course Service",
        "microservices/payment-service": "Payment Service",
        "microservices/profile-service": "Profile Service",
        "microservices/rating-service": "Rating Service",
        "microservices/media-service": "Media Service",
        "microservices/notification-service": "Notification Service",
        "frontend-microservice": "Frontend Service"
    ]

    echo "📋 StudyNotion Cache Status Report"
    echo "========================================================"
    
    // Check root
    if (fileExists('package.json')) {
        def size = getDirectorySize('node_modules')
        echo "📊 Root: node_modules=${fileExists('node_modules')}, size=${size}"
    }
    
    // Check each service
    services.each { servicePath, serviceName ->
        if (fileExists(servicePath + '/package.json')) {
            dir(servicePath) {
                def size = getDirectorySize('node_modules')
                echo "📊 ${serviceName}: node_modules=${fileExists('node_modules')}, size=${size}"
            }
        } else {
            echo "📊 ${serviceName}: ❌ package.json not found"
        }
    }
}

/**
 * Get directory size in human-readable format
 */
private String getDirectorySize(String path) {
    try {
        return sh(
            script: "du -sh ${path} 2>/dev/null | cut -f1 || echo '0K'",
            returnStdout: true
        ).trim()
    } catch (Exception e) {
        return '0K'
    }
}
