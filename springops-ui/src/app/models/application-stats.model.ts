/**
 * Represents resource usage statistics reported by the backend.
 */
export interface ApplicationStats {
  memoryMb: number;     // Resident memory usage in MB
  cpuLoad: number;      // CPU usage (percentage, 0–100)
  availMemMb: number;   // Available system memory in MB
  timestamp: string;    // ISO‑8601 string representing the sample time
}
