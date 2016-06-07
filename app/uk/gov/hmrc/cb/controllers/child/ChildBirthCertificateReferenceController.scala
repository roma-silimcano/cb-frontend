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

import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController
import uk.gov.hmrc.cb.service.keystore.KeystoreService
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

  def get(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      cacheClient.loadChildren.map {
        case Some(children) =>
          if (childrenService.childExistsAtIndex(id, children)) {
            Ok(children.toString)
          } else {
            redirectTechnicalDifficulties
          }
        case None =>
          Ok
      } recover {
        case e: Exception =>
         redirectTechnicalDifficulties
      }
  }

  def post(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      ChildBirthCertificateReferenceForm.form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest("")),
        model =>
          cacheClient.loadChildren() flatMap {
            cache =>
              handleChildrenWithCallback(cache, id, model) {
                children =>
                  saveToKeystore(children)
              }
          } recover {
            case e : Exception =>
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
        redirectConfirmation
    } recover {
      case e : Exception =>
        redirectTechnicalDifficulties
    }
  }
}
