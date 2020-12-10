package controllers

import models.{News, NewsItem}
import play.api.db.Database
import play.api.libs.json.{Json, OFormat}
import services.{NewsManager, NewsParser}
import exceptions.NotFoundException
import javax.inject.Inject
import java.io.File
import java.nio.file.{Files, Path}
import javax.inject._
import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

/**
 * Контроллер
 * @param db Database
 * @param controllerComponents ControllerComponents
 */
@Singleton
class NewsController @Inject()(db: Database, val controllerComponents: ControllerComponents)
                              (implicit executionContext: ExecutionContext) extends BaseController {

  implicit val newNewsJson: OFormat[NewsItem] = Json.format[NewsItem]
  implicit val dataJson: OFormat[News] = Json.format[News]

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  /**
   * Uses a custom FilePartHandler to return a type of "File" rather than
   * using Play's TemporaryFile class.  Deletion must happen explicitly on
   * completion, rather than TemporaryFile (which uses finalization to
   * delete temporary files).
   * Source: https://github.com/playframework/play-scala-fileupload-example
   *
   * @return
   */
  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType, _) =>
      val path: Path = Files.createTempFile("multipartBody", "tempFile")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          FilePart(partName, filename, contentType, path.toFile)
      }
  }

  /**
   * Удаление временного файла и получение XML
   */
  private def operateOnTempFile(file: File): Elem = {
    val xml = scala.xml.XML.loadFile(file.toPath.toFile)
    Files.deleteIfExists(file.toPath)
    xml
  }

  /**
   * Загрузка RSS
   * @return
   */
  def upload(): Action[MultipartFormData[File]] = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val xml = request.body.file("xml").map {
      case FilePart(key, filename, contentType, file, fileSize, dispositionType) =>
        val data = operateOnTempFile(file)
        new NewsParser(db).parseXml(data)
    }

    if (xml.isDefined) {
      Ok
    } else {
      BadRequest
    }
  }

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
        newsItem.get.save(db)
        Created
      } catch {
        case _: Throwable => InternalServerError
      }
    } else {
      BadRequest
    }
  }

  /**
   * Обновление новости
   * @param id ID новости
   * @return
   */
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
