def call(String ProjectName, String ImageTag, String DockerHubUser){
  echo "Building the docker image"
  sh "docker build -t ${DockerHubUser}/${ProjectNaame}:${ImageTag} ."
  echo "Image built successfully"
}
