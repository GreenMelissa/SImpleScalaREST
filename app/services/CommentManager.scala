package services

import models.{Comment, CommentItem}
import play.api.db.Database
import play.api.libs.json.{Json, OFormat}
import javax.inject.Inject
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Класс, отвечающий за работу с таблицей comment
 * @param db Database
 */
class CommentManager @Inject() (db: Database) {

  implicit val commentListJson: OFormat[Comment] = Json.format[Comment]

  /**
   * Получение всех комментариев к новости
   * @return
   */
  def getAll(newsId: Int): ListBuffer[Comment] = {
    db.withConnection { conn =>
      new NewsManager(db).findById(newsId)
      val statement = conn.prepareStatement(
        "SELECT * FROM comment WHERE news_id = ?"
      )
      statement.setInt(1, newsId)
      var commentList = new mutable.ListBuffer[Comment]()
      val resultSet = statement.executeQuery()
      while (resultSet.next()) {
        commentList += Comment(
          resultSet.getInt("id"),
          resultSet.getInt("news_id"),
          resultSet.getString("text"),
          resultSet.getString("created_at")
        )
      }
      return commentList;
    }
  }

  /**
   * Запись комментария в БД
   * @param comment Модель комментария
   * @return
   */
  def createComment(newsId: Int, comment: Option[CommentItem]): Unit = {
    db.withTransaction { conn =>
      new NewsManager(db).findById(newsId)
      val statement = conn.prepareStatement(
        "INSERT INTO comment (news_id, text, created_at) VALUES (?, ?, null);"
      )
      statement.setInt(1, newsId)
      statement.setString(2, comment.get.text)
      statement.execute
      statement.close()
    }
  }
}
