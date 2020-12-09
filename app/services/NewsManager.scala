package services

import exceptions.NotFoundException
import play.api.db.Database
import models.{News, NewsItem}
import play.api.libs.json.{Json, OFormat}
import java.sql.PreparedStatement
import javax.inject.Inject
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Класс, отвечающий за работу с таблицей news
 * @param db Database
 */
class NewsManager @Inject() (db: Database) {

  var model: News = _

  implicit val newsListJson: OFormat[News] = Json.format[News]

  /**
   * Получение всех новостей
   * @return
   */
  def getAll(): ListBuffer[News] = {
    db.withConnection { conn =>
      val resultSet = conn.createStatement().executeQuery("SELECT * FROM news")
      var newsList = new mutable.ListBuffer[News]()
      while (resultSet.next()) {
        newsList += News(
          resultSet.getInt("id"),
          resultSet.getString("title"),
          resultSet.getString("text"),
          resultSet.getString("created_at"),
          resultSet.getString("updated_at")
        )
      }
      return newsList;
    }
  }

  /**
   * Запись новости в БД
   * @param news Модель новости
   * @return
   */
  def createNews(news: Option[NewsItem]): Unit = {
    db.withTransaction { conn =>
      val statement = conn.prepareStatement(
        "INSERT INTO news (title, text, created_at, updated_at) VALUES (?, ?, null, null);"
      )
      this.setStatementParams(statement, news)
      statement.execute
      statement.close()
    }
  }

  /**
   * Обновление новости в БД
   * @param id ID новости
   * @param news Модель новости
   */
  def updateNews(id: Int, news: Option[NewsItem]): Unit = {
    db.withTransaction { conn =>
      this.findById(id)
      val statement = conn.prepareStatement(
        "UPDATE news SET title = ?, text = ? WHERE id = ?;"
      )
      this.setStatementParams(statement, news)
      statement.setInt(3, id)
      statement.execute
      statement.close()
    }
  }

  /**
   * Удаление новости по ID
   * @return
   */
  def deleteById(id: Int): Unit = {
    db.withTransaction { conn =>
      this.findById(id)
      val statement = conn.prepareStatement(
        "DELETE FROM news WHERE id = ?"
      )
      statement.setInt(1, id)
      statement.execute()
      statement.close()
    }
  }

  /**
   * Получение новости по ID
   * @param id ID новости
   * @return
   */
  def getById(id: Int): News = {
    this.findById(id)
    this.model
  }

  /**
   * Поиск новости по ID
   * @param id ID новости
   * @return
   */
  def findById(id: Int): Unit = {
    db.withConnection { conn =>
      val statement = conn.prepareStatement(
        "SELECT * FROM news WHERE id = ?"
      )
      statement.setInt(1, id)
      val resultSet = statement.executeQuery()
      if (resultSet.next()) {
        this.model = News(
          resultSet.getInt("id"),
          resultSet.getString("title"),
          resultSet.getString("text"),
          resultSet.getString("created_at"),
          resultSet.getString("updated_at")
        )
      } else {
        throw new NotFoundException()
      }
    }
  }

  /**
   * Подготовка SQL
   * @param statement SQL выражение
   * @param news Модель новости
   */
  def setStatementParams(statement: PreparedStatement, news: Option[NewsItem]): Unit = {
    statement.setString(1, news.get.title)
    statement.setString(2, news.get.text)
  }
}
