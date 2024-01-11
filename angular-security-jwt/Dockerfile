FROM node:16 AS node
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build --prod

FROM nginx:latest
COPY --from=node /app/dist/angular-security-jwt /usr/share/nginx/html
EXPOSE 4200

#COPY --from=build /app/dist/angular-security-jwt /usr/share/nginx/html
#COPY /nginx.conf  /etc/nginx/conf.d/default.conf
## Exposing a port, here it means that inside the container
## the app will be using Port 80 while running
#EXPOSE 80
