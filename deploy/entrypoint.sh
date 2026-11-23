#!/bin/sh
set -e

cd /var/www/html

# Regenerate the package manifest in a fresh image before caching config/routes.
php artisan package:discover --ansi
php artisan config:cache
php artisan route:cache
php artisan migrate --force

# The symlink may already exist when storage is on a persistent volume.
php artisan storage:link || true

exec apache2-foreground
