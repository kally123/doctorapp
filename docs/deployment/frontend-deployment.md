# 🎨 Frontend Deployment Guide

Deploy the patient web app and doctor dashboard to free hosting platforms.

---

## 📋 Table of Contents

- [Patient Web App (Next.js) → Vercel](#patient-web-app-nextjs--vercel)
- [Doctor Dashboard (React/Vite) → Netlify](#doctor-dashboard-reactvite--netlify)
- [Environment Variables](#environment-variables)
- [Custom Domains](#custom-domains)
- [CI/CD Setup](#cicd-setup)

---

## 🌐 Patient Web App (Next.js) → Vercel

Vercel is the creators of Next.js and offers the best hosting for Next.js apps.

### Free Tier Includes

- ✅ Unlimited deployments
- ✅ 100 GB bandwidth per month
- ✅ Automatic SSL
- ✅ CDN (Edge Network)
- ✅ Preview deployments for PRs
- ✅ Custom domains

### Option 1: Deploy via Vercel Dashboard (Easiest)

#### Step 1: Sign Up

1. Go to https://vercel.com
2. Sign up with GitHub account
3. Authorize Vercel to access your repositories

#### Step 2: Import Project

1. Click "Add New..." → "Project"
2. Import `kally123/doctorApp` repository
3. Vercel will detect it's a monorepo

#### Step 3: Configure

- **Framework Preset**: Next.js
- **Root Directory**: `frontend/patient-webapp`
- **Build Command**: `npm run build`
- **Output Directory**: `.next` (auto-detected)
- **Install Command**: `npm install`

#### Step 4: Environment Variables

Add these in Vercel dashboard:

```env
NEXT_PUBLIC_API_URL=https://your-api-gateway.com
NEXT_PUBLIC_RAZORPAY_KEY_ID=rzp_test_xxxxx
NEXT_PUBLIC_TWILIO_ACCOUNT_SID=ACxxxxx
NEXT_PUBLIC_APP_ENV=production
```

#### Step 5: Deploy

1. Click "Deploy"
2. Wait 2-3 minutes
3. Your app will be live at `https://your-app.vercel.app`

### Option 2: Deploy via Vercel CLI

```bash
# Install Vercel CLI
npm install -g vercel

# Login
vercel login

# Navigate to patient webapp
cd frontend/patient-webapp

# Deploy
vercel

# Follow prompts:
# - Link to existing project? No
# - Project name: healthcare-patient-app
# - Directory: ./
# - Override settings? No

# Deploy to production
vercel --prod
```

### Configure for Production

Create `vercel.json` in `frontend/patient-webapp/`:

```json
{
  "version": 2,
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/next"
    }
  ],
  "env": {
    "NEXT_PUBLIC_API_URL": "https://api.yourdomain.com"
  },
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        {
          "key": "X-Content-Type-Options",
          "value": "nosniff"
        },
        {
          "key": "X-Frame-Options",
          "value": "DENY"
        },
        {
          "key": "X-XSS-Protection",
          "value": "1; mode=block"
        }
      ]
    }
  ],
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://api.yourdomain.com/api/:path*"
    }
  ]
}
```

### Custom Domain

1. Go to Project Settings → Domains
2. Add your domain (e.g., `patients.yourdomain.com`)
3. Update DNS records as instructed
4. SSL is automatic

---

## 🎨 Doctor Dashboard (React/Vite) → Netlify

Netlify offers excellent hosting for static sites and SPAs.

### Free Tier Includes

- ✅ 100 GB bandwidth per month
- ✅ 300 build minutes per month
- ✅ Automatic SSL
- ✅ CDN (Edge Network)
- ✅ Preview deployments
- ✅ Custom domains
- ✅ Form handling
- ✅ Serverless functions (limited)

### Option 1: Deploy via Netlify Dashboard (Easiest)

#### Step 1: Sign Up

1. Go to https://www.netlify.com
2. Sign up with GitHub account
3. Authorize Netlify

#### Step 2: Add New Site

1. Click "Add new site" → "Import an existing project"
2. Choose GitHub
3. Select `kally123/doctorApp` repository
4. Grant permissions

#### Step 3: Configure Build Settings

- **Base directory**: `frontend/doctor-dashboard`
- **Build command**: `npm run build`
- **Publish directory**: `frontend/doctor-dashboard/dist`
- **Node version**: 18 (or 20)

#### Step 4: Environment Variables

Add in Site Settings → Build & deploy → Environment:

```env
VITE_API_URL=https://your-api-gateway.com
VITE_RAZORPAY_KEY_ID=rzp_test_xxxxx
VITE_TWILIO_ACCOUNT_SID=ACxxxxx
VITE_APP_ENV=production
```

#### Step 5: Deploy

1. Click "Deploy site"
2. Wait 2-4 minutes
3. Your app will be live at `https://random-name.netlify.app`

### Option 2: Deploy via Netlify CLI

```bash
# Install Netlify CLI
npm install -g netlify-cli

# Login
netlify login

# Navigate to doctor dashboard
cd frontend/doctor-dashboard

# Build the app
npm run build

# Deploy
netlify deploy

# Deploy to production
netlify deploy --prod
```

### Configure for Production

Create `netlify.toml` in `frontend/doctor-dashboard/`:

```toml
[build]
  base = "frontend/doctor-dashboard"
  command = "npm run build"
  publish = "dist"

[build.environment]
  NODE_VERSION = "18"

[[redirects]]
  from = "/api/*"
  to = "https://api.yourdomain.com/api/:splat"
  status = 200
  force = false

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200

[[headers]]
  for = "/*"
  [headers.values]
    X-Frame-Options = "DENY"
    X-XSS-Protection = "1; mode=block"
    X-Content-Type-Options = "nosniff"
    Referrer-Policy = "strict-origin-when-cross-origin"

[[headers]]
  for = "/assets/*"
  [headers.values]
    Cache-Control = "public, max-age=31536000, immutable"
```

### Custom Domain

1. Go to Site Settings → Domain management
2. Add custom domain (e.g., `doctors.yourdomain.com`)
3. Update DNS records
4. Enable HTTPS (automatic)

---

## 🔐 Environment Variables

### Patient Web App (Next.js)

Create `.env.production` in `frontend/patient-webapp/`:

```env
# API Configuration
NEXT_PUBLIC_API_URL=https://api.yourdomain.com
NEXT_PUBLIC_WS_URL=wss://api.yourdomain.com

# Payment Gateway
NEXT_PUBLIC_RAZORPAY_KEY_ID=rzp_live_xxxxx

# Video Consultation
NEXT_PUBLIC_TWILIO_ACCOUNT_SID=ACxxxxx

# App Configuration
NEXT_PUBLIC_APP_NAME=Healthcare Platform
NEXT_PUBLIC_APP_ENV=production
NEXT_PUBLIC_ENABLE_ANALYTICS=true

# Google Maps (if used)
NEXT_PUBLIC_GOOGLE_MAPS_API_KEY=your_key_here

# Optional: Analytics
NEXT_PUBLIC_GA_MEASUREMENT_ID=G-XXXXXXXXXX
```

### Doctor Dashboard (Vite/React)

Create `.env.production` in `frontend/doctor-dashboard/`:

```env
# API Configuration
VITE_API_URL=https://api.yourdomain.com
VITE_WS_URL=wss://api.yourdomain.com

# Payment Gateway
VITE_RAZORPAY_KEY_ID=rzp_live_xxxxx

# Video Consultation
VITE_TWILIO_ACCOUNT_SID=ACxxxxx
VITE_TWILIO_API_KEY_SID=SKxxxxx

# App Configuration
VITE_APP_NAME=Doctor Dashboard
VITE_APP_ENV=production

# Optional: Sentry (Error Tracking)
VITE_SENTRY_DSN=https://xxxxx@sentry.io/xxxxx
```

---

## 🌍 Custom Domains

### Option 1: Use Your Own Domain

If you have a domain from GoDaddy, Namecheap, etc.:

#### For Vercel:

1. Add domain in Vercel dashboard
2. Add DNS records:
   ```
   Type: CNAME
   Name: patients (or @)
   Value: cname.vercel-dns.com
   ```

#### For Netlify:

1. Add domain in Netlify dashboard
2. Add DNS records:
   ```
   Type: CNAME
   Name: doctors (or @)
   Value: your-site.netlify.app
   ```

### Option 2: Free Domain

Get a free domain from Freenom (freenom.com):

1. Search for available domains (.tk, .ml, .ga, .cf, .gq)
2. Register for free (12 months)
3. Configure DNS as above

### Option 3: Subdomain of Free Services

Use subdomains provided by:
- Vercel: `your-app.vercel.app`
- Netlify: `your-app.netlify.app`

---

## 🔄 CI/CD Setup

### Automatic Deployments

Both Vercel and Netlify support automatic deployments from Git.

#### Configure Git Branches

```bash
# Production branch → main
git checkout main
git push origin main  # Deploys to production

# Staging/Preview
git checkout -b staging
git push origin staging  # Deploys to preview URL
```

#### Vercel Preview Deployments

- Every PR gets a unique preview URL
- Automatically deployed on commit
- Comment on PR with preview link

#### Netlify Deploy Previews

- Every PR gets a deploy preview
- Branch deploys for specific branches
- Context-specific environment variables

### GitHub Actions (Optional)

Create `.github/workflows/deploy-frontend.yml`:

```yaml
name: Deploy Frontends

on:
  push:
    branches: [main]
    paths:
      - 'frontend/**'

jobs:
  deploy-patient-app:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install Vercel CLI
        run: npm install -g vercel
      
      - name: Pull Vercel Environment
        run: vercel pull --yes --environment=production --token=${{ secrets.VERCEL_TOKEN }}
        working-directory: frontend/patient-webapp
      
      - name: Build Project
        run: vercel build --prod --token=${{ secrets.VERCEL_TOKEN }}
        working-directory: frontend/patient-webapp
      
      - name: Deploy to Vercel
        run: vercel deploy --prebuilt --prod --token=${{ secrets.VERCEL_TOKEN }}
        working-directory: frontend/patient-webapp

  deploy-doctor-dashboard:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install Dependencies
        run: npm install
        working-directory: frontend/doctor-dashboard
      
      - name: Build
        run: npm run build
        working-directory: frontend/doctor-dashboard
        env:
          VITE_API_URL: ${{ secrets.VITE_API_URL }}
      
      - name: Deploy to Netlify
        uses: netlify/actions/cli@master
        with:
          args: deploy --prod --dir=frontend/doctor-dashboard/dist
        env:
          NETLIFY_AUTH_TOKEN: ${{ secrets.NETLIFY_AUTH_TOKEN }}
          NETLIFY_SITE_ID: ${{ secrets.NETLIFY_SITE_ID }}
```

---

## 📊 Performance Optimization

### Next.js (Patient App)

1. **Enable Image Optimization**:
```javascript
// next.config.js
module.exports = {
  images: {
    domains: ['your-api-domain.com'],
    formats: ['image/avif', 'image/webp'],
  },
}
```

2. **Enable Compression**:
Vercel handles this automatically

3. **Add Cache Headers** (via vercel.json above)

### React/Vite (Doctor Dashboard)

1. **Code Splitting**:
```javascript
// Use lazy loading
const Dashboard = lazy(() => import('./pages/Dashboard'));
```

2. **Optimize Bundle**:
```javascript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom'],
        },
      },
    },
  },
});
```

---

## 🎯 Deployment Checklist

### Pre-Deployment

- [ ] Update API URLs in environment variables
- [ ] Configure CORS on backend for frontend domains
- [ ] Set up error tracking (Sentry, etc.)
- [ ] Configure analytics (Google Analytics, Plausible)
- [ ] Test build locally (`npm run build`)
- [ ] Verify all environment variables
- [ ] Update API keys to production keys
- [ ] Test on mobile devices

### Post-Deployment

- [ ] Verify all pages load correctly
- [ ] Test user authentication flow
- [ ] Test API connectivity
- [ ] Check browser console for errors
- [ ] Verify SSL certificate
- [ ] Test on different browsers
- [ ] Configure custom domain (if applicable)
- [ ] Set up monitoring/alerts
- [ ] Update README with live URLs

---

## 🚀 Quick Deployment Summary

```bash
# Patient Web App to Vercel
cd frontend/patient-webapp
vercel --prod

# Doctor Dashboard to Netlify
cd frontend/doctor-dashboard
npm run build
netlify deploy --prod

# That's it! Both apps are live.
```

---

## 📞 Support

- **Vercel**: https://vercel.com/docs
- **Netlify**: https://docs.netlify.com
- **Issues**: [GitHub Issues](https://github.com/kally123/doctorApp/issues)

---

**Last Updated**: February 21, 2026

