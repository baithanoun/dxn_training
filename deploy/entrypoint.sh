#!/bin/sh
set -e

cd /var/www/html

# Regenerate the package manifest in a fresh image before caching config/routes.
php artisan package:discover --ansi
php artisan config:cache
php artisan route:cache
php artisan migrate --force

# No shell on DockHosting: seed here. ContentSeeder skips when content exists.
# AdminSeeder (re)sets the admin password from ADMIN_PASSWORD on every boot while the
# variable is set, so remove ADMIN_PASSWORD from the host Variables after the first login.
php artisan db:seed --class=ContentSeeder --force
if [ -n "$ADMIN_PASSWORD" ]; then
  php artisan db:seed --class=AdminSeeder --force
fi

# The symlink may already exist when storage is on a persistent volume.
php artisan storage:link || true

exec apache2-foreground
