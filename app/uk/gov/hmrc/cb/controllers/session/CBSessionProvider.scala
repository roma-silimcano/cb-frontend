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

package uk.gov.hmrc.cb.controllers.session

/**
 * Created by adamconder on 06/06/2016.
 */

import java.util.UUID

import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.UnauthorisedAction
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

object CBSessionProvider {

  private val INITIAL_CONTROLLER = uk.gov.hmrc.cb.controllers.routes.UpdateChildBenefitController.get()

  /**
   * CBSessionProvider is used to generate a session for the users browser
   * so that we can retrieve the data from keystore
   */

  private val NO_SESSION = "NO-SESSION"

  // construct a new session with the old session data
  def generateSession()(implicit request: Request[AnyContent]) = {
    val newId = generateSessionId()
    val session = Session(request.session.data ++ Map(newId))
    session
  }

  def generateSessionId() : (String, String) = SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

  def getSessionId()(implicit request : Request[AnyContent]) = request.session.get(SessionKeys.sessionId)

  def callbackWithSession(implicit request : Request[AnyContent]) = Results.Redirect(INITIAL_CONTROLLER).withSession(generateSession())

  def futureRequest(result: Result) = Future.successful(result)

  /**
   * every session should have an ID: required by key-store
   * If no session Id is found or session was deleted (NOSESSION), user is redirected to welcome page, where new session id will be issued on submit
   * @return redirect to required page
   */

  def withSession(f: => Request[AnyContent] => Future[Result]) : Action[AnyContent] = {
    UnauthorisedAction.async {
      implicit request : Request[AnyContent] =>
        getSessionId match {
          // $COVERAGE-OFF$Disabling highlighting by default until a workaround for https://issues.scala-lang.org/browse/SI-8596 is found
          case Some(NO_SESSION) =>
            futureRequest(callbackWithSession) // Continue original request with new session
          // $COVERAGE-ON
          case None =>
            futureRequest(callbackWithSession)
          case _ =>
            f(request) // Carry on
        }
    }
  }
}
