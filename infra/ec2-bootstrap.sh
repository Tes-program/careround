#!/usr/bin/env bash
set -euo pipefail

# EC2 bootstrap script — run ONCE on a fresh Ubuntu 22.04 t3.medium
# before the first deploy from GitHub Actions.

echo "==> Updating system packages..."
apt-get update -y
apt-get upgrade -y

# ── Docker CE (official repo, not snap) ────────────────────────────────────
echo "==> Installing Docker CE..."
apt-get install -y ca-certificates curl gnupg lsb-release

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" \
  > /etc/apt/sources.list.d/docker.list

apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# ── Docker service ──────────────────────────────────────────────────────────
echo "==> Enabling Docker service..."
systemctl enable docker
systemctl start docker

# ── Add ubuntu user to docker group ────────────────────────────────────────
echo "==> Adding ubuntu to docker group..."
usermod -aG docker ubuntu

# ── Certbot and nginx (host-level, for initial SSL cert provisioning) ───────
echo "==> Installing Certbot and nginx..."
apt-get install -y certbot python3-certbot-nginx nginx

# Stop host nginx after install so it doesn't conflict with Docker nginx on port 80
systemctl stop nginx
systemctl disable nginx

# ── Create deployment directory ─────────────────────────────────────────────
echo "==> Creating deployment directory..."
mkdir -p /home/ubuntu/careround
chown ubuntu:ubuntu /home/ubuntu/careround

echo ""
echo "========================================================"
echo " Bootstrap complete."
echo ""
echo " NEXT STEPS:"
echo ""
echo " 1. Obtain an SSL certificate (temporarily re-enable nginx):"
echo "    sudo systemctl start nginx"
echo "    sudo certbot certonly --nginx -d <your-domain>"
echo "    sudo systemctl stop nginx"
echo ""
echo " 2. Copy your production .env to the server:"
echo "    scp .env ubuntu@<ec2-host>:/home/ubuntu/careround/.env"
echo ""
echo " 3. Copy infra files and compose to the server:"
echo "    scp -r docker-compose.prod.yml infra/ ubuntu@<ec2-host>:/home/ubuntu/careround/"
echo ""
echo " 4. Start the stack:"
echo "    ssh ubuntu@<ec2-host>"
echo "    cd /home/ubuntu/careround"
echo "    docker compose -f docker-compose.prod.yml up -d"
echo ""
echo " GitHub Actions deploy key (add to repo secrets as EC2_SSH_KEY):"
echo "$(cat ~/.ssh/authorized_keys)"
echo "========================================================"
