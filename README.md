# Steps
1. First call to /setup/is-complete
2. Check what is not initialized,
   - If admin is not initialized, call /setup/initialize-admin
   - If files not initialized, call /setup/initialize-files
3. Call /setup/is-complete again. If it returns true redirect to login
