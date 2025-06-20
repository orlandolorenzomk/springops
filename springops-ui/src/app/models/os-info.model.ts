/**
 * Model representing OS information retrieved from the server
 */
export interface OsInfo {
  hostname: string;
  operatingSystem: string;
  memoryAvailable: string;
  diskTotal: string;
  diskFree: string;
  kernelVersion: string;
  architecture: string;
  uptime: string;
  timezone: string;
  bootTime: string;
  kernelModulesCount: string;
  packageCount: string;
  cpuModel: string;
  cpuCores: string;
  memoryTotal: string;
  memoryUsed: string;
  loadAverage: string;
  diskSpace: string;
  ipAddress: string;
  defaultGateway: string;
  dnsServers: string;
  macAddress: string;
  networkInterfaces: string;
  publicIp: string;
  firewallStatus: string;
  sshEnabled: string;
  openPorts: string;
  topMemoryProcesses: string;
  topCpuProcesses: string;
  runningServices: string;
  fileSystems: string;
  inodeUsage: string;
  loggedUsers: string;
  userAccountsCount: string;
  lastReboot: string;
  [key: string]: string; // Index signature for dynamic properties
}
