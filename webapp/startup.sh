#!/bin/sh
REACT_ENVS=$(env | grep '^REACT_APP_' | awk -F= '{print "\\$"$1}' | xargs)
echo "$REACT_ENVS"
echo ""
BACKEND_ENVS=$(env | grep '^BACKEND_' | awk -F= '{print "\\$"$1}' | xargs)
echo "$BACKEND_ENVS"
for file in /usr/share/nginx/html/static/js/*.js;
do
  envsubst "$REACT_ENVS" < "$file" > "$file.tmp" && mv "$file.tmp" "$file"
done
envsubst "$BACKEND_ENVS" < nginx.template.conf > /etc/nginx/conf.d/default.conf
nginx -t
nginx -g 'daemon off;'