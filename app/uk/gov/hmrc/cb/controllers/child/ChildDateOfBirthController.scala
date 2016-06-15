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

package uk.gov.hmrc.cb.controllers.child

import play.api.Logger
import play.api.data.Form
import play.api.mvc.{Result, AnyContent, Request}
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.{ChildBenefitChildrenController, ChildBenefitController}
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildDateOfBirthForm
import uk.gov.hmrc.cb.forms.ChildDateOfBirthForm.ChildDateOfBirthPageModel
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.cb.service.keystore.KeystoreService
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService

import uk.gov.hmrc.cb.implicits.Implicits._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by adamconder on 15/06/2016.
 */
object ChildDateOfBirthController extends ChildDateOfBirthController {
  override val authConnector = FrontendAuthConnector
  override val cacheClient = KeystoreService.cacheClient
  override val childrenService = ChildrenManager.childrenService
}

trait ChildDateOfBirthController extends ChildBenefitChildrenController {

//  val cacheClient : ChildBenefitKeystoreService
//  val childrenService : ChildrenService

  private val form = ChildDateOfBirthForm.form
  private def view(status: Status, form : Form[ChildDateOfBirthPageModel], id : Int)(implicit request: Request[AnyContent]) = {
    status(uk.gov.hmrc.cb.views.html.child.childdateofbirth(form, id))
  }

  private def nextController = Redirect(uk.gov.hmrc.cb.controllers.routes.SubmissionConfirmationController.get())

//  case class KeystoreException(e : String) extends IllegalArgumentException(e)

  def get(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      cacheClient.loadChildren().map {
        children =>
          Logger.debug(s"[ChildDateOfBirthController][get] loaded children $children")
          val resultWithNoChild = view(Ok, form, id)
          childrenService.getChildById(id, children).fold(resultWithNoChild){
            child =>
              if (child.hasDateOfBirth) {
                val model : ChildDateOfBirthPageModel = child
                view(Ok, form.fill(model), id)
              } else {
                resultWithNoChild
              }
          }
      } recover {
        case e : Exception =>
          Logger.error(s"[ChildDateOfBirthController][get] keystore exception whilst loading children ${e.getMessage}")
          redirectTechnicalDifficulties
      }
  }

  def post(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          Logger.info(s"s[ChildDateOfBirthController][post] invalid form submission $formWithErrors")
            Future.successful(
              view(BadRequest, formWithErrors, id)
            )
        },
      model =>
        cacheClient.loadChildren() flatMap {
          cache =>
            handleChildrenWithCallback(cache, id, model) {
              children =>
                saveToKeystore(children){
                  case Left(_) => nextController
                  case Right(redirect) => redirect
                }
            }
        } recover {
          case e: Exception =>
            Logger.error(s"[ChildDateOfBirthController][post] keystore exception whilst loading children ${e.getMessage}")
            redirectTechnicalDifficulties
        }
      )
  }

}
