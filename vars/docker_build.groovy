def call(String ProjectName, String ImageTag, String DockerHubUser){
    echo "Building the docker image"
    sh "docker build -t ${DockerHubUser}/${ProjectName}:${ImageTag} ."
    echo "Image built successfully"
}
