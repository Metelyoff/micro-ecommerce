#!/bin/sh

echo "$API_URL"
echo ""
for file in /usr/share/nginx/html/static/js/*.js;
do
  envsubst "$API_URL" < "$file" > "$file.tmp" && mv "$file.tmp" "$file"
done

BACKEND_ENVS=$(env | grep '^BACKEND_' | awk -F= '{print "\\$"$1}' | xargs)
echo "$BACKEND_ENVS"
echo ""
envsubst "$BACKEND_ENVS" < nginx.template.conf > /etc/nginx/conf.d/default.conf

nginx -t
nginx -g 'daemon off;'