# Jenkins Shared Files Repository

This repository contains **shared files and scripts** that are used across multiple Jenkins pipelines.
These files are imported into `Jenkinsfile` to simplify function calls and maintain reusable logic.

## 📌 Purpose

* Centralized storage of reusable pipeline functions.
* Improves maintainability and avoids duplication in multiple `Jenkinsfile`s.
* Provides a standard way to extend Jenkins pipeline functionality.

## ⚙️ Usage in Jenkinsfile

You can use these shared functions in any Jenkins pipeline by referencing them in your `Jenkinsfile`.

Example:

```groovy
@Library('jenkins-shared-library') _

pipeline {
    agent any
    stages {
        stage('Example') {
            steps {
                script {
                    // Call a shared function
                    exampleStep()
                }
            }
        }
    }
}
```

## 🚀 How to Use

1. Upload this repository as a **Jenkins Shared Library** in your Jenkins configuration:

   * Go to **Manage Jenkins > Configure System > Global Pipeline Libraries**.
   * Add a new library and point it to this repository.

2. Import the library in your `Jenkinsfile` using:

   ```groovy
   @Library('your-shared-lib-name') _
   ```

3. Call the shared functions in your pipeline stages.

---

✅ With this setup, my Jenkins pipelines can reuse common logic, making them cleaner, more modular, and easier to maintain.
