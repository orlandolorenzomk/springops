# Steps
1. First call to /setup/is-complete
2. Check what is not initialized,
   - If admin is not initialized, call /setup/initialize-admin
   - If files not initialized, call /setup/initialize-files
   - If encryption not initialized, call /setup/initialize-encryption
   - If git ssh is not initialized, call /setup/initialize-git-ssh-key
3. Call /setup/is-complete again. If it returns true redirect to login


# To do
- [ ] Create SSH Key save script in resources
- [ ] Save the Git SSH key in the host machine