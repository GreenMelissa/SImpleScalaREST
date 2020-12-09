package controllers

import models.{News, NewsItem}
import play.api.mvc._
import play.api.db.Database
import play.api.libs.json.{Json, OFormat}
import services.NewsManager
import exceptions.NotFoundException
import play.api.libs.Files

import java.nio.file.Paths
import javax.inject._
import javax.inject.Inject
import scala.xml.Elem

/**
 * Контроллер
 * @param db Database
 * @param controllerComponents ControllerComponents
 */
@Singleton
class NewsController @Inject()(db: Database, val controllerComponents: ControllerComponents) extends BaseController {

  implicit val newNewsJson: OFormat[NewsItem] = Json.format[NewsItem]
  implicit val dataJson: OFormat[News] = Json.format[News]

  /**
   * Получение всех новостей
   * @return
   */
  def index(): Action[AnyContent] = Action {
    val data = new NewsManager(db).getAll()
    if (data.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(data))
    }
  }

  /**
   * Получение новости по ID
   * @return
   */
  def view(id: Int): Action[AnyContent] = Action {
    try {
      Ok (Json.toJson(new NewsManager(db).getById(id)))
    } catch {
      case _: NotFoundException => NotFound
      case _: Throwable => InternalServerError
    }
  }

  /**
   * Удаление новости по ID
   * @return
   */
  def delete(id: Int): Action[AnyContent] = Action {
    try {
      new NewsManager(db).deleteById(id)
      Ok
    } catch {
      case _: NotFoundException => NotFound
      case _: Throwable => InternalServerError
    }
  }

  /**
   * Создание новости
   * @return
   */
  def create(): Action[AnyContent] = Action { implicit request =>
    val newsItem: Option[NewsItem] = {
      request.body.asJson.flatMap(
        Json.fromJson[NewsItem](_).asOpt
      )
    }
    if (newsItem.isDefined) {
      try {
        new NewsManager(db).createNews(newsItem)
        Created
      } catch {
        case _: Throwable => InternalServerError
      }
    } else {
      BadRequest
    }
  }

  def update(id: Int): Action[AnyContent] = Action { implicit request =>
    val newsItem: Option[NewsItem] = {
      request.body.asJson.flatMap(
        Json.fromJson[NewsItem](_).asOpt
      )
    }

    if (newsItem.isDefined) {
      new NewsManager(db).updateNews(id, newsItem)
      Ok
    } else {
      BadRequest
    }
  }
}
