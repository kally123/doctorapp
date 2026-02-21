# 🚀 Quick Deployment Reference Card

**Print this or keep it handy while deploying!**

---

## 🎯 Option 1: Complete Free Deployment (RECOMMENDED)

### Time: 40 minutes | Cost: $0/month forever

```bash
# Step 1: Clone (2 min)
git clone https://github.com/kally123/doctorApp.git
cd doctorApp

# Step 2: Deploy Backend to Oracle Cloud (30 min)
chmod +x scripts/deploy-oracle-cloud.sh
./scripts/deploy-oracle-cloud.sh

# Step 3: Deploy Patient App to Vercel (5 min)
cd frontend/patient-webapp
npm install -g vercel
vercel login
vercel --prod

# Step 4: Deploy Doctor Dashboard to Netlify (5 min)
cd ../doctor-dashboard
npm install -g netlify-cli
netlify login
npm run build
netlify deploy --prod
```

**Result**: All 13 services + 2 frontends running for FREE

📖 **Full Guide**: [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)

---

## 🎯 Option 2: Quick MVP (Railway)

### Time: 15 minutes | Cost: $5 credit/month

```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Deploy services
cd backend/api-gateway
railway up

cd ../user-service
railway up

cd ../doctor-service
railway up

# Add databases via Railway dashboard
# PostgreSQL, Redis, MongoDB
```

📖 **Full Guide**: [deployment-railway-render.md](deployment-railway-render.md)

---

## 🎯 Option 3: Render (Demo)

### Time: 20 minutes | Cost: $0 (with limitations)

```bash
# Create render.yaml in project root
# Connect GitHub repo in Render dashboard
# Select render.yaml
# Click "Apply"

# Services will auto-deploy
# Warning: Sleep after 15 min inactivity
```

📖 **Full Guide**: [deployment-railway-render.md](deployment-railway-render.md)

---

## 📋 Pre-Deployment Checklist

Before starting, have ready:

- [ ] GitHub account
- [ ] Cloud provider account (Oracle/Railway/Render)
- [ ] Razorpay keys (for payments) - get from razorpay.com
- [ ] Twilio keys (for video) - get from twilio.com
- [ ] Strong passwords for databases
- [ ] JWT secret keys (min 32 characters)

---

## 🔑 Environment Variables Template

Copy and fill these:

```bash
# Database Passwords (create strong ones)
POSTGRES_PASSWORD=___________________________
REDIS_PASSWORD=___________________________
MONGO_PASSWORD=___________________________

# JWT Secrets (min 32 chars each)
JWT_SECRET=___________________________
JWT_REFRESH_SECRET=___________________________

# Payment Gateway (Razorpay)
RAZORPAY_KEY_ID=___________________________
RAZORPAY_KEY_SECRET=___________________________

# Video Consultation (Twilio)
TWILIO_ACCOUNT_SID=___________________________
TWILIO_API_KEY_SID=___________________________
TWILIO_API_KEY_SECRET=___________________________
```

---

## 🏗️ What Gets Deployed

### Backend (Oracle Cloud)
- ✅ API Gateway (port 8080)
- ✅ 12 Microservices (ports 8081-8092)
- ✅ PostgreSQL (9 databases)
- ✅ MongoDB (3 databases)
- ✅ Redis (caching)
- ✅ Kafka (events)
- ✅ Elasticsearch (search)

### Frontend (Vercel + Netlify)
- ✅ Patient Web App (Next.js)
- ✅ Doctor Dashboard (React)

### Total Services: 13 backend + 2 frontend = 15

---

## 🌐 Access URLs (After Deployment)

```
API Gateway:      http://YOUR_ORACLE_VM_IP:8080
Patient App:      https://your-app.vercel.app
Doctor Dashboard: https://your-app.netlify.app
```

### Health Check

```bash
curl http://YOUR_ORACLE_VM_IP:8080/actuator/health
```

Should return: `{"status":"UP"}`

---

## 🔧 Common Commands

### View Logs (Oracle Cloud)
```bash
ssh ubuntu@YOUR_ORACLE_VM_IP
cd ~/healthcare-app
docker-compose logs -f
```

### Restart Services
```bash
docker-compose restart
```

### Check Service Status
```bash
docker-compose ps
```

### Update Services
```bash
git pull
docker-compose up -d --build
```

---

## 💰 Cost Summary

| Component | Provider | Cost |
|-----------|----------|------|
| 13 Backend Services | Oracle Cloud | $0 |
| Patient Web App | Vercel | $0 |
| Doctor Dashboard | Netlify | $0 |
| CDN + SSL | Cloudflare | $0 |
| **TOTAL** | | **$0/month** |

---

## 🆘 Troubleshooting Quick Fixes

### Services Won't Start
```bash
# Check logs
docker-compose logs service-name

# Restart
docker-compose restart service-name

# Rebuild
docker-compose up -d --build service-name
```

### Port Already in Use
```powershell
# Windows: Find process
netstat -ano | findstr :8080

# Kill process
taskkill /PID [PID] /F
```

### Database Connection Failed
```bash
# Check if running
docker-compose ps postgres

# Restart database
docker-compose restart postgres

# Check logs
docker-compose logs postgres
```

### Out of Memory
```bash
# Check memory usage
docker stats

# Reduce service memory
# Edit docker-compose.yaml
# Add under service:
#   deploy:
#     resources:
#       limits:
#         memory: 512M
```

---

## 📞 Get Help

1. **Check Docs**: [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
2. **FAQ**: [README.md#faq](../README.md#faq)
3. **Issues**: https://github.com/kally123/doctorApp/issues

---

## ⏱️ Deployment Time Breakdown

| Task | Time |
|------|------|
| Create Oracle Cloud account | 5 min |
| Configure OCI CLI | 5 min |
| Run deployment script | 20 min |
| Deploy frontends | 10 min |
| Configure & test | 5 min |
| **Total** | **45 min** |

---

## ✅ Success Checklist

After deployment, verify:

- [ ] Backend API responds to health check
- [ ] All 13 services show as "Up" in `docker ps`
- [ ] Patient app loads in browser
- [ ] Doctor dashboard loads in browser
- [ ] User registration works
- [ ] Doctor search works
- [ ] Can create appointment
- [ ] All URLs saved for reference

---

## 🎯 Next Steps After Deployment

1. **Set up monitoring** (UptimeRobot - free)
2. **Configure custom domain** (optional)
3. **Enable Cloudflare CDN** (free)
4. **Set up error tracking** (Sentry - free tier)
5. **Configure backups** (important!)
6. **Add SSL certificate** (Let's Encrypt via Cloudflare)
7. **Test all user flows**
8. **Invite beta users**

---

## 📱 Mobile Apps (Coming Soon)

Watch for deployment guides for:
- React Native apps
- iOS App Store deployment
- Google Play Store deployment

---

**Print this card and keep it handy during deployment!**

**Version**: 1.0 | **Last Updated**: February 21, 2026

