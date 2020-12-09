

REST API для новостного канала

Методы API

    GET    news Просмотр списка новостей
    GET    news/ID Просмотр новости по ID
    POST   news Создание новости (параметры title, text)
    PUT    news/ID Редактирование новости по ID (параметры title, text)
    DELETE news/ID Удаление новости по ID
    GET    comment/newsID Просмотр списка всех комментариев к новости
    POST   comment/newsID Создание комментария к новости (параметры text)

В файле conf/application.conf укажите конфигурацию БД. Используйте sql/dbseed.sql заполнения БД тестовыми данными.

Запуск sbt ~run

Вы можете протестировать API, используя Postman.
