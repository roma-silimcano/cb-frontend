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
import play.api.mvc.{AnyContent, Request, Action}
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController
import uk.gov.hmrc.cb.forms.ChildNameForm
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.models.Child
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

  def get(id: Int) = Action.async {
    implicit request =>
      cacheClient.loadChildren().map {
        case Some(children) =>
          Logger.debug(s"[ChildNameController][get] loaded children $children")
          if (childrenService.childExistsAtIndex(id, children)) {
            Logger.debug(s"[ChildNameController][get] child does exist at index")
            Ok(children.toString)
          } else {
            Logger.debug(s"[ChildNameController][get] child does not exist at index")
            Redirect(uk.gov.hmrc.cb.controllers.routes.TechnicalDifficultiesController.get())
          }
        case None =>
          Logger.debug(s"[ChildNameController][get] loaded children None")
          Ok("")
      } recover {
        case e : Exception =>
          Logger.error(s"[ChildNameController][get] keystore exception whilst loading children")
          Redirect(uk.gov.hmrc.cb.controllers.routes.TechnicalDifficultiesController.get())
      }
  }

  def post(id: Int) = Action.async{
    implicit request =>
      ChildNameForm.form.bindFromRequest().fold(
        formWithErrors =>
            Future.successful(BadRequest("")),
        model =>
            cacheClient.loadChildren() flatMap {
              children =>
                handleChildren(children, id, model)
            } recover {
              case e : Exception =>
                Logger.error(s"[ChildNameController][get] keystore exception whilst loading children")
                Redirect(uk.gov.hmrc.cb.controllers.routes.TechnicalDifficultiesController.get())
            }
      )
    }

  private def handleChildren(children: Option[List[Child]], id: Int, model : ChildNamePageModel)(implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
      Logger.debug(s"[ChildNameController][handleChildren] $children, model : $model")
      children match {
        case Some(x) => {
          if (childrenService.childExistsAtIndex(id, x)) {
            // modify child
            val originalChild = childrenService.getChildById(id, x)
            val child = originalChild.copy(firstname = Some(model.firstName), surname = Some(model.lastName))
            val r = childrenService.replaceChildInAList(x, id, child)
            saveToKeystore(r)
          } else {
            // add child
            val c = createChild(id, model.firstName, model.lastName)
            Logger.debug(s"[ChildNameController][handleChildren] new child $c")
            val amended = childrenService.modifyListOfChildren(id, x)
            val amendedWithChild = childrenService.replaceChildInAList(amended, id, c)
            Logger.debug(s"[ChildNameController][handleChildren] add child : $amendedWithChild children: $children")
            saveToKeystore(amendedWithChild)
          }
        }
        case None =>
          // create children
          val children = List(createChild(id, model.firstName, model.lastName))
          saveToKeystore(children)
      }
  }

  /*
    Incorrect logic for unit test was not replacing second child correctly,
    this was replacing the first child always and leaving the second unchanged
    refactor this into the children manager
   */
  private def createChild(id: Int, firstName: String, lastName : String) : Child = {
    val child = childrenService.createChild(id)
    child.copy(firstname = Some(firstName), surname = Some(lastName))
  }

  private def saveToKeystore(children : List[Child])(implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
    Logger.debug(s"[ChildNameController][saveToKeystore] saving children to keystore : $children")
    cacheClient.saveChildren(children).map {
      children =>
        Logger.debug(s"[ChildNameController][saveToKeystore] saved children redirecting to submission")
        Redirect(uk.gov.hmrc.cb.controllers.routes.SubmissionConfirmationController.get())
    } recover {
      case e : Exception =>
        Logger.error(s"[ChildNameController][saveToKeystore] keystore exception whilst saving children: $children")
        Redirect(uk.gov.hmrc.cb.controllers.routes.TechnicalDifficultiesController.get())
    }
  }

}
