# Application API

Base Path: `/applications`

| Method | Path              | Description                   | Request Body       | Response Body       |
|--------|-------------------|-------------------------------|--------------------|---------------------|
| GET    | `/applications`   | List all applications         | —                  | `List<ApplicationDto>` |
| GET    | `/applications/{id}` | Get application by ID      | —                  | `ApplicationDto`    |
| POST   | `/applications`   | Create a new application      | `ApplicationDto`   | `ApplicationDto`    |
| PUT    | `/applications/{id}` | Update existing application | `ApplicationDto`   | `ApplicationDto`    |
| DELETE | `/applications/{id}` | Delete application by ID    | —                  | 204 No Content      |