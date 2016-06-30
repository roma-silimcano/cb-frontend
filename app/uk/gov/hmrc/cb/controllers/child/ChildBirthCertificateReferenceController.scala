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
import play.api.mvc._
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm
import uk.gov.hmrc.cb.forms.ChildBirthCertificateReferenceForm.ChildBirthCertificateReferencePageModel
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.cb.implicits.Implicits._
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.models.payload.submission.child.Child

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

  private def redirectConfirmation = Redirect(uk.gov.hmrc.cb.controllers.claimant.routes.ClaimantNameController.get())

  private val form = ChildBirthCertificateReferenceForm.form
  private def view(status : Status, form : Form[ChildBirthCertificateReferencePageModel], id : Int)
                  (implicit request: Request[AnyContent]) = {
    status(uk.gov.hmrc.cb.views.html.child.childBirthCertificate(form, id))
  }

  def get(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      val resultWithNoChild = view(Ok, form, id)
      cacheClient.loadPayload().map {
        payload =>
          Logger.error(s"[ChildBirthCertificateReferenceController][get] loaded payload")
          payload.fold(
            resultWithNoChild
          )(
            cache => {
              childrenService.getChildById(id, cache.children).fold(resultWithNoChild){
                child =>
                  if(child.hasBirthCertificateReferenceNumber) {
                    Logger.debug(s"[ChildBirthCertificateReferenceController][get] child does exist at index")
                    val model : ChildBirthCertificateReferencePageModel = child
                    view(Ok, form.fill(model), id)
                  } else {
                    resultWithNoChild
                  }
              }
            }
          )
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
          Logger.info(s"[ChildBirthCertificateReferenceController][bindFromRequest] invalid form submission $formWithErrors")
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
              Logger.error(s"[ChildBirthCertificateReferenceController][get] keystore exception whilst loading children: ${e.getMessage}")
              redirectTechnicalDifficulties
          }
      )
  }

  private def addChild(id : Int, model : ChildBirthCertificateReferencePageModel, children : List[Child]) = {
    val child = Child(id = id, birthCertificateReference = Some(model.birthCertificateReference))
    val modified = childrenService.addChild(id, children, child)
    modified
  }

  private def saveToKeystore(payload : Payload, id: Int)(implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
    cacheClient.savePayload(payload).map {
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
