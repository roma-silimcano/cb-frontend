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
import play.api.mvc.{Result, AnyContent, Request, Action}
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController
import uk.gov.hmrc.cb.forms.ChildNameForm
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.cb.implicits.Implicits._
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
  private def view(form : Form[ChildNamePageModel], id : Int)(implicit request: Request[AnyContent]) = uk.gov.hmrc.cb.views.html.child.childname(form, id)

  private def redirectTechnicalDifficulties = Redirect(uk.gov.hmrc.cb.controllers.routes.TechnicalDifficultiesController.get())
  private def redirectConfirmation = Redirect(uk.gov.hmrc.cb.controllers.routes.SubmissionConfirmationController.get())

  def get(id: Int) = Action.async {
    implicit request =>
      cacheClient.loadChildren().map {
        case Some(children) =>
          Logger.debug(s"[ChildNameController][get] loaded children $children")
          if (childrenService.childExistsAtIndex(id, children)) {
            Logger.debug(s"[ChildNameController][get] child does exist at index")
            val model : ChildNamePageModel = childrenService.getChildById(id, children)
            Ok(view(form.fill(model), id))
          } else {
            Logger.debug(s"[ChildNameController][get] child does not exist at index")
            redirectTechnicalDifficulties
          }
        case None =>
          Logger.debug(s"[ChildNameController][get] loaded children None")
          Ok(view(form, id))
      } recover {
        case e : Exception =>
          Logger.error(s"[ChildNameController][get] keystore exception whilst loading children")
          redirectTechnicalDifficulties
      }
  }

  def post(id: Int) = Action.async{
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
            Future.successful(BadRequest(
              view(formWithErrors, id)
            )),
        model =>
          cacheClient.loadChildren() flatMap {
            cache =>
              handleChildrenWithCallback(cache, id, model) {
                children =>
                  saveToKeystore(children)
              }
          } recover {
            case e : Exception =>
              Logger.error(s"[ChildNameController][get] keystore exception whilst loading children")
              redirectTechnicalDifficulties
          }
      )
    }


  /*
    Make this generic in the model it accepts. Extend a ChildPageModel trait and pattern to determine operation
    return list of modified children
    Refactor this into childrenmanager
   */
  private def handleChildrenWithCallback(children: Option[List[Child]], id : Int, model : ChildNamePageModel)(block: (List[Child]) => Future[Result]) = {
    children match {
      case Some(x) =>
        if (childrenService.childExistsAtIndex(id, x)) {
          // modify child
          val originalChild = childrenService.getChildById(id, x)
          val child = originalChild.editFullName(firstName = model.firstName, lastName = model.lastName)
          val amendedList = childrenService.replaceChildInAList(x, id, child)
          block(amendedList)
        } else {
          // add child
          val child = childrenService.createChildWithName(id, model.firstName, model.lastName)
          Logger.debug(s"[ChildNameController][handleChildren] new child $child")
          val amendedList = childrenService.modifyListOfChildren(id, x)
          val amendedWithChild = childrenService.replaceChildInAList(amendedList, id, child)
          Logger.debug(s"[ChildNameController][handleChildren] add child : $amendedWithChild children: $children")
          block(amendedWithChild)
        }
      case None =>
        // create children
        val children = List(childrenService.createChildWithName(id, model.firstName, model.lastName))
        block(children)
    }
  }

  private def saveToKeystore(children : List[Child])(implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
    Logger.debug(s"[ChildNameController][saveToKeystore] saving children to keystore : $children")
    cacheClient.saveChildren(children).map {
      children =>
        Logger.debug(s"[ChildNameController][saveToKeystore] saved children redirecting to submission")
        redirectConfirmation
    } recover {
      case e : Exception =>
        Logger.error(s"[ChildNameController][saveToKeystore] keystore exception whilst saving children: $children")
        redirectTechnicalDifficulties
    }
  }

}
