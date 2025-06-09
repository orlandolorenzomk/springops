export interface AuthenticationResponse {
  token: string;
  expiration: string; // in minutes
  userId: string;
  email: string;
}
