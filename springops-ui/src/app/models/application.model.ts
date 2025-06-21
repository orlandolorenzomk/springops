export interface Application {
  id: number;
  name: string;
  folderRoot: string;
  description: string;
  createdAt: Date;
  mvnSystemVersionId: number | null;
  javaSystemVersionId: number | null;
  gitProjectHttpsUrl: string;
  gitProjectSshUrl: string;
}

export interface ApplicationDto {
  id: number;
  name: string;
  folderRoot: string;
  description: string;
  createdAt: Date;
  mvnSystemVersionId: number | null;
  javaSystemVersionId: number | null;
  gitProjectHttpsUrl: string;
  gitProjectSshUrl: string;
  port: number;
  javaMinimumMemory?: string;
  javaMaximumMemory?: string;
}
