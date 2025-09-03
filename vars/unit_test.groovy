def call(){
  echo "Testing each services."
  sh 'npm run install:all'
  sh 'npm run test:services:ready'
  echo "Testing completed."
}
