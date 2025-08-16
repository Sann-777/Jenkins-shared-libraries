def call(){
  echo "Deploying the Django App"
  sh "docker-compose down && docker-compose up -d"
  echo "App successfully deployed"
}
