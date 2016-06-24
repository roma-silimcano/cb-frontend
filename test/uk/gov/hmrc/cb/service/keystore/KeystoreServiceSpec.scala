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
import uk.gov.hmrc.cb.models.payload.submission.child.Child
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

  "GET data should " should {

    "fetch children" in {
      implicit val request = FakeRequest().withSession(CBSessionProvider.generateSessionId())
      implicit val hc = HeaderCarrier()
      when(mockSessionCache.fetchAndGetEntry[List[Child]](mockEq("cb-children"))(any(), any())).thenReturn(Future.successful(Some(children)))
      val result = Await.result(TestKeystoreService.cacheClient.loadChildren()(hc, request), 10 seconds)
      result shouldBe children
    }

    "return Nil when no children exist" in {
      implicit val request = FakeRequest().withSession(CBSessionProvider.generateSessionId())
      implicit val hc = HeaderCarrier()
      when(mockSessionCache.fetchAndGetEntry[List[Child]](mockEq("cb-children"))(any(), any())).thenReturn(Future.successful(None))
      val result = Await.result(TestKeystoreService.cacheClient.loadChildren()(hc, request), 10 seconds)
      result shouldBe Nil
    }

  }

  "POST data should" should {

    "save children" in {
      when(mockSessionCache.fetchAndGetEntry[List[Child]](mockEq("cb-children"))(any(), any())).thenReturn(Future.successful(None))

      implicit val request = FakeRequest().withSession(CBSessionProvider.generateSessionId())
      implicit val hc = HeaderCarrier()
      val json = Json.toJson[List[Child]](children)

      when(mockSessionCache.cache[List[Child]](mockEq("cb-children"), any())(any(), any())).thenReturn(Future.successful(CacheMap("sessionValue", Map("cb-children" -> json))))
      val result = Await.result(TestKeystoreService.cacheClient.saveChildren(children)(hc, request), 10 seconds)
      result shouldBe Some(children)
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
