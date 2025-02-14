FROM postgres:15

COPY init.sql /docker-entrypoint-initdb.d/init.sql
COPY postgresql.conf /etc/postgresql/postgresql.conf

EXPOSE 5432

HEALTHCHECK --interval=3s --timeout=2s --start-period=2s --retries=3 CMD pg_isready -U postgres -h localhost -p 5432 || exit 1

CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf", "-p", "5432"]