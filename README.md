# Jenkins Shared Files Repository

This repository contains **shared files and scripts** that are used across multiple Jenkins pipelines.
These files are imported into `Jenkinsfile` to simplify function calls and maintain reusable logic.

## 📌 Purpose

* Centralized storage of reusable pipeline functions.
* Improves maintainability and avoids duplication in multiple `Jenkinsfile`s.
* Provides a standard way to extend Jenkins pipeline functionality.

## ⚙️ Usage in Jenkinsfile

We can use these shared functions in any Jenkins pipeline by referencing them in our `Jenkinsfile`.

Example:

```groovy
@Library("Shared") _
pipeline {
    agent { label "Vinod" }
    stages {
        stage(hello){
            steps{
                script{
                    hello()
                }
            }
        }
        stage("Clone"){
            steps{
                script{
                    clone("https://github.com/Sann-777/Django-notes-k8s.git","main")
                }
            }
        }
        stage("Build"){
            steps{
                script{
                    docker_build("notes-app", "latest", "asxhazard")
                }
            }
        }
        stage("Push to DockerHub"){
            steps{
                script{
                   docker_push("notes-app", "latest", "asxhazard") 
                }
            }
        }
        stage("Deploy"){
            steps{
                script{
                    docker_compose()
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
   @Library('Shared') _
   ```

3. Call the shared functions in your pipeline stages.

---

✅ With this setup, our Jenkins pipelines can reuse common logic, making them cleaner, more modular, and easier to maintain.
