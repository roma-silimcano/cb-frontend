package uk.gov.hmrc.cb.controllers.child

import play.api.mvc.Action
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.cb.controllers.ChildBenefitController

import scala.concurrent.Future

/**
 * Created by adamconder on 01/06/2016.
 */

object ChildNameController extends ChildNameController {
  override val authConnector = FrontendAuthConnector
  override val cacheClient = KeystoreService.cacheClient
}

trait ChildNameController extends ChildBenefitController {

  val cacheClient : ChildBenefitKeystoreService

  def get = Action.async {
    implicit request =>
      Future.successful(Ok(""))
  }

  def post = Action.async {
    implicit request =>
      Future.successful(Ok(""))
  }

}
