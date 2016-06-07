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
        case Some(children) =>
          Logger.debug(s"[ChildBirthCertificateReferenceController][get] loaded children $children")
          if (childrenService.childExistsAtIndex(id, children)) {
            Logger.debug(s"[ChildBirthCertificateReferenceController][get] child does exist at index")
            val child = childrenService.getChildById(id, children)
            if (child.hasBirthCertificateReferenceNumber) {
              val model : ChildBirthCertificateReferencePageModel = child
              Ok(view(form.fill(model), id))
            } else {
              Ok(view(form, id))
            }
          } else {
            Logger.debug(s"[ChildBirthCertificateReferenceController][get] child does not exist at index")
            redirectTechnicalDifficulties
          }
        case None =>
          Logger.debug(s"[ChildBirthCertificateReferenceController][get] loaded children None")
          Ok(view(form, id))
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

  /*
    Make this generic in the model it accepts. Extend a ChildPageModel trait and pattern to determine operation
    return list of modified children
    Refactor this into childrenmanager
   */
  private def handleChildrenWithCallback(children: Option[List[Child]], id : Int, model : ChildBirthCertificateReferencePageModel)(block: (List[Child]) => Future[Result]) = {
    children match {
      case Some(x) =>
        if (childrenService.childExistsAtIndex(id, x)) {
          // modify child
          val originalChild = childrenService.getChildById(id, x)
          val child = originalChild.editUniqueReferenceNumber(birthCertificateReference = model.birthCertificateReference)
          val amendedList = childrenService.replaceChildInAList(x, id, child)
          block(amendedList)
        } else {
          // add child
          val child = childrenService.createChildWithBirthCertificateReference(id, model.birthCertificateReference)
          val amendedList = childrenService.modifyListOfChildren(id, x)
          val amendedWithChild = childrenService.replaceChildInAList(amendedList, id, child)
          block(amendedWithChild)
        }
      case None =>
        // create children
        val children = List(childrenService.createChildWithBirthCertificateReference(id, model.birthCertificateReference))
        block(children)
    }
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
