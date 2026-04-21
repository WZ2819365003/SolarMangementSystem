FROM node:22-bullseye AS build
WORKDIR /app

COPY package.json vue.config.js babel.config.js .eslintrc.js .eslintignore .env.example ./
COPY public ./public
COPY src ./src
COPY vendor ./vendor

ENV NODE_OPTIONS=--openssl-legacy-provider
RUN npm install --no-audit --no-fund --legacy-peer-deps --ignore-scripts
RUN npm run build

FROM nginx:1.27-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html/pvms

EXPOSE 80
