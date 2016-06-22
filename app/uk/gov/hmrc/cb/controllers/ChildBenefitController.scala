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

package uk.gov.hmrc.cb.controllers

import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.{AnyContent, Request, Result, Action}
import uk.gov.hmrc.cb.forms.ChildDateOfBirthForm.ChildDateOfBirthPageModel
import uk.gov.hmrc.cb.managers.ChildrenManager.ChildrenService
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

/**
 * Created by andrew on 03/05/16.
 */

trait ChildBenefitController extends FrontendController with Actions {

  val authConnector: AuthConnector

  protected def redirectTechnicalDifficulties = Redirect(uk.gov.hmrc.cb.controllers.routes.TechnicalDifficultiesController.get())

}

trait ChildBenefitChildrenController extends ChildBenefitController {

  val childrenService : ChildrenService
  val cacheClient : ChildBenefitKeystoreService

//  private def addChild(id : Int, model : ChildDateOfBirthPageModel, children : List[Child]) = {
//    val child = Child(id = id, dob = Some(model.dateOfBirth))
//    childrenService.addChild(id, children, child)
//  }
//
//  protected def handleChildrenWithCallback(children : List[Child], id : Int, model : ChildDateOfBirthPageModel)
//                                        (block: List[Child] => Future[Result]) = {
//    val modified = childrenService.getChildById(id, children).fold {
//     addChild(id, model, children)
//    }{
//      c =>
//        val editedChild = c.edit(model.dateOfBirth)
//        childrenService.replaceChild(children, id, editedChild)
//    }
//
//    block(modified)
//  }

  protected def saveToKeystore(children : List[Child])
                              (block: Either[Option[List[Child]], Result] => Result)
                              (implicit hc : HeaderCarrier, request: Request[AnyContent]) = {
    cacheClient.saveChildren(children).map {
      children =>
        Logger.debug(s"[ChildBenefitChildrenController][saveToKeystore] saved children redirecting to submission")
        block(Left(children))
    } recover {
      case e : Exception =>
        Logger.error(s"[ChildBenefitChildrenController][saveToKeystore] keystore exception whilst saving children: ${e.getMessage}")
        block(Right(redirectTechnicalDifficulties))
    }
  }









//  type DateOfBirth = DateTime

//  editChildWithCallback[DateOfBirth](Child(id = 0), DateTime.now, childrenService.edit)

  protected def editChildWithCallback[T](child : Child, value : T, f : (Child, T)  => Child)
                                          (block: Child => Future[Result]) = {
    block(f(child, value))
  }

//  addChildWithCallback[DateTime](List(), DateTime.now, 1, childrenService.create)

  protected def addChildWithCallback[T](children: List[Child], value : T, index : Int, f : (Int, T) => Child)(block : List[Child] => Future[Result]) = {
    val child = f(index, value)
    val newChildren = childrenService.addChild(index, children, child)
    block(newChildren)
  }

}
