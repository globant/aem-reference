FROM ubuntu:trusty

ENV DISPATCHER_MOD=dispatcher-apache2.4-4.1.10.so
ENV DISPATCHER_TAR=dispatcher-apache2.4-linux-x86-64-4.1.10.tar.gz
ENV DISPATCHER_URL=https://www.adobeaemcloud.com/content/companies/public/adobe/dispatcher/dispatcher/_jcr_content/top/download_8/file.res/$DISPATCHER_TAR

# Install apache
RUN \
  apt-get install -y apache2 && \
  apt-get install -y wget

RUN mkdir -p \
  /usr/lib/apache2/modules \
  /opt/aem/dispatcher \
  /var/log/dispatcher \
  /var/www/aem-reference

RUN chown www-data:www-data /var/www/aem-reference

# Download dispatcher
WORKDIR /opt/aem/dispatcher
RUN \
  wget $DISPATCHER_URL && \
  tar -zxvf $DISPATCHER_TAR && \
  ln -s /opt/aem/dispatcher/$DISPATCHER_MOD /usr/lib/apache2/modules/mod_dispatcher.so
# ln -s /opt/aem/dispatcher/dispatcher-apache2.2-4.1.10.so /usr/lib/apache2/modules/mod_dispatcher.so

# Add config files
ADD etc/apache2 /etc/apache2

# Enable dispatcher module
RUN a2enmod aem_dispatcher
#RUN a2enconf servername
RUN a2ensite aem-reference
RUN a2enmod rewrite

CMD ["-D", "FOREGROUND"]
ENTRYPOINT ["/usr/sbin/apache2ctl"]

EXPOSE 80 443
