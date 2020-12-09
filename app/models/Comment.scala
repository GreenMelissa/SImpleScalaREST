package models

case class Comment(
  id: Int,
  news_id: Int,
  text: String,
  created_at: String
)
