# CAGL-Customer-Management-Backend — Free Deployment & CI/CD Guide

Repository: [vivekratnala/CAGL-Customer-Management-Backend](https://github.com/vivekratnala/CAGL-Customer-Management-Backend) (Branch: `main`)

This repository is a multi-module Spring Boot 3 / Java 17 microservices project.

---

## 🏗️ Free Architecture Setup

| Component | Free Tool | Details |
|---|---|---|
| **Hosting Platform** | [Render.com](https://render.com) or [Koyeb](https://www.koyeb.com) | Free Web Service with automated Deploy Hooks |
| **Container Registry** | [Docker Hub](https://hub.docker.com) | Free personal account (stores container images) |
| **CI / CD Pipeline** | **Jenkins** (or GitHub Actions) | Builds multi-module Maven libs, packages service, creates Docker image, pushes to Docker Hub, and triggers cloud deploy |

---

## 📦 Project Microservices

The repository contains microservices under `apz_java_microservices/`:
- `AppzillonBanking-CM` *(Customer Management)*
- `AppzillonBanking-CAGL` *(CDH Data Hub)*
- `AppzillonBanking-KYC`, `AppzillonBanking-LOAN`, `AppzillonBanking-DOCUMENT`, `AppzillonBanking-CBS`, etc.

**Shared Core Library**:
- `dependencies-lib/` (Must be installed via `mvn clean install` prior to service compilation)

---

## 🚀 Quick Setup Instructions

### 1. Free Docker Hub Setup
1. Sign up at [hub.docker.com](https://hub.docker.com).
2. Create a repository: `cagl-backend`.
3. Generate an Access Token under **Account Settings > Security**.

### 2. Free Cloud Setup (Render.com)
1. Sign up at [Render.com](https://render.com) (Free).
2. Click **New +** > **Web Service** > **Deploy an existing image**.
3. Image URL: `docker.io/<your-dockerhub-username>/cagl-backend:latest`
4. Select **Free** instance.
5. In Environment Variables, add your database credentials and `PORT=8080`.
6. Under Settings, copy the **Deploy Hook URL** (e.g. `https://api.render.com/deploy/srv-xxxx?key=yyyy`).

### 3. Jenkins Configuration
1. In Jenkins (`Manage Jenkins > Credentials`), add:
   - `docker-hub-credentials` (Username with password: Docker Hub username & token).
   - `render-deploy-hook-url` (Secret text: Render deploy hook URL).
2. Create a Pipeline Job linked to repository `https://github.com/vivekratnala/CAGL-Customer-Management-Backend.git` (Branch: `main`) using the [`Jenkinsfile`](./Jenkinsfile).
3. Under Build Triggers, enable **GitHub hook trigger for GITScm polling**.

### 4. Git Auto-Trigger (Webhook)
In your GitHub repo:
- Go to **Settings > Webhooks > Add webhook**.
- URL: `http://<your-jenkins-host>/github-webhook/`.
- Event: `Pushes`.

On every `git push` to `main`, Jenkins will automatically build the code, push the Docker image, and trigger Render to redeploy your Spring Boot backend!
