CREATE TABLE time_records (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(1000) NOT NULL,
    time_in_epoch INT UNSIGNED,
    time_out_epoch INT UNSIGNED,
    PRIMARY KEY (id)
);
