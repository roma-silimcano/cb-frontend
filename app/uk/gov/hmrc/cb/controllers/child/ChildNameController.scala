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
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildNameForm
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.implicits.Implicits._
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.models.payload.submission.child.Child
import uk.gov.hmrc.cb.service.keystore.KeystoreService
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by adamconder on 01/06/2016.
 */

object ChildNameController extends ChildNameController {
  override val authConnector = FrontendAuthConnector
  override val cacheClient = KeystoreService.cacheClient
  override val childrenService = ChildrenManager.childrenService
}

trait ChildNameController extends ChildBenefitController {

  val cacheClient : ChildBenefitKeystoreService
  val childrenService : ChildrenService

  private val form = ChildNameForm.form
  private def view(status: Status, form : Form[ChildNamePageModel], id : Int)(implicit request: Request[AnyContent]) = {
    status(uk.gov.hmrc.cb.views.html.child.childname(form, id))
  }

  private def redirectConfirmation(id : Int) = Redirect(uk.gov.hmrc.cb.controllers.child.routes.ChildDateOfBirthController.get(id))

  def get(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      val resultWithNoChild = view(Ok, form, id)
      cacheClient.loadPayload().map {
        payload =>
          Logger.debug(s"[ChildNameController][get] loaded payload")
          payload.fold(
            resultWithNoChild
          )(
            cache => {
              childrenService.getChildById(id, cache.children).fold(resultWithNoChild){
                child =>
                  if(child.hasName) {
                    Logger.debug(s"[ChildNameController][get] child does exist at index")
                    val model : ChildNamePageModel = child
                    view(Ok, form.fill(model), id)
                  } else {
                    resultWithNoChild
                  }
              }
            }
          )
      } recover {
        case e: Exception =>
          Logger.error(s"[ChildNameController][get] keystore exception whilst loading children: ${e.getMessage}")
          redirectTechnicalDifficulties
      }
  }

  def post(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          Logger.debug(s"[ChildNameController][bindFromRequest] invalid form submission $formWithErrors")
            Future.successful(
              view(BadRequest, formWithErrors, id)
            )},
        model =>
          cacheClient.loadPayload() flatMap {
            cache =>
              val modifiedPayload = cache match {
                case Some(x) =>
                  val children = addChild(id, model, x.children)
                  x.copy(children = children)
                case None =>
                  val children = addChild(id, model, List())
                  Payload(children = children)
              }

              saveToKeystore(modifiedPayload, id)
          } recover {
            case e : Exception =>
              Logger.error(s"[ChildNameController][post] keystore exception whilst loading children: ${e.getMessage}")
              redirectTechnicalDifficulties
          }
      )
    }

  private def addChild(id : Int, model : ChildNamePageModel, children : List[Child]) = {
    val child = Child(id = id, firstname = Some(model.firstName), surname = Some(model.lastName))
    childrenService.addChild(id, children, child)
  }

  private def saveToKeystore(payload : Payload, id: Int)(implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
    cacheClient.savePayload(payload).map {
      children =>
        Logger.debug(s"[ChildNameController][saveToKeystore] saved children redirecting to submission")
        redirectConfirmation(id)
    } recover {
      case e : Exception =>
        Logger.error(s"[ChildNameController][saveToKeystore] keystore exception whilst saving children: ${e.getMessage}")
        redirectTechnicalDifficulties
    }
  }

}
