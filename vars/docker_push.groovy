def call(String ProjectName, String ImageTag, String DockerHubuser){
  echo "Pushing the image to DockerHub"
  
  withCredentials([usernamePassword('credentialsId':"dockerHubCred", passwordVariable:"dockerHubPass", usernameVariable:"dockerHubUser")]){
    sh "docker login -u ${dockerHubUser} -p ${dockerHubPass}"
  }
  
  sh "docker push ${dockerHubUser}/${ProjectName}:${ImageTag}"
  
  echo "Image pushed to DockerHub"
}
