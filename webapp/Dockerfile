FROM node:18-alpine AS build
WORKDIR /app

ARG REACT_APP_PRODUCTS_API_URL
ARG REACT_APP_ORDERS_API_URL
ARG REACT_APP_PAYMENTS_API_URL

ENV REACT_APP_PRODUCTS_API_URL=${REACT_APP_PRODUCTS_API_URL}
ENV REACT_APP_ORDERS_API_URL=${REACT_APP_ORDERS_API_URL}
ENV REACT_APP_PAYMENTS_API_URL=${REACT_APP_PAYMENTS_API_URL}

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine
RUN apk --no-cache add gettext
#COPY nginx.template.conf /etc/nginx/
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 CMD curl -f http://localhost/ || exit 1
CMD ["nginx", "-g", "daemon off;"]
#CMD envsubst '$REACT_APP_ORDERS_API_URL' < /etc/nginx/nginx.template.conf > /etc/nginx/nginx.conf && nginx -g "daemon off;"