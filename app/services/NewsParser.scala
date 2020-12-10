package services

import models.NewsItem
import play.api.db.Database
import javax.inject.Inject
import scala.xml._
import scala.language.postfixOps

/**
 * Парсер новостей
 * @param db Database
 */
class NewsParser @Inject() (db: Database) {

  /**
   * Парс файла и сохранение новостей
   * @param xml XML для парсинга
   */
  def parseXml(xml: Elem): Unit = {
    xml \\ "item" foreach { node =>
      NewsItem(node \ "description" text, node \ "title" text).save(db)
    }
  }
}
