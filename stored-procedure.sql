DROP PROCEDURE IF EXISTS add_movie;
DELIMITER //

CREATE PROCEDURE add_movie (
    IN p_title VARCHAR(100),
    IN p_year INTEGER,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_star_birthYear INTEGER,
    IN p_genre_name VARCHAR(32)
)
BEGIN
    DECLARE v_star_id VARCHAR(10);
    DECLARE v_genre_id INTEGER;
    DECLARE v_movie_id VARCHAR(10);

    SELECT id INTO v_movie_id FROM movies WHERE title = p_title AND year = p_year AND director = p_director LIMIT 1;

    IF v_movie_id IS NOT NULL THEN
        SELECT 'Movie already exists.' AS message;
    ELSE
        SELECT id INTO v_star_id FROM stars WHERE name = p_star_name LIMIT 1;
        IF v_star_id IS NULL THEN
            INSERT INTO stars (id, name, birthYear)
            VALUES (CONCAT('nm', LPAD((SELECT CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) + 1 FROM stars), 7, '0')), p_star_name, p_star_birthYear);
            SELECT LAST_INSERT_ID() INTO v_star_id;
        END IF;

        SELECT id INTO v_genre_id FROM genres WHERE name = p_genre_name LIMIT 1;
        IF v_genre_id IS NULL THEN
            INSERT INTO genres (name) VALUES (p_genre_name);
            SELECT LAST_INSERT_ID() INTO v_genre_id;
        END IF;

        CREATE TEMPORARY TABLE temp_movie_id AS
        SELECT CONCAT('tt', LPAD((SELECT CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) + 1 FROM movies), 7, '0')) AS new_id;

        INSERT INTO movies (id, title, year, director)
        SELECT new_id, p_title, p_year, p_director FROM temp_movie_id;

        SELECT new_id INTO v_movie_id FROM temp_movie_id;

        DROP TEMPORARY TABLE IF EXISTS temp_movie_id;

        INSERT INTO stars_in_movies (starId, movieId) VALUES (v_star_id, v_movie_id);
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (v_genre_id, v_movie_id);

        SELECT 'Movie added successfully.' AS message;
    END IF;
END //

DELIMITER ;
