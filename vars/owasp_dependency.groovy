def call(){
  echo "Dependency check started"
  dependencyCheck additionalArguments: '--scan ./', odcInstallation: 'OWASP'
  dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
  echo "Dependency check completed"
}
