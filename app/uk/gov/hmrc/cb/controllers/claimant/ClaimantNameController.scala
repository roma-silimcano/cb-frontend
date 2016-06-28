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

package uk.gov.hmrc.cb.controllers.claimant

import play.api.Logger
import play.api.data.Form
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.service.keystore.KeystoreService
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.cb.forms.ClaimantNameForm
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.cb.forms.ClaimantNameForm.ClaimantNamePageModel
import uk.gov.hmrc.cb.implicits.Implicits._
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.models.payload.submission.claimant.Claimant
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
  * Created by chrisianson on 24/06/16.
  */

object ClaimantNameController extends ClaimantNameController {
  override val authConnector = FrontendAuthConnector
  override val cacheClient = KeystoreService.cacheClient
}

trait ClaimantNameController extends ChildBenefitController {

  val cacheClient : ChildBenefitKeystoreService

  private val form = ClaimantNameForm.form
  private def view(status: Status, form : Form[ClaimantNamePageModel])(implicit request: Request[AnyContent]) = {
    status(uk.gov.hmrc.cb.views.html.claimant.claimantname(form))
  }

  private def redirectConfirmation() = Redirect(uk.gov.hmrc.cb.controllers.claimant.routes.ClaimantNameController.get())

  def get() = CBSessionProvider.withSession {
    implicit request =>
      val resultWithNoClaimant = view(Ok, form)
      cacheClient.loadPayload().map {
        payload =>
          Logger.error(s"[ClaimantNameController][get] loaded payload")
          payload.fold(
            redirectInitialController
          )(
            cache => {
              cache.claimant match {
                case Some(x) =>
                  Logger.error(s"[ClaimantNameController][get] loaded claimant")
                  val pageModel : ClaimantNamePageModel = x
                  view(Ok, form.fill(pageModel))
                case _ =>
                  resultWithNoClaimant
              }
            }
          )
      } recover {
        case e: Exception =>
          Logger.error(s"[ClaimantNameController][get] keystore exception whilst loading payload: ${e.getMessage}")
          redirectTechnicalDifficulties
      }
  }

  def post() = CBSessionProvider.withSession {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          Logger.info(s"[ClaimantNameController][post] invalid form submission $formWithErrors")
          Future.successful(
            view(BadRequest, formWithErrors)
          )
        },
        model =>
          cacheClient.loadPayload() flatMap {
            cache =>
              saveToKeystore(modifyClaimant(cache.get, model))
          }
      )
  }

  private def modifyClaimant(payload: Payload, model: ClaimantNamePageModel): Payload = {
    val claimant = Claimant(firstName = model.firstName, lastName = model.lastName, None, None)
    Payload(children = payload.children, claimant = Some(claimant))
  }

  private def saveToKeystore(payload : Payload)(implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
    cacheClient.savePayload(payload).map {
      claimant =>
        Logger.debug(s"[ClaimantNameController][saveToKeystore] saved claimant redirecting to submission")
        redirectConfirmation()
    } recover {
      case e : Exception =>
        Logger.error(s"[ClaimantNameController][saveToKeystore] keystore exception whilst saving claimant: ${e.getMessage}")
        redirectTechnicalDifficulties
    }
  }

}
