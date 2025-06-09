export interface SetupStatus {
  isSetupComplete: boolean;
  isFirstAdminInitialized: boolean;
  isFilesRootInitialized: boolean;
  ipAddress: string;
  serverName: string;
  environment: string;
}
