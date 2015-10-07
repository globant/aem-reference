FROM node:0.12

RUN mkdir -p /opt/elastic
ADD server.js /opt/elastic/server.js

CMD ["/opt/elastic/server.js"]
ENTRYPOINT ["node"]
