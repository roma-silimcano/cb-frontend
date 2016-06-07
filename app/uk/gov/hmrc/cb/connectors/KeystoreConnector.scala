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

package uk.gov.hmrc.cb.connectors

import uk.gov.hmrc.cb.config.WSHttp
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}

/**
  * Created by chrisianson on 01/06/16.
  */


trait KeystoreConnector extends SessionCache with AppName with ServicesConfig {
  override lazy val http = WSHttp
  override lazy val defaultSource = appName
  override lazy val baseUri = baseUrl("keystore")
  // $COVERAGE-OFF$Trivial and never going to be called by a test that uses it's own object implementation
  override lazy val domain = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
  // $COVERAGE-ON$
}

object KeystoreConnector extends KeystoreConnector
