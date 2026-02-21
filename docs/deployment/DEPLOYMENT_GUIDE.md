# 🚀 Complete Deployment Guide - Healthcare Platform

**Quick reference for deploying the entire Healthcare Platform to free cloud services**

---

## 🎯 Recommended Deployment Architecture (100% FREE)

```
┌─────────────────────────────────────────────────────────────┐
│                    USERS (Patients & Doctors)                │
└─────────────────────┬───────────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        │                           │
        ▼                           ▼
┌───────────────┐          ┌──────────────────┐
│  Cloudflare   │          │   Cloudflare     │
│  (FREE CDN)   │          │   (FREE CDN)     │
└───────┬───────┘          └────────┬─────────┘
        │                           │
        ▼                           ▼
┌───────────────┐          ┌──────────────────┐
│    Vercel     │          │     Netlify      │
│ (Patient App) │          │ (Doctor Portal)  │
│    FREE       │          │      FREE        │
└───────┬───────┘          └────────┬─────────┘
        │                           │
        └───────────┬───────────────┘
                    │ API Calls
                    ▼
           ┌────────────────┐
           │  Cloudflare    │
           │  (FREE CDN)    │
           └────────┬───────┘
                    │
                    ▼
        ┌───────────────────────┐
        │   Oracle Cloud        │
        │   Always Free Tier    │
        │                       │
        │  ┌─────────────────┐  │
        │  │  Load Balancer  │  │
        │  └────────┬────────┘  │
        │           │           │
        │  ┌────────┴────────┐  │
        │  │                 │  │
        │  ▼                 ▼  │
        │ VM-1 (12GB)    VM-2 (12GB) │
        │                       │
        │ • Infrastructure      │
        │ • Core Services       │
        │ • All 13 Services     │
        │ • Databases           │
        └───────────────────────┘
              100% FREE
```

---

## 📋 Complete Deployment Steps

### Phase 1: Backend Deployment (Oracle Cloud)

**Time Required**: 30-40 minutes

1. **Create Oracle Cloud Account**
   ```
   Visit: https://www.oracle.com/cloud/free/
   Sign up for Always Free tier
   No credit card required initially
   ```

2. **Run Automated Deployment**
   ```bash
   git clone https://github.com/kally123/doctorApp.git
   cd doctorApp
   chmod +x scripts/deploy-oracle-cloud.sh
   ./scripts/deploy-oracle-cloud.sh
   ```

3. **Note Your API URL**
   ```
   API Gateway will be at: http://YOUR_VM_IP:8080
   ```

**Result**: All 13 backend services running on Oracle Cloud (FREE forever)

📖 **Detailed Guide**: [deployment-free-cloud.md](deployment-free-cloud.md)

---

### Phase 2: Frontend Deployment

#### 2A: Patient Web App (Vercel)

**Time Required**: 5-10 minutes

1. **Deploy to Vercel**
   ```bash
   cd frontend/patient-webapp
   npm install -g vercel
   vercel login
   vercel --prod
   ```

2. **Configure Environment Variables**
   ```env
   NEXT_PUBLIC_API_URL=http://YOUR_ORACLE_VM_IP:8080
   NEXT_PUBLIC_RAZORPAY_KEY_ID=rzp_test_xxxxx
   ```

3. **Note Your URL**
   ```
   Patient App: https://your-app.vercel.app
   ```

**Result**: Patient web app live on Vercel (FREE forever)

#### 2B: Doctor Dashboard (Netlify)

**Time Required**: 5-10 minutes

1. **Deploy to Netlify**
   ```bash
   cd frontend/doctor-dashboard
   npm install -g netlify-cli
   netlify login
   npm run build
   netlify deploy --prod
   ```

2. **Configure Environment Variables**
   ```env
   VITE_API_URL=http://YOUR_ORACLE_VM_IP:8080
   VITE_RAZORPAY_KEY_ID=rzp_test_xxxxx
   ```

3. **Note Your URL**
   ```
   Doctor Dashboard: https://your-app.netlify.app
   ```

**Result**: Doctor dashboard live on Netlify (FREE forever)

📖 **Detailed Guide**: [frontend-deployment.md](frontend-deployment.md)

---

### Phase 3: Domain & SSL (Optional but Recommended)

**Time Required**: 15-20 minutes

1. **Get Free Domain** (Optional)
   ```
   Option 1: Freenom.com (.tk, .ml, .ga) - FREE
   Option 2: Use Vercel/Netlify subdomains - FREE
   Option 3: Use your own domain
   ```

2. **Configure DNS with Cloudflare** (FREE CDN + SSL)
   ```
   1. Sign up at cloudflare.com
   2. Add your domain
   3. Update nameservers
   4. Add DNS records:
      - patients.yourdomain.com → Vercel CNAME
      - doctors.yourdomain.com → Netlify CNAME
      - api.yourdomain.com → Oracle Cloud IP
   5. Enable SSL/TLS (Full)
   6. Enable CDN caching
   ```

**Result**: Custom domain with free SSL and CDN

---

## 💰 Total Cost Breakdown

| Component | Provider | Monthly Cost | Forever? |
|-----------|----------|--------------|----------|
| **Backend (13 services)** | Oracle Cloud | $0 | ✅ Yes |
| **Patient Web App** | Vercel | $0 | ✅ Yes |
| **Doctor Dashboard** | Netlify | $0 | ✅ Yes |
| **CDN + SSL** | Cloudflare | $0 | ✅ Yes |
| **Domain** | Freenom | $0 | ⚠️ Renew yearly |
| **Databases** | Self-hosted on Oracle | $0 | ✅ Yes |
| **Total** | - | **$0** | ✅ **Forever Free** |

---

## 🎯 Alternative Deployment Options

### Option A: Minimal Setup (3-4 Services Only)

**Use Case**: Quick demo, MVP, testing

**Platform**: Railway.app

**Cost**: $5/month credit (FREE)

```bash
# Deploy to Railway
cd backend/api-gateway
railway login
railway up

# Repeat for user-service, doctor-service, appointment-service
```

**Limitations**: 
- Only 3-4 services fit in $5 credit
- Not suitable for full platform

📖 **Guide**: [deployment-railway-render.md](deployment-railway-render.md)

---

### Option B: Individual Service Testing

**Use Case**: Testing individual microservices

**Platform**: Render.com or Fly.io

```bash
# Deploy to Render
# Via dashboard: connect GitHub repo, select service folder

# Or deploy to Fly.io
cd backend/user-service
flyctl launch
flyctl deploy
```

**Limitations**:
- Services sleep after inactivity (Render)
- Very limited RAM (Fly.io - 256MB)

---

### Option C: Hybrid Approach (Recommended for Production)

**Best balance of performance and cost**

```
Backend:        Oracle Cloud (Always Free)
Patient App:    Vercel (Free)
Doctor Portal:  Netlify (Free)
MongoDB:        MongoDB Atlas (Free 512MB)
Redis:          Redis Cloud (Free 30MB)
CDN:            Cloudflare (Free)
SSL:            Let's Encrypt (Free via Cloudflare)
Monitoring:     UptimeRobot (Free)
Logs:           Better Stack (Free tier)
```

**Total Cost**: $0/month

---

## 🔧 Post-Deployment Configuration

### 1. Update CORS Settings

Update backend API Gateway to allow frontend domains:

```yaml
# backend/api-gateway/src/main/resources/application.yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "https://your-app.vercel.app"
              - "https://your-app.netlify.app"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```

### 2. Configure Environment Variables

**Backend** (Oracle Cloud):
```bash
# SSH into VM
ssh ubuntu@YOUR_ORACLE_VM_IP

# Update .env file
cd ~/healthcare-app
nano .env

# Update these:
POSTGRES_PASSWORD=your_secure_password
JWT_SECRET=your_production_secret_min_32_chars
RAZORPAY_KEY_ID=your_razorpay_key
TWILIO_ACCOUNT_SID=your_twilio_sid

# Restart services
docker-compose restart
```

**Frontend** (Vercel/Netlify):
- Update via dashboard: Settings → Environment Variables

### 3. Set Up Monitoring

**Free Monitoring Tools**:

1. **UptimeRobot** (uptime monitoring)
   ```
   Monitor: http://YOUR_API_URL/actuator/health
   Alert: When down
   ```

2. **Better Stack** (formerly Logtail - log management)
   ```
   Integrate with Docker logs
   Free tier: 1GB/month
   ```

3. **Sentry** (error tracking)
   ```
   Free tier: 5K errors/month
   Add to frontends
   ```

---

## 📊 Resource Allocation (Oracle Cloud)

### VM-1 (12 GB RAM, 2 OCPUs)

```
Infrastructure Services:
├── PostgreSQL:      2 GB
├── MongoDB:         1.5 GB
├── Redis:           512 MB
├── Kafka:           1 GB
├── Zookeeper:       512 MB
├── Elasticsearch:   1 GB
└── LocalStack:      256 MB

Application Services:
├── API Gateway:     512 MB
├── User Service:    512 MB
├── Doctor Service:  512 MB
├── Search Service:  512 MB
└── Appointment:     512 MB

Total: ~11 GB (1 GB buffer)
```

### VM-2 (12 GB RAM, 2 OCPUs)

```
Application Services:
├── Payment:         512 MB
├── Notification:    512 MB
├── Consultation:    1 GB
├── Prescription:    512 MB
├── EHR:            1 GB
├── Order:          512 MB
├── Review:         512 MB
└── Content:        512 MB

Total: ~5.5 GB (6.5 GB buffer for scaling)
```

---

## 🚀 Quick Start Commands

### Complete Deployment (One Command)

```bash
# Clone repository
git clone https://github.com/kally123/doctorApp.git
cd doctorApp

# Deploy everything
./scripts/deploy-everything.sh

# This script will:
# 1. Deploy backend to Oracle Cloud
# 2. Deploy patient app to Vercel
# 3. Deploy doctor dashboard to Netlify
# 4. Configure environment variables
# 5. Test all endpoints
```

### Manual Deployment (Step by Step)

```bash
# 1. Backend (Oracle Cloud)
./scripts/deploy-oracle-cloud.sh

# 2. Patient App (Vercel)
cd frontend/patient-webapp
vercel --prod

# 3. Doctor Dashboard (Netlify)
cd frontend/doctor-dashboard
netlify deploy --prod

# 4. Verify
curl http://YOUR_API_URL/actuator/health
```

---

## ✅ Deployment Checklist

### Pre-Deployment

- [ ] Oracle Cloud account created
- [ ] GitHub repository cloned
- [ ] SSH keys generated
- [ ] Environment variables prepared
- [ ] Payment gateway keys obtained (Razorpay)
- [ ] Video SDK keys obtained (Twilio)

### During Deployment

- [ ] Backend deployed to Oracle Cloud
- [ ] All 13 services running
- [ ] Databases initialized
- [ ] Health checks passing
- [ ] Patient app deployed to Vercel
- [ ] Doctor dashboard deployed to Netlify
- [ ] Environment variables configured

### Post-Deployment

- [ ] CORS configured
- [ ] Custom domain added (optional)
- [ ] SSL certificate active
- [ ] CDN enabled (Cloudflare)
- [ ] Monitoring set up
- [ ] Error tracking configured
- [ ] Backup strategy in place
- [ ] Documentation updated with URLs

---

## 🎯 Success Criteria

After deployment, you should have:

✅ **Backend API** running on Oracle Cloud
   - All 13 services operational
   - Health endpoint responding
   - API Gateway accessible

✅ **Patient Web App** on Vercel
   - User registration working
   - Doctor search functional
   - Appointment booking working

✅ **Doctor Dashboard** on Netlify
   - Doctor login working
   - Dashboard displaying data
   - Appointment management functional

✅ **All Free** - $0/month cost

✅ **Production Ready** - Can handle real users

---

## 📞 Get Help

If you encounter issues:

1. **Check documentation**: 
   - [Deployment Guide](deployment-free-cloud.md)
   - [Troubleshooting](../README.md#troubleshooting)
   - [FAQ](../README.md#faq)

2. **View logs**:
   ```bash
   # Backend
   ssh ubuntu@YOUR_VM_IP
   cd ~/healthcare-app
   docker-compose logs -f

   # Vercel
   vercel logs

   # Netlify
   netlify logs
   ```

3. **Open an issue**:
   https://github.com/kally123/doctorApp/issues

---

## 🎉 Congratulations!

You've successfully deployed a complete healthcare platform with:
- 13 microservices
- 2 web applications
- Multiple databases
- Event streaming
- Search engine
- **All for FREE!**

---

**Next Steps**:
1. Configure custom domain
2. Set up monitoring and alerts
3. Add more features
4. Scale as needed (Oracle Cloud supports up to 4 VMs free)
5. Deploy mobile apps (coming soon)

---

**Last Updated**: February 21, 2026

