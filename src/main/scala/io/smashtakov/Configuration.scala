package io.smashtakov

import com.typesafe.config.Config
import java.time.{Duration => JDuration}

object Configuration {

  def configOpt(path: String, config: Config): Option[Config] = Some(path).filter(config.hasPath).map(config.getConfig)
  def configGet(path: String, config: Config): Config = configOpt(path, config)
    .getOrElse(throw new Exception(s"$path config was not found"))

  def stringOpt(path: String, config: Config): Option[String] = Some(path).filter(config.hasPath).map(config.getString)
  def stringGet(path: String, config: Config): String = stringOpt(path, config)
    .getOrElse(throw new Exception(s"$path was not defined"))

  def durationOpt(path: String, config: Config): Option[JDuration] = Some(path).filter(config.hasPath).map(config.getDuration)
  def durationGet(path: String, config: Config): JDuration = durationOpt(path, config)
    .getOrElse(throw new Exception(s"$path was not defined"))

  def intOpt(path: String, config: Config): Option[Int] = Some(path).filter(config.hasPath).map(config.getInt)
  def intGet(path: String, config: Config): Int = intOpt(path, config)
    .getOrElse(throw new Exception(s"$path was not defined"))

  def apply(appConfig: Config): Configuration = Configuration(
    routes = RoutesConfiguration(configGet("routes", appConfig)),
    httpServer = HttpServerConfiguration(configGet("http-server", appConfig))
  )

}

import io.smashtakov.Configuration._

case class Configuration(routes: RoutesConfiguration, httpServer: HttpServerConfiguration)


object RoutesConfiguration {

  def apply(config: Config): RoutesConfiguration = RoutesConfiguration(
    path = stringGet("path", config),
    timeout = durationGet("ask-timeout", config)
  )

}

case class RoutesConfiguration(path: String, timeout: JDuration)


object HttpServerConfiguration {

  def apply(config: Config): HttpServerConfiguration = HttpServerConfiguration(
    host = stringGet("host", config),
    port = intGet("port", config)
  )

}

case class HttpServerConfiguration(host: String, port: Int)

