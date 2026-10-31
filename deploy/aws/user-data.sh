#!/bin/bash
# =====================================================================
# User data para AWS EC2 (Amazon Linux 2023, x86_64)
#
# Se ejecuta automáticamente, como root, en el PRIMER arranque de la
# instancia y deja la aplicación funcionando en el puerto 80:
#   1. Crea 1 GB de swap (útil en instancias de 1 GB de RAM)
#   2. Instala Docker y el plugin Docker Compose
#   3. Descarga docker-compose.yml desde el repositorio de GitHub
#   4. Genera el archivo .env con una contraseña aleatoria para PostgreSQL
#   5. Descarga la imagen de Docker Hub y levanta los servicios
#
# Log de la ejecución: /var/log/cloud-init-output.log
# =====================================================================
set -euxo pipefail

APP_DIR=/opt/lab5-champions
COMPOSE_URL=https://raw.githubusercontent.com/fabio2152/LAB5_1/main/docker-compose.yml

# --- 1. Swap de 1 GB ---------------------------------------------------
if [ ! -f /swapfile ]; then
  dd if=/dev/zero of=/swapfile bs=1M count=1024
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap defaults 0 0' >> /etc/fstab
fi

# --- 2. Docker y Docker Compose ----------------------------------------
dnf install -y docker
systemctl enable --now docker
usermod -aG docker ec2-user

mkdir -p /usr/local/lib/docker/cli-plugins
curl -fsSL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
docker compose version

# --- 3. docker-compose.yml desde GitHub --------------------------------
mkdir -p "$APP_DIR"
cd "$APP_DIR"
curl -fsSL "$COMPOSE_URL" -o docker-compose.yml

# --- 4. Variables de entorno (.env solo en el servidor) ----------------
# La BD no se publica fuera de la red de Compose, así que la contraseña
# solo la usan los contenedores; se genera aleatoria en cada instancia.
if [ ! -f .env ]; then
  cat > .env <<EOF
DB_NAME=champions
DB_USER=postgres
DB_PASSWORD=$(head -c 16 /dev/urandom | od -An -tx1 | tr -d ' \n')
APP_PORT=80
APP_TIMEZONE=America/Lima
APP_IMAGE=fabio2152/lab5-champions:latest
SPORTSDB_SEASON=2026-2027
SPORTSDB_SYNC_ON_STARTUP=true
EOF
  chmod 600 .env
fi
chown -R ec2-user:ec2-user "$APP_DIR"

# --- 5. Descargar la imagen y levantar ---------------------------------
docker compose pull
docker compose up -d --no-build
docker compose ps
