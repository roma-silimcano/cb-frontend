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

import play.api.Logger
import uk.gov.hmrc.cb.connectors.KeystoreConnector
import uk.gov.hmrc.http.cache.client.SessionCache
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.play.http.logging.{LoggingDetails, MdcLoggingExecutionContext}
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by chrisianson on 01/06/16.
  */

object KeystoreService  {

  val cacheClient = new ChildBenefitKeystoreService

  class ChildBenefitKeystoreService extends CBKeystoreKeys {

    val sessionCache: SessionCache = KeystoreConnector

    implicit def mdcExecutionContext(implicit loggingDetails: LoggingDetails): ExecutionContext = MdcLoggingExecutionContext.fromLoggingDetails

    /**
     * get particular key out of keystore
     */
    private def fetchEntryForSession[T](key :String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
      Logger.debug(s"[KeystoreService][fetchAndGetEntry] $key hc: $hc format : $format request: $request")
      sessionCache.fetchAndGetEntry[T](key)
    }

    /**
      * Store data to Keystore using a key
      */
    private def cacheEntryForSession[T](data: T,key :String)(implicit hc: HeaderCarrier, format: play.api.libs.json.Format[T], request: Request[Any]): Future[Option[T]] = {
      Logger.debug(s"[KeystoreService][cacheEntryForSession] $data, $key hc: $hc format : $format request: $request")
      sessionCache.cache[T](key, data) map {
        case x => x.getEntry[T](key)
      }
    }

    def loadChildren()(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      fetchEntryForSession[List[Child]](childrenKey).map {
        result =>
          result.getOrElse(Nil)
      }
    }

    def saveChildren(children : List[Child])(implicit hc : HeaderCarrier, request : Request[AnyContent]) = {
      cacheEntryForSession[List[Child]](children, childrenKey).map {
        result =>
          result.getOrElse(Nil)
      }
    }

  }
}
