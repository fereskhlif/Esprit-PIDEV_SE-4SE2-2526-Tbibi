export interface User {
  userId: number;
  name: string;
  email: string;
  adresse: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  adresse: string;
}
