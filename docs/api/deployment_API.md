# Deployment API

## DeploymentManagerController.java

| Method | Path | Function |
|--------|------|----------|
| GET | `/deployment-manager/status` | `getDeploymentStatus` |
| POST | `/deployment-manager/kill` | `killApplication` |
| POST | `/deployment-manager/deploy` | `deployApplication` |

## DeploymentController.java

| Method | Path | Function |
|--------|------|----------|
| GET | `/deployments/{id}` | `findById` |
| GET | `/deployments/` | `findAll` |
| POST | `/deployments/` | `save` |
| PUT | `/deployments/` | `update` |
| DELETE | `/deployments/{id}` | `deleteById` |
| GET | `/deployments/search` | `searchDeployments` |
| GET | `/deployments/logs` | `downloadLog` |