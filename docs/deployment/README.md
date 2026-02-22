# 📚 Deployment Documentation

This folder contains comprehensive guides for deploying the Healthcare Platform to various cloud providers.

---

## 📖 Documentation Files

### 🚀 [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - **START HERE**
**Complete deployment guide** with step-by-step instructions for deploying the entire platform.

**What's Inside**:
- Recommended architecture (100% FREE)
- Complete 3-phase deployment process
- Cost breakdown ($0/month forever)
- Resource allocation guide
- Quick start commands
- Deployment checklist
- Success criteria

**Best For**: Complete production deployment

---

### ☁️ [deployment-free-cloud.md](deployment-free-cloud.md)
**Detailed guide for 7 free cloud providers** with pros/cons and step-by-step instructions.

**Providers Covered**:
1. ⭐ **Oracle Cloud (Always Free)** - RECOMMENDED
   - 4 ARM VMs (24GB RAM total)
   - Forever free, no time limit
   - Can run all 13 services
2. **Google Cloud Platform (GCP)**
3. **AWS Free Tier**
4. **Azure for Students**
5. **Railway.app**
6. **Render.com**
7. **Fly.io**
8. **Hybrid Approach** (combining multiple providers)

**Best For**: Choosing the right cloud provider

---

### 🚂 [deployment-railway-render.md](deployment-railway-render.md)
**Quick deployment guides** for developer-friendly platforms.

**What's Inside**:
- Railway.app deployment (dashboard + CLI)
- Render.com deployment (render.yaml + dashboard)
- Fly.io deployment (flyctl)
- Cost comparisons
- Limitations of each platform

**Best For**: Quick MVPs, demos, testing

---

### 🎨 [frontend-deployment.md](frontend-deployment.md)
**Complete frontend deployment guide** for Vercel and Netlify.

**What's Inside**:
- Patient Web App (Next.js) → Vercel
- Doctor Dashboard (React/Vite) → Netlify
- Environment variables configuration
- Custom domains setup
- CI/CD with GitHub Actions
- Performance optimization

**Best For**: Deploying frontend applications

---

### 🐋 [docker-optimization.md](docker-optimization.md)
**Docker image optimization guide** for resource-constrained environments.

**What's Inside**:
- Multi-stage Docker builds (70% size reduction)
- JVM heap optimization for containers
- Spring Boot configuration for low memory
- Production docker-compose.yaml
- Resource allocation strategies
- Monitoring resource usage

**Results**: 
- 500MB → 150MB images
- 768MB → 384MB RAM usage
- 90s → 45s startup time

**Best For**: Optimizing for free tier limitations

---

### 📋 [QUICK_DEPLOY.md](QUICK_DEPLOY.md)
**Quick reference card** - Print this and keep it handy!

**What's Inside**:
- One-page cheat sheet
- Copy-paste commands
- Environment variables template
- Common troubleshooting
- Quick fixes

**Best For**: Quick reference during deployment

---

## 🎯 Which Guide Should I Use?

### I want to deploy the complete platform for production
👉 Start with **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)**
- Follow Oracle Cloud deployment
- 30-40 minutes total
- $0/month forever

### I want to compare different cloud providers
👉 Read **[deployment-free-cloud.md](deployment-free-cloud.md)**
- Detailed comparison of 7 providers
- Pros and cons for each
- Cost analysis

### I want a quick MVP/demo (3-4 services only)
👉 Use **[deployment-railway-render.md](deployment-railway-render.md)**
- Railway.app section
- 15 minutes deployment
- $5/month credit

### I want to deploy just the frontends
👉 Follow **[frontend-deployment.md](frontend-deployment.md)**
- Vercel for Patient App
- Netlify for Doctor Dashboard
- 10 minutes total

### I need to optimize Docker images
👉 Read **[docker-optimization.md](docker-optimization.md)**
- Reduce image sizes
- Lower RAM usage
- Faster startup

### I need quick commands during deployment
👉 Keep **[QUICK_DEPLOY.md](QUICK_DEPLOY.md)** open
- All commands in one place
- Quick troubleshooting
- Environment variables template

---

## 🚀 Quick Start

### Complete Free Deployment (RECOMMENDED)

```bash
# 1. Clone repository
git clone https://github.com/kally123/doctorApp.git
cd doctorApp

# 2. Deploy backend (30 minutes)
chmod +x scripts/deploy-oracle-cloud.sh
./scripts/deploy-oracle-cloud.sh

# 3. Deploy patient app (5 minutes)
cd frontend/patient-webapp
vercel --prod

# 4. Deploy doctor dashboard (5 minutes)
cd ../doctor-dashboard
netlify deploy --prod
```

**Total Time**: 40 minutes  
**Total Cost**: $0/month forever

---

## 💰 Cost Comparison

| Deployment Option | Monthly Cost | Services | Time |
|------------------|--------------|----------|------|
| **Oracle Cloud** | $0 forever | All 13 + 2 frontends | 40 min |
| **Railway.app** | $5 credit | 3-4 services | 15 min |
| **Render.com** | $0 (with sleep) | 3-5 services | 20 min |
| **Fly.io** | $0 | 1-3 services | 10 min |
| **Hybrid** | $0 forever | All + frontends | 45 min |

---

## 📊 What Gets Deployed

### Backend Services (13)
1. API Gateway (port 8080)
2. User Service (8081)
3. Doctor Service (8082)
4. Search Service (8083)
5. Appointment Service (8084)
6. Payment Service (8085)
7. Notification Service (8086)
8. Consultation Service (8087)
9. Prescription Service (8088)
10. EHR Service (8089)
11. Order Service (8090)
12. Review Service (8091)
13. Content Service (8092)

### Frontend Applications (2)
1. Patient Web App (Next.js)
2. Doctor Dashboard (React/Vite)

### Infrastructure
- PostgreSQL (9 databases)
- MongoDB (3 databases)
- Redis (caching)
- Kafka + Zookeeper (events)
- Elasticsearch (search)
- LocalStack (S3 simulation)
- MailHog (email testing)

---

## ✅ Deployment Checklist

### Before Starting
- [ ] Read [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
- [ ] Choose your cloud provider
- [ ] Create account (Oracle Cloud recommended)
- [ ] Prepare environment variables
- [ ] Have Razorpay keys ready (payments)
- [ ] Have Twilio keys ready (video)

### During Deployment
- [ ] Run deployment script
- [ ] Verify all services running
- [ ] Deploy frontends
- [ ] Configure environment variables
- [ ] Test health endpoints

### After Deployment
- [ ] Configure CORS
- [ ] Set up monitoring
- [ ] Configure custom domain (optional)
- [ ] Enable CDN (Cloudflare)
- [ ] Test all user flows

---

## 🆘 Need Help?

1. **Check the guides** - Each file has troubleshooting sections
2. **View logs** - Commands in [QUICK_DEPLOY.md](QUICK_DEPLOY.md)
3. **FAQ** - See [main README.md](../../README.md#faq)
4. **Open an issue** - [GitHub Issues](https://github.com/kally123/doctorApp/issues)

---

## 📞 Additional Resources

- **Main README**: [../../README.md](../../README.md)
- **Architecture Guide**: [../../ARCHITECTURE_INSTRUCTIONS.md](../ARCHITECTURE_INSTRUCTIONS.md)
- **Project Plan**: [../../PROJECT_PLAN.md](../PROJECT_PLAN.md)
- **Deployment Script**: [../../scripts/deploy-oracle-cloud.sh](../../scripts/deploy-oracle-cloud.sh)

---

## 🎉 Success Stories

After following these guides, you'll have:
- ✅ 13 microservices running
- ✅ 2 web applications live
- ✅ Multiple databases operational
- ✅ Event streaming configured
- ✅ Search engine running
- ✅ **All for $0/month!**

---

**Last Updated**: February 21, 2026

**Found an issue?** Please report it: https://github.com/kally123/doctorApp/issues

