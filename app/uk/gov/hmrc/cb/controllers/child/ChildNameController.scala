package uk.gov.hmrc.cb.controllers.child

import play.api.Logger
import play.api.mvc.Action
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.{routes, ChildBenefitController}
import uk.gov.hmrc.cb.forms.ChildNameForm
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.models.Child

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
          Ok(children.toString)
        case None =>
          Ok("")
      } recover {
        case e : Exception =>
          Logger.error(s"[ChildNameController][get] keystore exception whilst loading children")
          Redirect(routes.TechnicalDifficultiesController.get())
      }
  }

  def post(id: Int) = Action.async {
    implicit request =>
      ChildNameForm.form.bindFromRequest().fold(
        formWithErrors =>
            BadRequest(formWithErrors.get.toString),
        success =>
            cacheClient.loadChildren().map {
              case Some(children) =>
                val result = if (childrenService.childExistsAtIndex(id, children)) {
                  // modify child
                  val originalChild = childrenService.getChildById(id, children).head
                  val child = originalChild.copy(firstname = success.firstName, surname = success.lastName)
                  childrenService.replaceChildInAList(children, id, child)
                } else {
                  // add child
                  createChild(id, success.firstName, success.lastName)
                }
                saveToKeystore(children)
              case None =>
                val children = createChild(id, success.firstName, success.lastName)
                saveToKeystore(children)
            } recover {
              case e : Exception =>
                Logger.error(s"[ChildNameController][get] keystore exception whilst loading children")
                Redirect(routes.TechnicalDifficultiesController.get())
            }
      )

  }

  private def createChild(id: Int, firstName: String, lastName : String) : List[Child] = {
    val children = childrenService.createListOfChildren(requiredNumberOfChildren = id)
    val modifiedChild = children.head.copy(firstname = Some(firstName), surname = Some(lastName))
    childrenService.replaceChildInAList(children, id, modifiedChild)
  }

  private def saveToKeystore(children : List[Child]) = {
    cacheClient.saveChildren(children).map {
      Redirect(routes.SubmissionConfirmationController.get())
    } recover {
      case e : Exception =>
        Logger.error(s"[ChildNameController][saveToKeystore] keystore exception whilst saving children")
        Redirect(routes.TechnicalDifficultiesController.get())
    }
  }

}
