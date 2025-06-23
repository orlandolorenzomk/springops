# Application API

## ApplicationController.java

| Method | Path | Function |
|--------|------|----------|
| GET | `/applications/{id}` | `findById` |
| GET | `/applications/` | `findAll` |
| POST | `/applications/` | `save` |
| PUT | `/applications/{id}` | `update` |
| DELETE | `/applications/{id}` | `deleteById` |
| POST | `/applications/{applicationId}/dependencies` | `updateDependencies` |
| GET | `/applications/{applicationId}/dependencies` | `getDependencies` |