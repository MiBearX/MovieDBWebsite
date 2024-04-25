CREATE TABLE movies (
                        id VARCHAR(10) PRIMARY KEY,
                        title VARCHAR(100) NOT NULL,
                        year INTEGER NOT NULL,
                        director VARCHAR(100) NOT NULL
);

CREATE TABLE stars (
                       id VARCHAR(10) PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       birthYear INTEGER
);

CREATE TABLE stars_in_movies (
                                 starId VARCHAR(10),
                                 movieId VARCHAR(10),
                                 FOREIGN KEY (starId) REFERENCES stars(id),
                                 FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE genres (
                        id INTEGER AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(32) NOT NULL
);

CREATE TABLE genres_in_movies (
                                  genreId INTEGER,
                                  movieId VARCHAR(10),
                                  FOREIGN KEY (genreId) REFERENCES genres(id),
                                  FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE customers (
                           id INTEGER AUTO_INCREMENT PRIMARY KEY,
                           firstName VARCHAR(50) NOT NULL,
                           lastName VARCHAR(50) NOT NULL,
                           ccId VARCHAR(20),
                           address VARCHAR(200),
                           email VARCHAR(50) NOT NULL,
                           password VARCHAR(20) NOT NULL
);

CREATE TABLE sales (
                       id INTEGER AUTO_INCREMENT PRIMARY KEY,
                       customerId INTEGER,
                       movieId VARCHAR(10),
                       saleDate DATE NOT NULL,
                       FOREIGN KEY (customerId) REFERENCES customers(id),
                       FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE creditcards (
                             id VARCHAR(20) PRIMARY KEY,
                             firstName VARCHAR(50) NOT NULL,
                             lastName VARCHAR(50) NOT NULL,
                             expiration DATE NOT NULL
);

CREATE TABLE ratings (
                         movieId VARCHAR(10) PRIMARY KEY,
                         rating FLOAT NOT NULL,
                         numVotes INTEGER NOT NULL,
                         FOREIGN KEY (movieId) REFERENCES movies(id)
);