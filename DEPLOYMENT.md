# Blood Connect Backend - Render Deployment Guide

## Overview
This guide explains how to deploy the Blood Connect backend to Render.

## Prerequisites
- GitHub account with the Connect-with-Blood repository
- Render account (https://render.com)
- MySQL database (you can use Render's managed PostgreSQL or an external MySQL service)
- API keys and credentials for:
  - OpenRouter AI
  - Cloudinary
  - Google OAuth (optional)

## Step 1: Prepare Environment Variables

Create or set the following environment variables in Render:

### Database Configuration
- `DATABASE_URL`: Full JDBC URL (e.g., `jdbc:mysql://your-host:3306/bloodconnect_db`)
- `DATABASE_USERNAME`: Database user
- `DATABASE_PASSWORD`: Database password

### Server Configuration
- `PORT`: Should be `9292` or let Render assign it
- `SPRING_SECURITY_USER_NAME`: Admin username
- `SPRING_SECURITY_USER_PASSWORD`: Strong password

### Security
- `JWT_SECRET`: Strong secret key for JWT tokens

### Email Configuration
- `MAIL_USERNAME`: Gmail address for sending emails
- `MAIL_PASSWORD`: Gmail app password (not regular password)

### File Upload
- `UPLOAD_DIR`: Set to `/tmp/uploads` for Render

### Cloudinary (Image Upload)
- `CLOUDINARY_CLOUD_NAME`: Your Cloudinary cloud name
- `CLOUDINARY_API_KEY`: Cloudinary API key
- `CLOUDINARY_API_SECRET`: Cloudinary API secret

### AI Features (OpenRouter)
- `OPENROUTER_API_KEY`: Your OpenRouter API key
- `OPENROUTER_API_URL`: `https://openrouter.ai/api/v1`
- `OPENROUTER_MODEL`: Model to use (default: `nvidia/nemotron-3-super-120b-a12b:free`)

### OAuth (Optional)
- `GOOGLE_CLIENT_ID`: Google OAuth client ID
- `APPLE_TEAM_ID`: Apple Team ID
- `APPLE_KEY_ID`: Apple Key ID

## Step 2: Deploy to Render

1. Go to https://render.com/dashboard
2. Click "New +" → "Web Service"
3. Connect your GitHub account and select `Connect-with-Blood` repository
4. Configure the service:
   - **Name**: `blood-connect-backend`
   - **Runtime**: Docker
   - **Dockerfile Path**: `bloodconnect/bloodconnect/Dockerfile`
   - **Region**: Choose closest to your users
   - **Plan**: Free (for testing) or Paid ($7/month starting)

5. Add all environment variables listed above
6. Click "Create Web Service"

## Step 3: Configure Database

If using a remote MySQL database:
1. Ensure your database is accessible from Render's servers
2. Update `DATABASE_URL` with the remote connection string
3. Run migrations (the app will auto-update schema with `ddl-auto=update`)

**Alternative**: Use a managed database service like PlanetScale (MySQL-compatible)

## Step 4: Update Frontend

Update your frontend's API base URL to point to Render:

```javascript
// .env.production
VITE_API_BASE_URL=https://your-render-service-url
```

## Step 5: Monitor Deployment

1. Check Render's logs for any errors
2. Common issues:
   - Database connection: Check `DATABASE_URL` format
   - Missing environment variables: Check all required vars are set
   - Port binding: Render assigns a specific port automatically

## Troubleshooting

### Build Fails: "cannot find symbol class GeminiService"
- Ensure all source files are committed to GitHub
- Check that the Dockerfile is correct
- Run `git add -A && git commit && git push` to update

### Database Connection Error
- Verify `DATABASE_URL` format is correct
- Check database credentials
- Ensure database is accessible from Render's network

### Service Not Starting
- Check logs in Render dashboard
- Verify all environment variables are set
- Check that application listens on the correct PORT

### Health Check Failing
- Ensure the service is actually running
- Check if actuator endpoint `/actuator/health` is enabled
- Wait a few minutes for the service to fully initialize

## Local Development

To test locally before deploying:

```bash
cd bloodconnect/bloodconnect

# Copy .env.example to .env and fill in values
cp .env.example .env

# Build with Maven
mvn clean package -DskipTests

# Run the application
java -jar target/bloodconnect-0.0.1-SNAPSHOT.jar
```

## Useful Commands

```bash
# View deployment logs
# -> Use Render dashboard

# Rebuild and redeploy
# -> Push changes to GitHub (automatic redeploy)

# Check service status
# -> Visit https://<service-url>/actuator/health
```

## Security Notes

1. **Never commit secrets** to GitHub - use Render's environment variables
2. **Use strong passwords** for all services
3. **Enable HTTPS** (Render does this automatically)
4. **Rotate API keys** periodically
5. **Keep dependencies updated** for security patches

## Support URLs

- Render Documentation: https://render.com/docs
- Spring Boot on Docker: https://spring.io/guides/topical/docker/
- OpenRouter API: https://openrouter.ai/docs

---

**Last Updated**: April 2026
