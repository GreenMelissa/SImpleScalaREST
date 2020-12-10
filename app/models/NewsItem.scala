package models

import play.api.db.Database

/**
 * Модель новости для сохранения
 * @param title Заголовок
 * @param text Описание
 */
case class NewsItem(title: String, text: String) {

  /**
   * Создание записи новости
   * @param db Database
   */
  def save(db: Database): Unit = {
    db.withTransaction { conn =>
      val statement = conn.prepareStatement(
        "INSERT INTO news (title, text, created_at, updated_at) VALUES (?, ?, null, null);"
      )
      statement.setString(1, this.title)
      statement.setString(2, this.text)
      statement.execute
      statement.close()
    }
  }
}
