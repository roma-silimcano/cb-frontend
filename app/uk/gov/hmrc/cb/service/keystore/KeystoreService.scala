/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cb.service.keystore

import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.ws._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.cb.models.{KeystoreConnector, Child}
import uk.gov.hmrc.play.http.logging.{LoggingDetails, MdcLoggingExecutionContext}
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by chrisianson on 01/06/16.
  */
object KeystoreService extends CBKeystoreKeys {

  val cacheClient = new ChildBenefitKeystoreService

  class ChildBenefitKeystoreService {

    private val keystoreConnector: SessionCache = KeystoreConnector
    private val source = "cb-frontend"

    implicit def mdcExecutionContext(implicit loggingDetails: LoggingDetails): ExecutionContext = MdcLoggingExecutionContext.fromLoggingDetails

    /**
      * Store data to Keystore using a key
      */
    def cacheEntryForSession[T](data: T,key :String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
      keystoreConnector.cache[T](source, buildId, key, data) map {
        case x => x.getEntry[T](key)
      }
    }

    /**
      * get particular key out of keystore
      */
    def fetchEntryForSession[T](key :String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
      keystoreConnector.fetchAndGetEntry[T](source, buildId, key)
    }

    //This will append a session id or similar to construct a unique id for this user
    private def buildId(implicit request: Request[Any]) = {
      val id = "cb_pages"
      val sessionId = request.session.get(SessionKeys.sessionId)

      sessionId match {
        case Some(_) =>
          val sSessionId = sessionId.get
          s"$id:$sSessionId"
        case _ =>
          "noSessionIdFound"
      }
    }

    def loadChildren()(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      fetchEntryForSession[List[Child]](childrenKey).map {
        result =>
          result
      }
    }

    def saveChildren(children : List[Child])(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      cacheEntryForSession[List[Child]](children, childrenKey).map {
        result =>
          result
      }
    }

  }
}
