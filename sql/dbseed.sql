DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS news;

CREATE TABLE IF NOT EXISTS news (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT '0000-00-00 00:00:00',
    updated_at TIMESTAMP DEFAULT now() ON UPDATE now(),
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS comment (
    id INT NOT NULL AUTO_INCREMENT,
    text TEXT NOT NULL,
    news_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (id),
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE
    ) ENGINE=INNODB;
INSERT INTO news
    (id, title, text, created_at, updated_at)
VALUES
    (1, 'Заголовок 1', 'Текст 1', null, null),
    (2, 'Заголовок 2', 'Текст 2', null, null),
    (3, 'Заголовок 3', 'Текст 3', null, null),
    (4, 'Заголовок 4', 'Текст 4', null, null),
    (5, 'Заголовок 5', 'Текст 5', null, null);
INSERT INTO comment
    (id, text, news_id)
VALUES
    (1, 'Первый комментарий к первой новости', 1),
    (2, 'Второй комментарий к первой новости', 1),
    (3, 'Первый комментарий ко второй новости', 2);