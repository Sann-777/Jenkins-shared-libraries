def call(){
  echo "insatlling required dependencies"
  sh 'npm run ci:setup'
  echo "dependencies installed."
  
  echo "Testing each services."
  sh 'npm run install:all'
  sh 'npm run test:services:ready'
  echo "Testing completed."

  echo "Testing lint and style"
  sh 'npm run lint:services:ready'
  echo "Testing complete"
}
