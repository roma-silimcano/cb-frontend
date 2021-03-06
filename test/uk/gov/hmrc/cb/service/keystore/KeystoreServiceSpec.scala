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

import org.mockito.Matchers.{eq => mockEq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.config.WSHttp
import uk.gov.hmrc.cb.connectors.KeystoreConnector
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.models.payload.submission.Payload
import uk.gov.hmrc.cb.models.payload.submission.child.Child
import uk.gov.hmrc.cb.models.payload.submission.claimant.Claimant
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by chrisianson on 01/06/16.
  */
class KeystoreServiceSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  val mockSessionCache = mock[SessionCache]
  class MockChildBenefitService extends ChildBenefitKeystoreService with CBKeystoreKeys {
    override val sessionCache = mockSessionCache
  }

  val mockKeystoreService = new MockChildBenefitService

  object TestKeystoreService {
    val cacheClient = mockKeystoreService
  }

  val children : List[Child] = List(Child.apply(id = 1))

  val payload : Option[Payload] = Some(Payload(children = children, claimant = Some(Claimant("John", "Smith", None, None))))

  "GET data should " should {

    "fetch payload" in {
      implicit val request = FakeRequest().withSession(CBSessionProvider.generateSessionId())
      implicit val hc = HeaderCarrier()
      when(mockSessionCache.fetchAndGetEntry[Payload](mockEq("cb-payload"))(any(), any())).thenReturn(Future.successful(payload))
      val result = Await.result(TestKeystoreService.cacheClient.loadPayload()(hc, request), 10 seconds)
      result shouldBe payload
    }

  }

  "POST data should" should {

    "save payload" in {
      when(mockSessionCache.fetchAndGetEntry[Payload](mockEq("cb-payload"))(any(), any())).thenReturn(Future.successful(None))
      implicit val request = FakeRequest().withSession(CBSessionProvider.generateSessionId())
      implicit val hc = HeaderCarrier()
      val json = Json.toJson[Payload](payload.get)

      when(mockSessionCache.cache[Payload](mockEq("cb-payload"), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("cb-payload" -> json))))
      val result = Await.result(TestKeystoreService.cacheClient.savePayload(payload.get)(hc, request), 10 seconds)
      result shouldBe payload
    }

  }

  "KeystoreService.cacheClient" should {

    "be instance of ChildBenefitKeystoreService" in {
      KeystoreService.cacheClient shouldBe a[ChildBenefitKeystoreService]
      KeystoreService.cacheClient.sessionCache shouldBe a[SessionCache]
    }
  }

  "KeystoreConnector" should {

    "initialise with dependencies" in {
      KeystoreConnector.http shouldBe a[WSHttp.type]
      KeystoreConnector.defaultSource shouldBe a[String]
      KeystoreConnector.baseUri shouldBe a[String]
      KeystoreConnector.domain shouldBe a[String]
    }
  }
}
