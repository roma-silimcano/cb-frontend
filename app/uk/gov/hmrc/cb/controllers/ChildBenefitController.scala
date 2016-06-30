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

package uk.gov.hmrc.cb.controllers

import play.api.Logger
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by andrew on 03/05/16.
 */

trait ChildBenefitController extends FrontendController with Actions with CBRoutes {

  val authConnector: AuthConnector

  protected def redirectTechnicalDifficulties = Redirect(technicalDifficulties)
}
