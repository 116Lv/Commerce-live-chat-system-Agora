FROM node:22-alpine AS build

WORKDIR /workspace

ARG VITE_API_BASE_URL=/
ARG VITE_PORTONE_STORE_ID=
ARG VITE_PORTONE_CHANNEL_KEY=
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_PORTONE_STORE_ID=$VITE_PORTONE_STORE_ID
ENV VITE_PORTONE_CHANNEL_KEY=$VITE_PORTONE_CHANNEL_KEY

COPY package.json package-lock.json ./
RUN npm ci

COPY index.html vite.config.js ./
COPY src ./src

RUN npm run build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html

EXPOSE 80
