package controllers

import exceptions.NotFoundException
import models.{Comment, CommentItem}
import play.api.db.Database
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{CommentManager, NewsManager}

import javax.inject.{Inject, Singleton}

/**
 * Контроллер для работы с комментариями
 * @param db Database
 * @param controllerComponents ControllerComponents
 */
@Singleton
class CommentController @Inject()(db: Database, val controllerComponents: ControllerComponents) extends BaseController {

  implicit val newCommentJson: OFormat[CommentItem] = Json.format[CommentItem]
  implicit val dataJson: OFormat[Comment] = Json.format[Comment]

  /**
   * Получение всех комментариев к новости
   * @return
   */
  def index(newsId: Int): Action[AnyContent] = Action {
    try {
      val data = new CommentManager(db).getAll(newsId)
      if (data.isEmpty) {
        NoContent
      } else {
        Ok(Json.toJson(data))
      }
    } catch {
      case _: NotFoundException => NotFound
      case _: Throwable => InternalServerError
    }
  }

  /**
   * Создание комментария к новости
   * @param newsId ID новости
   * @return
   */
  def create(newsId: Int): Action[AnyContent] = Action { implicit request =>
    val commentItem: Option[CommentItem] = {
      request.body.asJson.flatMap(
        Json.fromJson[CommentItem](_).asOpt
      )
    }
    if (commentItem.isDefined) {
      try {
        new CommentManager(db).createComment(newsId, commentItem)
        Created
      } catch {
        case _: NotFoundException => NotFound
        case _: Throwable => InternalServerError
      }
    } else {
      BadRequest
    }
  }
}
