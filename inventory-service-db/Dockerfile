FROM postgres:15

ARG POSTGRES_USER
ARG POSTGRES_PASSWORD
ARG POSTGRES_DB

ENV POSTGRES_USER=$POSTGRES_USER
ENV POSTGRES_PASSWORD=$POSTGRES_PASSWORD

COPY init.sql /docker-entrypoint-initdb.d/init.sql
COPY postgresql.conf /etc/postgresql/postgresql.conf

EXPOSE 5432

HEALTHCHECK --interval=3s --timeout=2s --start-period=2s --retries=3 CMD pg_isready -U postgres -h localhost -p 5432 || exit 1

CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf", "-p", "5432"]