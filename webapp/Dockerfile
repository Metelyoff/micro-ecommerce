FROM nginx:alpine
RUN apk --no-cache add gettext
COPY build /usr/share/nginx/html
COPY nginx.template.conf nginx.template.conf
COPY startup.sh /startup.sh
RUN chmod +x /startup.sh
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 CMD curl -f http://localhost/ || exit 1
CMD ["/startup.sh"]

#docker buildx create --name multiarch-builder --use
#docker login
#docker buildx build \
#  --no-cache \
#  --platform linux/arm64 \
#  -t metelyoff/webapp:latest \
#  --push \
#  .