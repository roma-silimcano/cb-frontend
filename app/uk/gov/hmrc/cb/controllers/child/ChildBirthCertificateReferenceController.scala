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
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.service.keystore.KeystoreService
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm.ChildBirthCertificateReferencePageModel
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.play.http.HeaderCarrier

import uk.gov.hmrc.cb.implicits.Implicits._

import scala.concurrent.Future

/**
  * Created by chrisianson on 06/06/16.
  */

object ChildBirthCertificateReferenceController extends ChildBirthCertificateReferenceController {
  override val authConnector = FrontendAuthConnector
  override val cacheClient = KeystoreService.cacheClient
  override val childrenService = ChildrenManager.childrenService
}

trait ChildBirthCertificateReferenceController extends ChildBenefitController {

  val cacheClient : ChildBenefitKeystoreService
  val childrenService : ChildrenService

  private def redirectTechnicalDifficulties = Redirect(uk.gov.hmrc.cb.controllers.routes.TechnicalDifficultiesController.get())
  private def redirectConfirmation = Redirect(uk.gov.hmrc.cb.controllers.routes.SubmissionConfirmationController.get())

  private val form = ChildBirthCertificateReferenceForm.form
  private def view(form : Form[ChildBirthCertificateReferencePageModel], id : Int)(implicit request: Request[AnyContent]) = uk.gov.hmrc.cb.views.html.child.childBirthCertificate(form, id)

  def get(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      cacheClient.loadChildren.map {
        children =>
          Logger.debug(s"[ChildBirthCertificateReferenceController][get] loaded children $children")

          val child = childrenService.getChildById(id, children)
          if (child.hasBirthCertificateReferenceNumber) {
            Logger.debug(s"[ChildBirthCertificateReferenceController][get] child does exist at index")
            val model : ChildBirthCertificateReferencePageModel = child
            Ok(view(form.fill(model), id))
          } else {
            Ok(view(form, id))
          }
      } recover {
        case e: Exception =>
          Logger.error(s"[ChildBirthCertificateReferenceController][get] keystore exception whilst loading children: ${e.getMessage}")
         redirectTechnicalDifficulties
      }
  }

  def post(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          Logger.debug(s"[ChildBirthCertificateReferenceController][bindFromRequest] invalid form submission $formWithErrors")
          Future.successful(BadRequest(
            view(formWithErrors, id)
          ))},
        model =>
          cacheClient.loadChildren() flatMap {
            cache =>
              handleChildrenWithCallback(cache, id, model) {
                children =>
                  saveToKeystore(children)
              }
          } recover {
            case e : Exception =>
              Logger.error(s"[ChildBirthCertificateReferenceController][post] keystore exception whilst loading children: ${e.getMessage}")
              redirectTechnicalDifficulties
          }
      )
  }

  private def handleChildrenWithCallback(children: List[Child], id : Int, model : ChildBirthCertificateReferencePageModel)(block: (List[Child]) => Future[Result]) = {
    val modified = if (childrenService.childExistsAtIndex(id, children)) {
      // modify child
      val child = childrenService.getChildById(id, children).edit(birthCertificateReference = model.birthCertificateReference)
      childrenService.replaceChild(children, id, child)
    } else {
      // add child
      val child = Child(id = id, birthCertificateReference = Some(model.birthCertificateReference))
      childrenService.addChild(id, children, child)
    }

    block(modified)
  }

  private def saveToKeystore(children : List[Child])(implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
    cacheClient.saveChildren(children).map {
      children =>
        Logger.debug(s"[ChildBirthCertificateReferenceController][saveToKeystore] saved children redirecting to submission")
        redirectConfirmation
    } recover {
      case e : Exception =>
        Logger.error(s"[ChildBirthCertificateReferenceController][saveToKeystore] keystore exception whilst saving children: ${e.getMessage}")
        redirectTechnicalDifficulties
    }
  }
}
