# init-users.sql (optional)
CREATE USER app_user WITH PASSWORD 'app_password';
GRANT ALL PRIVILEGES ON DATABASE order_management TO app_user;