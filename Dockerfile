# Root build entry for DockHosting: the host builds a Dockerfile with its own folder as context,
# and this image needs dxn-api/ + dxn-admin-web/, so it must live at the repo root.
# Keep in sync with deploy/Dockerfile (same content, root-relative paths).
# syntax=docker/dockerfile:1
# Build context is the repository root: docker build -f deploy/Dockerfile .

# Stage 1 — React admin. VITE_API_URL="" => fetch() calls same-origin /api/*.
FROM node:22-alpine AS admin-build
WORKDIR /app
COPY dxn-admin-web/package.json dxn-admin-web/package-lock.json ./
RUN npm ci
COPY dxn-admin-web/ ./
ENV VITE_API_URL=""
RUN npm run build -- --outDir /admin-dist --emptyOutDir

# Stage 2 — shared PHP 8.3 base with every extension the API needs.
# No MySQL/MariaDB server is installed: DockHosting refuses such images.
FROM php:8.3-apache AS php-base
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libfreetype6-dev libicu-dev libjpeg62-turbo-dev libpng-dev libzip-dev unzip \
    && docker-php-ext-configure gd --with-freetype --with-jpeg \
    && docker-php-ext-install -j"$(nproc)" pdo_mysql intl opcache gd zip \
    && a2enmod rewrite \
    && rm -rf /var/lib/apt/lists/*

# Stage 3 — production vendor directory (no dev dependencies).
FROM php-base AS php-vendor
COPY --from=composer:2 /usr/bin/composer /usr/bin/composer
WORKDIR /app
COPY dxn-api/ ./
RUN composer install --no-dev --no-interaction --prefer-dist --optimize-autoloader

# Stage 4 — runtime image. DockHosting routes to the EXPOSEd port;
# Apache's default listener is 0.0.0.0:80.
FROM php-base
WORKDIR /var/www/html
COPY --from=php-vendor /app/ /var/www/html/
COPY --from=admin-build /admin-dist/ /var/www/html/public/admin/
COPY deploy/apache-vhost.conf /etc/apache2/sites-available/000-default.conf
RUN mkdir -p storage/framework/cache/data storage/framework/sessions \
             storage/framework/views storage/logs bootstrap/cache \
    && chown -R www-data:www-data storage bootstrap/cache
COPY deploy/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
EXPOSE 80
ENTRYPOINT ["entrypoint.sh"]
