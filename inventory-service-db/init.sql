\c postgres;
DROP DATABASE IF EXISTS inventory_service_db;
CREATE DATABASE inventory_service_db;

-- replace the username and password
CREATE ROLE inventory WITH
    PASSWORD 'inventory'
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOINHERIT
    LOGIN
    NOREPLICATION
    NOBYPASSRLS
    CONNECTION LIMIT -1;

GRANT CREATE,USAGE ON SCHEMA public TO inventory;
\c inventory_service_db;

