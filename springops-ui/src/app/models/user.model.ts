export interface User {
    id: string;
    username: string;
    email: string;
    createdAt: Date;
    updatedAt: Date;
}

export interface UserCreationDto {
    username: string;
    email: string;
    password: string;
}

export interface UserCacheEntry {
    users: User[];
    timestamp: number;
}