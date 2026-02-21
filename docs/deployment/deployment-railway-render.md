# 🚂 Quick Deploy to Railway.app

Railway.app offers easy deployment with $5/month free credit. Perfect for testing or small-scale deployments.

## Prerequisites

- GitHub account
- Railway account (sign up at https://railway.app)
- Git repository of your project

## Option 1: Deploy via Railway Dashboard (Easiest)

### Step 1: Connect GitHub

1. Go to https://railway.app
2. Click "Start a New Project"
3. Select "Deploy from GitHub repo"
4. Authorize Railway to access your GitHub
5. Select `kally123/doctorApp` repository

### Step 2: Add Databases

1. Click "+ New" → "Database" → "Add PostgreSQL"
2. Click "+ New" → "Database" → "Add Redis"
3. Click "+ New" → "Database" → "Add MongoDB"

Railway will automatically create and configure these databases.

### Step 3: Deploy Services (One by One)

Due to Railway's pricing model, deploy essential services first:

#### Deploy API Gateway

1. Click "+ New" → "GitHub Repo" → Select `doctorApp`
2. Configure:
   - **Name**: `api-gateway`
   - **Root Directory**: `/backend/api-gateway`
   - **Builder**: Dockerfile
   - **Port**: 8080

3. Add Environment Variables:
```env
SPRING_PROFILES_ACTIVE=prod
POSTGRES_HOST=${{Postgres.RAILWAY_PRIVATE_DOMAIN}}
POSTGRES_PORT=${{Postgres.RAILWAY_TCP_PROXY_PORT}}
POSTGRES_USER=${{Postgres.PGUSER}}
POSTGRES_PASSWORD=${{Postgres.PGPASSWORD}}
REDIS_HOST=${{Redis.RAILWAY_PRIVATE_DOMAIN}}
REDIS_PORT=${{Redis.RAILWAY_TCP_PROXY_PORT}}
REDIS_PASSWORD=${{Redis.REDISPASSWORD}}
JWT_SECRET=your-super-secret-jwt-key-min-32-characters
JWT_REFRESH_SECRET=your-super-secret-refresh-key-min-32-characters
```

4. Click "Deploy"

#### Deploy User Service

1. Click "+ New" → "GitHub Repo"
2. Configure:
   - **Name**: `user-service`
   - **Root Directory**: `/backend/user-service`
   - **Builder**: Dockerfile
   - **Port**: 8081

3. Add Environment Variables (similar to API Gateway, but for user_db)

4. Click "Deploy"

#### Repeat for Other Services

Follow the same pattern for:
- doctor-service (port 8082)
- appointment-service (port 8084)
- payment-service (port 8085)
- consultation-service (port 8087)

### Step 4: Get Service URLs

Railway will provide public URLs for each service:
- `api-gateway`: `https://api-gateway-production.up.railway.app`
- `user-service`: `https://user-service-production.up.railway.app`

### Cost Estimation

With $5/month free credit:
- PostgreSQL: ~$1/month
- Redis: ~$0.50/month
- MongoDB: ~$1/month
- 3-4 Services: ~$2-3/month

**Total: Can run 3-4 services + databases within free tier**

### Limitations

- $5 credit runs out quickly with many services
- Better for **development/testing** or **MVP with 3-4 services**
- Services don't sleep (unlike Render), so always consuming credit

---

## Option 2: Deploy via Railway CLI

### Installation

```powershell
# Windows PowerShell
npm install -g @railway/cli

# Or use Scoop
scoop install railway
```

### Login

```bash
railway login
```

### Deploy

```bash
# Navigate to project
cd C:\PROJECTS\AI\doctorApp

# Initialize Railway project
railway init

# Link to existing project (if already created on dashboard)
railway link

# Add PostgreSQL
railway add --plugin postgresql

# Add Redis
railway add --plugin redis

# Deploy API Gateway
cd backend/api-gateway
railway up

# Deploy other services
cd ../user-service
railway up
```

### Configure Environment Variables via CLI

```bash
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set JWT_SECRET="your-secret-key"
railway variables set POSTGRES_PASSWORD="$(railway variables get PGPASSWORD)"
```

---

## 🎨 Quick Deploy to Render.com

Render offers 750 hours/month free per service. Services sleep after 15 minutes of inactivity.

## Option 1: Deploy via render.yaml

### Create render.yaml in project root

```yaml
# render.yaml
services:
  # API Gateway
  - type: web
    name: healthcare-api-gateway
    env: docker
    dockerfilePath: ./backend/api-gateway/Dockerfile
    dockerContext: ./backend/api-gateway
    plan: free
    healthCheckPath: /actuator/health
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: POSTGRES_URL
        fromDatabase:
          name: healthcare-postgres
          property: connectionString
      - key: REDIS_URL
        fromService:
          type: redis
          name: healthcare-redis
          property: connectionString
      - key: JWT_SECRET
        generateValue: true
      - key: JWT_REFRESH_SECRET
        generateValue: true

  # User Service
  - type: web
    name: healthcare-user-service
    env: docker
    dockerfilePath: ./backend/user-service/Dockerfile
    dockerContext: ./backend/user-service
    plan: free
    healthCheckPath: /actuator/health
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: POSTGRES_URL
        fromDatabase:
          name: healthcare-postgres
          property: connectionString
      - key: REDIS_URL
        fromService:
          type: redis
          name: healthcare-redis
          property: connectionString

  # Doctor Service
  - type: web
    name: healthcare-doctor-service
    env: docker
    dockerfilePath: ./backend/doctor-service/Dockerfile
    dockerContext: ./backend/doctor-service
    plan: free
    healthCheckPath: /actuator/health
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: POSTGRES_URL
        fromDatabase:
          name: healthcare-postgres
          property: connectionString

  # Patient Frontend
  - type: web
    name: healthcare-patient-app
    env: static
    buildCommand: cd frontend/patient-webapp && npm install && npm run build
    staticPublishPath: ./frontend/patient-webapp/out
    plan: free
    envVars:
      - key: NEXT_PUBLIC_API_URL
        value: https://healthcare-api-gateway.onrender.com

databases:
  - name: healthcare-postgres
    databaseName: healthcare
    user: healthcare_user
    plan: free

  - name: healthcare-redis
    plan: free
```

### Deploy to Render

1. Go to https://render.com
2. Sign up/Login with GitHub
3. Click "New +" → "Blueprint"
4. Connect repository
5. Select `render.yaml`
6. Click "Apply"

Render will automatically:
- Create all services
- Set up databases
- Configure environment variables
- Deploy everything

### Limitations

- **Services sleep after 15 minutes** of inactivity
- **Cold start**: 30-60 seconds when waking up
- **Free tier**: 750 hours/month per service
- **Not suitable for production** due to cold starts
- Good for **demos, testing, MVPs**

---

## Option 2: Manual Deployment on Render Dashboard

### Step 1: Create PostgreSQL Database

1. Go to Render Dashboard
2. Click "New +" → "PostgreSQL"
3. Name: `healthcare-postgres`
4. Database: `healthcare`
5. User: `healthcare_user`
6. Plan: **Free**
7. Click "Create Database"

Copy the connection strings (internal and external)

### Step 2: Create Redis

1. Click "New +" → "Redis"
2. Name: `healthcare-redis`
3. Plan: **Free**
4. Click "Create Redis"

### Step 3: Deploy API Gateway

1. Click "New +" → "Web Service"
2. Connect GitHub repository
3. Configure:
   - **Name**: `healthcare-api-gateway`
   - **Environment**: Docker
   - **Dockerfile Path**: `./backend/api-gateway/Dockerfile`
   - **Plan**: Free
   - **Health Check Path**: `/actuator/health`

4. Environment Variables:
```env
SPRING_PROFILES_ACTIVE=prod
SPRING_R2DBC_URL=r2dbc:postgresql://[HOST]:[PORT]/healthcare
SPRING_R2DBC_USERNAME=healthcare_user
SPRING_R2DBC_PASSWORD=[from database]
SPRING_REDIS_HOST=[redis-host]
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=[redis-password]
JWT_SECRET=your-secret-key-min-32-chars
JWT_REFRESH_SECRET=your-refresh-secret-min-32-chars
```

5. Click "Create Web Service"

### Step 4: Deploy Other Services

Repeat for:
- user-service
- doctor-service
- appointment-service
- payment-service

### Step 5: Deploy Frontend (Static Site)

1. Click "New +" → "Static Site"
2. Connect repository
3. Configure:
   - **Name**: `healthcare-patient-app`
   - **Root Directory**: `frontend/patient-webapp`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `out` (for Next.js)

4. Environment Variables:
```env
NEXT_PUBLIC_API_URL=https://healthcare-api-gateway.onrender.com
```

---

## 🪰 Quick Deploy to Fly.io

Fly.io offers 3 free VMs with 256MB RAM each. Good for small services.

### Installation

```powershell
# Windows PowerShell
iwr https://fly.io/install.ps1 -useb | iex
```

### Login

```bash
flyctl auth login
```

### Deploy Each Service

```bash
# Navigate to API Gateway
cd backend/api-gateway

# Create fly.toml
flyctl launch --name healthcare-gateway --no-deploy

# Edit fly.toml
# Set internal_port = 8080
# Add environment variables

# Deploy
flyctl deploy

# Repeat for other services
cd ../user-service
flyctl launch --name healthcare-user
flyctl deploy

cd ../doctor-service
flyctl launch --name healthcare-doctor
flyctl deploy
```

### Add PostgreSQL

```bash
flyctl postgres create --name healthcare-db --region iad --initial-cluster-size 1

# Attach to service
flyctl postgres attach healthcare-db --app healthcare-gateway
```

### Limitations

- **256MB RAM per VM** - Very limited
- **3 free VMs** - Can only run 3 services
- Not suitable for all 13 services
- Better for **single service testing**

---

## 📊 Comparison Summary

| Platform | Best For | Free Resources | Limitations |
|----------|----------|----------------|-------------|
| **Railway** | Quick MVP, 3-4 services | $5/month credit | Credit runs out fast |
| **Render** | Demo, Testing | 750hrs/service | Services sleep, slow cold start |
| **Fly.io** | Microservices (1-3) | 3 VMs (256MB) | Very limited RAM |

## 🎯 Recommended Approach for Free Deployment

**For complete platform:**
1. Use **Oracle Cloud** (see main deployment guide)
2. Deploy frontend to **Vercel** (Next.js) - Free
3. Deploy doctor dashboard to **Netlify** (React) - Free

**For testing/demo (3-4 services only):**
1. Use **Railway** for backend (API Gateway + User + Doctor + Appointment)
2. Use **Vercel** for frontend - Free
3. Railway databases (PostgreSQL + Redis)

**For individual service testing:**
1. Use **Fly.io** for 1-2 services
2. Use **Render** for frontend

---

## 🚀 Next Steps

After deployment:
1. Configure custom domain
2. Set up SSL (automatic on Railway/Render)
3. Monitor usage and costs
4. Set up health checks
5. Configure CI/CD

---

## 📞 Support

- Railway: https://railway.app/help
- Render: https://render.com/docs
- Fly.io: https://fly.io/docs

---

**Last Updated**: February 21, 2026

