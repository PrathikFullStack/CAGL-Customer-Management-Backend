# Appzillon Customer Management (`appzillon-cm`) Helm Chart

This Helm chart deploys the **Appzillon Customer Management (`AppzillonBanking-CM`)** Spring Boot microservice onto a Kubernetes cluster.

---

## 📁 Chart Directory Structure

```text
helm/appzillon-cm/
├── Chart.yaml              # Helm Chart Metadata
├── values.yaml             # Default configuration values (Overridable)
├── README.md               # DevOps instructions
└── templates/
    ├── _helpers.tpl        # Label and naming helper functions
    ├── configmap.yaml      # Environment variable ConfigMap
    ├── secret.yaml         # Database credentials Secret
    ├── deployment.yaml     # Kubernetes Deployment manifest
    ├── service.yaml        # Kubernetes ClusterIP Service
    └── ingress.yaml        # NGINX Ingress Controller routing
```

---

## 🚀 How DevOps Can Install / Upgrade

### 1. Test / Dry Run Rendering
```bash
helm template appzillon-cm ./helm/appzillon-cm --values ./helm/appzillon-cm/values.yaml
```

### 2. Install on Kubernetes Cluster
```bash
helm install appzillon-cm ./helm/appzillon-cm \
  --namespace cagl-banking \
  --create-namespace \
  --set image.repository="your-registry.domain.com/cagl/appzillon-cm" \
  --set image.tag="latest" \
  --set database.host="cagl-postgres.internal" \
  --set database.password="ProductionPassword123"
```

### 3. Upgrade Existing Deployment
```bash
helm upgrade appzillon-cm ./helm/appzillon-cm \
  --namespace cagl-banking \
  --set image.tag="v1.0.2"
```

### 4. Rollback if Needed
```bash
helm rollback appzillon-cm 1 --namespace cagl-banking
```
