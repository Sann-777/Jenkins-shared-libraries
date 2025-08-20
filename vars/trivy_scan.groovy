def call(){
  echo "Scanning <path or image>"
  sh "trivy fs ."
  echo "✅ Scan complete"
}
