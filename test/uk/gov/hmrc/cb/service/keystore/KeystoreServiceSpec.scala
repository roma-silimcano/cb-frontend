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

package uk.gov.hmrc.cb.service.keystore

import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.config.FrontendAuthConnector
import uk.gov.hmrc.play.frontend.auth.Actions
import play.api.http.Status
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

/**
  * Created by chrisianson on 01/06/16.
  */
class KeystoreServiceSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  trait CBAuthConnector {
    lazy val authConnector = FrontendAuthConnector
  }

  object TestController extends TestController with KeystoreService with CBAuthConnector

  trait TestController extends FrontendController with Actions {
    this: KeystoreService with CBAuthConnector =>

    val cacheClient : ChildBenefitKeystoreService

    def testKeystoreSave() = UnauthorisedAction {
      implicit request =>
        cacheClient.cacheEntryForSession[String]("test", "calDetails").map {
          res =>
            Ok("Successfully inserted")
        } recover {
          case e : Exception =>
            e.getMessage match {
              case s : String =>
                InternalServerError(s)
              case _ =>
                InternalServerError
            }

        }
    }

    def testKeystoreFetch() = UnauthorisedAction {
      implicit request =>
        cacheClient.fetchEntryForSession[String]("test").map {
          case Some(x) =>
            Ok("fetched object")
          case None =>
            Ok("cound not fetch object")
        } recover {
          case e : Exception =>
            e.getMessage match {
              case s : String =>
                InternalServerError(s)
              case _ =>
                InternalServerError
            }
        }
    }
  }

  val testController = new TestController with KeystoreService with CBAuthConnector {
    override val cacheClient = mock[ChildBenefitKeystoreService]
  }

  "KeystoreService " when {

    "GET data should " should {

      "(GET) return 200 when data is not found for key" in {
        implicit val request = FakeRequest()
        val controller = testController
        /// could return Some("") or None both but return Status.OK / None is where the object hasn't been saved to keystore yet
        when(controller.cacheClient.fetchEntryForSession[String](mockEq("test"))(any(),any(),any())).thenReturn(Future.successful(Some("test")))
        val result = await(controller.testKeystoreFetch() (request))
        status(result) shouldBe Status.OK
      }

      "(GET) throw an Exception when keystore is down" in {
        implicit val request = FakeRequest()
        val controller = testController
        when(controller.cacheClient.fetchEntryForSession[String](mockEq("test"))(any(),any(),any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(controller.testKeystoreFetch() (request))
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "POST data" should {

      "(POST) Successfully insert the data to keystore" in {
        implicit val request = FakeRequest()
        val controller = testController
        /// could return Some("") or None both but return Status.OK / None is where the object hasn't been saved to keystore yet
        when(controller.cacheClient.cacheEntryForSession[String](any(), mockEq("calDetails"))(any(),any(),any())).thenReturn(Future.successful(Some("test")))
        val result = await(controller.testKeystoreSave() (request))
        status(result) shouldBe Status.OK
      }

      "(POST) throw an Exception when keystore is down" in {
        implicit val request = FakeRequest()
        val controller = testController
        when(controller.cacheClient.cacheEntryForSession[String](any(), mockEq("calDetails"))(any(),any(),any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(controller.testKeystoreSave() (request))
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }
}
