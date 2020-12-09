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
    (1, 'First title', 'Text of first news', null, null),
    (2, 'Second title', 'Text of second news', null, null),
    (3, 'Third title', 'Text of third news', null, null),
    (4, 'Fourth title', 'Text of fourth news', null, null),
    (5, 'Fifth title', 'Text of fifth news', null, null);
INSERT INTO comment
    (id, text, news_id)
VALUES
    (1, 'First news comment - 1', 1),
    (2, 'First news comment - 2', 1),
    (3, 'Second news comment - 1', 2);