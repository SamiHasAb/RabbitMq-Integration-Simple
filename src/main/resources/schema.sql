-- Drop tables if they exist (in correct order due to foreign key constraints)
DROP TABLE IF EXISTS orderdetails CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS shippers CASCADE;

-- Create tables
CREATE TABLE Employees (
                           EmployeeID SERIAL PRIMARY KEY,
                           LastName VARCHAR(255),
                           FirstName VARCHAR(255),
                           BirthDate DATE,
                           Photo VARCHAR(255),
                           Notes TEXT
);

CREATE TABLE Shippers (
                          ShipperID SERIAL PRIMARY KEY,
                          ShipperName VARCHAR(255),
                          Phone VARCHAR(50)
);

CREATE TABLE Customers (
                           CustomerID SERIAL PRIMARY KEY,
                           CustomerName VARCHAR(256) NOT NULL,
                           ContactName VARCHAR(50),
                           Address VARCHAR(512),
                           City VARCHAR(50),
                           PostalCode VARCHAR(20),
                           Country VARCHAR(50)
);

CREATE TABLE Categories (
                            CategoryID SERIAL PRIMARY KEY,
                            CategoryName VARCHAR(255) NOT NULL,
                            Description TEXT
);

CREATE TABLE Suppliers (
                           SupplierID SERIAL PRIMARY KEY,
                           SupplierName VARCHAR(255) NOT NULL,
                           ContactName VARCHAR(255),
                           Address VARCHAR(512),
                           City VARCHAR(100),
                           PostalCode VARCHAR(20),
                           Country VARCHAR(100),
                           Phone VARCHAR(50)
);

CREATE TABLE Products (
                          ProductID SERIAL PRIMARY KEY,
                          ProductName VARCHAR(255) NOT NULL,
                          SupplierID INTEGER,
                          CategoryID INTEGER,
                          Unit VARCHAR(255),
                          Price DECIMAL(10,2),
                          FOREIGN KEY (SupplierID) REFERENCES Suppliers(SupplierID),
                          FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);

CREATE TABLE Orders (
                        OrderID SERIAL PRIMARY KEY,
                        CustomerID INTEGER,
                        EmployeeID INTEGER,
                        OrderDate DATE,
                        ShipperID INTEGER,
                        FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID),
                        FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID),
                        FOREIGN KEY (ShipperID) REFERENCES Shippers(ShipperID)
);

CREATE TABLE OrderDetails (
                              OrderDetailID SERIAL PRIMARY KEY,
                              OrderID INTEGER,
                              ProductID INTEGER,
                              Quantity INTEGER,
                              FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
                              FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);