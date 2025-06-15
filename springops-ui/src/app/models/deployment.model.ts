export interface DeploymentStatusDto {
  isRunning: boolean;
  pid?: number;
  port?: number;
  error?: string;
}

export interface CommandResultDto {
  success: boolean;
  output: string;
  error?: string;
  exitCode?: number;
}

export interface DeploymentResultDto {
  success: boolean;
  updateResult: CommandResultDto;
  buildResult: CommandResultDto;
  runResult: CommandResultDto;
  builtJar: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNumber?: number;
  pageSize?: number;
}

export interface DeploymentDto {
  id?: number;
  version: string;
  status: string;
  pid?: number;
  type?: string;
  createdAt?: string; // ISO 8601 format
  applicationId: number;
  branch?: string;
  logsPath?: string;
  notes?: string;
}
