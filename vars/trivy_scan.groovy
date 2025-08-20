def call(){
  echo "Scanning image"
  sh "trivy fs ."
  echo "✅ Scan complete"
}
