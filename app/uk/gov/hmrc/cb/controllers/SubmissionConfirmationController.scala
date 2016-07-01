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

import play.api.mvc.{Action, Request}
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.models.payload.submission.claimant.Claimant
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

/**
 * Created by adamconder on 05/05/2016.
 */
object SubmissionConfirmationController extends SubmissionConfirmationController {
  override val authConnector : AuthConnector = FrontendAuthConnector
}

trait SubmissionConfirmationController extends ChildBenefitController {

  def get = Action.async {
    implicit request =>
      val claimant = Claimant(firstName = "Louise", lastName = "Smith")
      Future.successful(Ok(uk.gov.hmrc.cb.views.html.confirmation_submission(claimant = claimant)))
  }
}
