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

import play.api.mvc._
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.forms.UpdateChildBenefitForm
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

/**
  * Created by chrisianson on 04/05/16.
  */
object UpdateChildBenefitController extends UpdateChildBenefitController {
  override protected def authConnector: AuthConnector = FrontendAuthConnector
}

trait UpdateChildBenefitController extends ChildBenefitController {

  protected def authConnector: AuthConnector

  def present = Action.async {
    implicit request =>
      val childBenefitForm = UpdateChildBenefitForm
      Future.successful(Ok(uk.gov.hmrc.cb.views.html.update_child_benefit(childBenefitForm.form)))
  }

  def submit = Action.async {
    implicit request =>
      UpdateChildBenefitForm.form.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(uk.gov.hmrc.cb.views.html.update_child_benefit(formWithErrors)))
        },
        success => {
          val update = success.updateChildBenefit.get
          if (update) {
            Future.successful(Redirect(routes.SubmissionConfirmationController.get()))
          } else {
            Future.successful(Redirect(routes.TechnicalDifficultiesController.get()))
          }
        }
      )
  }
}
