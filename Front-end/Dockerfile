FROM node:23-alpine as build
WORKDIR /app
COPY package.json .
RUN npm install --force
COPY . .
RUN npm run build

FROM nginxinc/nginx-unprivileged

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 3000
EXPOSE 443

CMD ["nginx", "-g", "daemon off;"]