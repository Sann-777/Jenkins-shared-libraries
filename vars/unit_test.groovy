def call(){
  echo "Testing each services."
  sh 'npm run install:all'
  sh 'npm run test:all'
  echo "Testing completed."
}
