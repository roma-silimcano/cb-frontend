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
import play.api.mvc.Results.Status
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.HelloTestForm
import uk.gov.hmrc.cb.forms.HelloTestForm.HelloTestPageModel
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.models.payload.submission.child.Child
import uk.gov.hmrc.cb.service.keystore.KeystoreService
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.cb.implicits.Implicits._

import scala.concurrent.Future

/**
  * Created by anuja on 28/06/16.
  */
object HelloTestController  extends HelloTestController {
  override val authConnector = FrontendAuthConnector
  override val cacheClient = KeystoreService.cacheClient
  override val childrenService = ChildrenManager.childrenService
}

trait HelloTestController extends ChildBenefitController {
  //
  val cacheClient: ChildBenefitKeystoreService
  val childrenService: ChildrenService


  private val form = HelloTestForm.form
  private def view(status: Status, form : Form[HelloTestPageModel], id : Int)(implicit request: Request[AnyContent]) = {
    status(uk.gov.hmrc.cb.views.html.child.helloTest(form,id))
  }

  private def redirectConfirmation(id : Int) = Redirect(uk.gov.hmrc.cb.controllers.child.routes.ChildDateOfBirthController.get(id))

  def get(id: Int) = CBSessionProvider.withSession {
    implicit request =>
      cacheClient.loadChildren().map {
        children =>
          Logger.debug(s"[ChildBirthCertificateReferenceController][get] loaded children $children")
          val resultWithNoChild = view(Ok, form, id)
          childrenService.getChildById(id, children).fold(resultWithNoChild){
            child =>
              if(child.hasName1) {
                Logger.debug(s"[ChildBirthCertificateReferenceController][get] child does exist at index")
                val model : HelloTestPageModel = child
                view(Ok, form.fill(model), id)
              } else {
                resultWithNoChild
              }
          }
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
          Logger.info(s"[ChildNameController][bindFromRequest] invalid form submission $formWithErrors")
          Future.successful(
            view(BadRequest, formWithErrors, id)
          )},
        model =>
          cacheClient.loadChildren() flatMap {
            cache =>
              handleChildrenWithCallback(cache, id, model) {
                children =>
                  saveToKeystore(children, id)
              }
          } recover {
            case e : Exception =>
              Logger.error(s"[ChildNameController][get] keystore exception whilst loading children: ${e.getMessage}")
              redirectTechnicalDifficulties
          }
      )
  }

  private def addChild(id : Int, model : HelloTestPageModel, children : List[Child]) = {
    val child = Child(id = id, firstname = Some(model.firstName))
    childrenService.addChild(id, children, child)
  }

  private def handleChildrenWithCallback(children: List[Child], id : Int, model : HelloTestPageModel)
                                        (block: List[Child] => Future[Result]) = {
    val child = childrenService.getChildById(id, children).fold {
      addChild(id, model, children)
    }{
      c =>
        val modified = c.edit(model.firstName)
        childrenService.replaceChild(children, id, modified)
    }

    block(child)
  }

  private def saveToKeystore(children : List[Child], id: Int)(implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
    cacheClient.saveChildren(children).map {
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
